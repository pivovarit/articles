package com.pivovarit.deadlock;

import com.pivovarit.deadlock.entity.Account;
import com.pivovarit.deadlock.entity.AccountH4;
import com.pivovarit.deadlock.entity.Transfer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.math.BigDecimal;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Reproduces the deadlock introduced by upgrading from Hibernate 4 to Hibernate 5
 * in a system that manually increments a JPA-managed sequence inside a large
 * transaction.
 *
 * <h2>Background — what changed between Hibernate 4 and 5</h2>
 *
 * <pre>
 * Hibernate 4  →  @SequenceGenerator default allocationSize = 1
 *                 Database sequence:  CREATE SEQUENCE … INCREMENT BY 1
 *                 A manual SELECT nextval('account_seq') advances the counter by 1.
 *                 Consecutive nextval() calls return: 1, 2, 3, 4 …
 *
 * Hibernate 5  →  @SequenceGenerator default allocationSize = 50
 *                 Database sequence:  CREATE SEQUENCE … INCREMENT BY 50
 *                 A manual SELECT nextval('account_seq') advances the counter by 50.
 *                 Consecutive nextval() calls return: 50, 100, 150, 200 …
 * </pre>
 *
 * <h2>The legacy code pattern that triggers the deadlock</h2>
 *
 * Many Hibernate 4 applications mixed JPA persistence with direct JDBC sequence
 * calls to pre-fetch entity IDs:
 *
 * <pre>
 * // Fetch the next two IDs for a "settlement pair" before opening the transaction
 * long idA = (Long) em.createNativeQuery("SELECT nextval('account_seq')").getSingleResult();
 * long idB = (Long) em.createNativeQuery("SELECT nextval('account_seq')").getSingleResult();
 *
 * // Create both accounts and the mutual transfer in one transaction
 * Account a = new Account(idA, "Bank A");
 * Account b = new Account(idB, "Bank B");
 * Transfer t = new Transfer(a, b, amount);
 * em.persist(a); em.persist(b); em.persist(t);
 * </pre>
 *
 * <h2>Why it was safe with Hibernate 4 (INCREMENT BY 1)</h2>
 *
 * <pre>
 * Thread-1: nextval() → 1,  nextval() → 2   idA=1, idB=2
 * Thread-2: nextval() → 3,  nextval() → 4   idA=3, idB=4
 *
 * Thread-1 creates Account(1), Account(2), Transfer(1→2)  — all in Thread-1's TX
 * Thread-2 creates Account(3), Account(4), Transfer(3→4)  — all in Thread-2's TX
 *
 * No cross-transaction FK references → no deadlock.
 * </pre>
 *
 * <h2>Why it deadlocks with Hibernate 5 (INCREMENT BY 50)</h2>
 *
 * <pre>
 * Thread-1: nextval() → 50,  nextval() → 100   idA=50,  idB=100
 * Thread-2: nextval() → 150, nextval() → 200   idA=150, idB=200
 *
 *  ... if the concurrent interleaving gives Thread-1 idA=50 and Thread-2 idB=50,
 *  or the application treats "every 50th ID" as the boundary between settlement
 *  batches that run in parallel:
 *
 * Thread-1 TX:
 *   INSERT Account(id=50)             → holds EXCLUSIVE ROW lock on Account(50)
 *   INSERT Transfer(from=50, to=100)  → FK check: needs ROW SHARE lock on Account(100)
 *                                        Account(100) is being inserted by Thread-2 → WAIT
 *
 * Thread-2 TX:
 *   INSERT Account(id=100)            → holds EXCLUSIVE ROW lock on Account(100)
 *   INSERT Transfer(from=100, to=50)  → FK check: needs ROW SHARE lock on Account(50)
 *                                        Account(50) is being inserted by Thread-1 → WAIT
 *
 * Thread-1 waits for Thread-2, Thread-2 waits for Thread-1 → DEADLOCK
 * PostgreSQL detects the cycle and rolls back one of the transactions.
 * </pre>
 *
 * <h2>PostgreSQL FK lock mechanics</h2>
 *
 * When a child row is inserted with a FK pointing to a parent that is being
 * inserted (uncommitted) by another transaction, PostgreSQL's referential-integrity
 * trigger issues a {@code SELECT … FOR SHARE} on the parent row.  That share lock
 * is blocked by the inserting transaction's exclusive lock, so the FK check waits.
 * Two transactions waiting for each other in this way form the classic AB-BA cycle.
 *
 * <h2>Running the demo</h2>
 * <ol>
 *   <li>{@code docker compose up -d}
 *   <li>{@code mvn compile exec:java -Dexec.mainClass=com.pivovarit.deadlock.DeadlockReproducer}
 * </ol>
 */
public class DeadlockReproducer {

    private static final Logger log = LoggerFactory.getLogger(DeadlockReproducer.class);

    public static void main(String[] args) throws Exception {
        System.out.println("=== Hibernate Sequence-Deadlock Reproducer ===\n");

        System.out.println("--- SCENARIO 1: Hibernate 5 defaults (allocationSize=50, INCREMENT BY 50) ---");
        System.out.println("    Expected: DEADLOCK\n");
        demonstrateDeadlock();

        System.out.println("\n--- SCENARIO 2: Hibernate 4-compatible (allocationSize=1, INCREMENT BY 1) ---");
        System.out.println("    Expected: both transactions commit successfully\n");
        demonstrateNoDeadlock();
    }

    // -------------------------------------------------------------------------
    // SCENARIO 1 — Hibernate 5, INCREMENT BY 50 → DEADLOCK
    // -------------------------------------------------------------------------

    /**
     * Simulates two concurrent "settlement batch" transactions that each:
     * <ol>
     *   <li>Pre-fetch an account ID via a manual {@code SELECT nextval('account_seq')}
     *       (legacy pattern used to pre-assign IDs before opening the transaction).
     *   <li>Open a large transaction and INSERT the Account row.
     *   <li>INSERT a Transfer that references the <em>other</em> thread's Account.
     * </ol>
     *
     * With {@code INCREMENT BY 50}, each manual nextval() call consumes 50 IDs.
     * The IDs assigned to Thread-1 and Thread-2 are 50 apart, which the application
     * interprets as "different batches → process concurrently".  The resulting
     * cross-transaction FK references cause the deadlock.
     */
    private static void demonstrateDeadlock() throws Exception {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hibernate5-deadlock");

        try {
            /*
             * PRE-FETCH PHASE — simulates the legacy "manual sequence increment".
             *
             * In Hibernate 4 (INCREMENT BY 1):  returns 1, 2
             * In Hibernate 5 (INCREMENT BY 50): returns 50, 100
             *
             * The application used these IDs to decide whether two accounts belong
             * to the same batch (adjacent IDs → same TX) or different batches
             * (IDs 50 apart → run concurrently).  After the H5 upgrade the IDs
             * are always 50 apart, so every pair is processed in separate TXs.
             */
            long accountIdT1 = fetchNextSequenceValue(emf, "account_seq");
            long accountIdT2 = fetchNextSequenceValue(emf, "account_seq");

            log.info("[pre-fetch] Thread-1 will use Account id={}", accountIdT1);
            log.info("[pre-fetch] Thread-2 will use Account id={}", accountIdT2);
            log.info("[pre-fetch] ID gap = {} ({})",
                    accountIdT2 - accountIdT1,
                    accountIdT2 - accountIdT1 == 1
                            ? "adjacent → same TX, no deadlock"
                            : "50 apart → concurrent TXs → DEADLOCK");

            CountDownLatch bothStarted = new CountDownLatch(2);  // both threads have acquired their row lock
            CountDownLatch proceed     = new CountDownLatch(1);  // release both to attempt the FK insert

            AtomicReference<Exception> t1Error = new AtomicReference<>();
            AtomicReference<Exception> t2Error = new AtomicReference<>();

            // Thread 1: INSERT Account(accountIdT1), then Transfer(T1 → T2)
            Thread thread1 = new Thread(() -> {
                EntityManager em = emf.createEntityManager();
                try {
                    em.getTransaction().begin();

                    // Create and INSERT this thread's account — acquires EXCLUSIVE row lock
                    Account myAccount = new Account("Settlement-Account-T1");
                    em.persist(myAccount);
                    em.flush(); // Force INSERT now so the row lock is held before we signal

                    log.info("[T1] Inserted {} — holding exclusive row lock", myAccount);
                    bothStarted.countDown();

                    // Wait until both threads hold their locks before proceeding
                    // This maximises the chance that both FK checks hit in-progress rows
                    proceed.await(10, TimeUnit.SECONDS);

                    /*
                     * Insert a Transfer referencing Thread-2's account, which Thread-2
                     * is CURRENTLY inserting (not yet committed).
                     *
                     * PostgreSQL FK enforcement issues:
                     *   SELECT 1 FROM account WHERE id = accountIdT2 FOR SHARE
                     *
                     * That row is locked exclusively by Thread-2 → this thread WAITS.
                     * Meanwhile Thread-2 does the same for accountIdT1 → DEADLOCK.
                     */
                    Account theirAccount = em.getReference(Account.class, accountIdT2);
                    Transfer transfer = new Transfer(myAccount, theirAccount, new BigDecimal("1000.00"));
                    em.persist(transfer);

                    em.getTransaction().commit();
                    log.info("[T1] Committed successfully (no deadlock occurred)");
                } catch (Exception e) {
                    t1Error.set(e);
                    log.error("[T1] Transaction rolled back: {}", rootCause(e));
                    if (em.getTransaction().isActive()) em.getTransaction().rollback();
                } finally {
                    em.close();
                }
            }, "settlement-thread-1");

            // Thread 2: INSERT Account(accountIdT2), then Transfer(T2 → T1)
            Thread thread2 = new Thread(() -> {
                EntityManager em = emf.createEntityManager();
                try {
                    em.getTransaction().begin();

                    Account myAccount = new Account("Settlement-Account-T2");
                    em.persist(myAccount);
                    em.flush();

                    log.info("[T2] Inserted {} — holding exclusive row lock", myAccount);
                    bothStarted.countDown();

                    proceed.await(10, TimeUnit.SECONDS);

                    // Transfer referencing Thread-1's account (held by Thread-1) → WAITS
                    Account theirAccount = em.getReference(Account.class, accountIdT1);
                    Transfer transfer = new Transfer(myAccount, theirAccount, new BigDecimal("2000.00"));
                    em.persist(transfer);

                    em.getTransaction().commit();
                    log.info("[T2] Committed successfully (no deadlock occurred)");
                } catch (Exception e) {
                    t2Error.set(e);
                    log.error("[T2] Transaction rolled back: {}", rootCause(e));
                    if (em.getTransaction().isActive()) em.getTransaction().rollback();
                } finally {
                    em.close();
                }
            }, "settlement-thread-2");

            thread1.start();
            thread2.start();

            // Wait until both threads have inserted their accounts and are holding row locks
            if (!bothStarted.await(10, TimeUnit.SECONDS)) {
                System.out.println("  Threads did not start in time — aborting");
                return;
            }

            // Release both threads simultaneously so their FK checks race
            proceed.countDown();

            thread1.join(15_000);
            thread2.join(15_000);

            summarise(t1Error.get(), t2Error.get());
        } finally {
            emf.close();
        }
    }

    // -------------------------------------------------------------------------
    // SCENARIO 2 — Hibernate 4-compatible, INCREMENT BY 1 → no deadlock
    // -------------------------------------------------------------------------

    /**
     * Same logic, but with {@code allocationSize=1} (Hibernate 4 default).
     *
     * <p>With {@code INCREMENT BY 1}, consecutive nextval() calls return IDs 1 and 2.
     * Because the IDs are adjacent the application puts both accounts in the
     * <em>same</em> transaction, so there is no cross-transaction FK reference
     * and therefore no deadlock.
     *
     * <p>Note: even if the accounts were in separate transactions the window for a
     * deadlock would be far smaller because shorter, non-batched transactions hold
     * row locks for less time.
     */
    private static void demonstrateNoDeadlock() throws Exception {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hibernate4-compat");

        try {
            long accountIdT1 = fetchNextSequenceValue(emf, "account_h4_seq");
            long accountIdT2 = fetchNextSequenceValue(emf, "account_h4_seq");

            log.info("[pre-fetch H4] Thread-1 will use Account id={}", accountIdT1);
            log.info("[pre-fetch H4] Thread-2 will use Account id={}", accountIdT2);
            log.info("[pre-fetch H4] ID gap = {} → {} TX",
                    accountIdT2 - accountIdT1,
                    accountIdT2 - accountIdT1 == 1 ? "SAME" : "SEPARATE");

            /*
             * With INCREMENT BY 1, accountIdT1=1 and accountIdT2=2.
             * The application logic (idB == idA + 1) recognises them as a pair
             * for the SAME transaction — no concurrency, no deadlock.
             */
            EntityManager em = emf.createEntityManager();
            try {
                em.getTransaction().begin();

                AccountH4 accountA = new AccountH4("Settlement-Account-A");
                AccountH4 accountB = new AccountH4("Settlement-Account-B");
                em.persist(accountA);
                em.persist(accountB);

                // Both accounts exist in the same TX; FK checks succeed immediately
                // (no wait on another transaction's uncommitted row)
                log.info("[H4] Both accounts persisted in the same transaction — no deadlock possible");

                em.getTransaction().commit();
                log.info("[H4] Committed successfully");
            } finally {
                em.close();
            }
        } finally {
            emf.close();
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Calls {@code SELECT nextval(sequenceName)} outside any application transaction.
     * Simulates the legacy pattern of manually pre-fetching an ID from a
     * JPA-managed sequence before (or during) a large transaction.
     *
     * <p><strong>Hibernate 5 effect:</strong> because the database sequence has
     * {@code INCREMENT BY allocationSize (50)}, each call here advances the
     * sequence by 50, consuming one entire Hibernate in-memory batch.
     */
    private static long fetchNextSequenceValue(EntityManagerFactory emf, String sequenceName) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Long value = (Long) em
                    .createNativeQuery("SELECT nextval('" + sequenceName + "')")
                    .getSingleResult();
            em.getTransaction().commit();
            return value;
        } finally {
            em.close();
        }
    }

    private static String rootCause(Exception e) {
        Throwable t = e;
        while (t.getCause() != null) t = t.getCause();
        return t.getClass().getSimpleName() + ": " + t.getMessage();
    }

    private static void summarise(Exception t1Error, Exception t2Error) {
        System.out.println();
        if (t1Error != null || t2Error != null) {
            System.out.println("  RESULT: DEADLOCK DETECTED");
            System.out.println("  PostgreSQL rolled back one of the transactions to break the cycle.");
            System.out.println("  Root cause in victim TX: " +
                    rootCause(t1Error != null ? t1Error : t2Error));
        } else {
            System.out.println("  RESULT: both transactions committed — no deadlock");
        }
        System.out.println();
    }
}

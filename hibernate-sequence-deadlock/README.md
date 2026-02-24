# Reproducer: Hibernate 4 → 5 Deadlock via Manual Sequence Increment

A self-contained Maven project that reproduces the deadlock introduced when
upgrading from Hibernate 4 to Hibernate 5 in a system that called
`SELECT nextval(…)` manually inside (or just before) a large transaction.

---

## The core change that broke everything

| | Hibernate 4 | Hibernate 5 |
|---|---|---|
| Default `allocationSize` | 1 | **50** |
| DB sequence DDL | `INCREMENT BY 1` | **`INCREMENT BY 50`** |
| Manual `nextval()` advances counter by | **1** | **50** |
| Consecutive `nextval()` returns | 1, 2, 3, 4 … | 50, 100, 150, 200 … |

Hibernate 5 introduced the **pooled optimizer**: instead of hitting the
database for every ID, it reserves a batch of 50 IDs in memory with a single
`nextval()` call. To make that work, the database sequence must have
`INCREMENT BY 50`. The side-effect is that any manual `nextval()` call outside
Hibernate now jumps the counter by 50, consuming an entire batch.

---

## The deadlock mechanism (step by step)

The legacy codebase pre-fetched account IDs with raw SQL before opening the
transaction — a common Hibernate 4 pattern:

```java
// Pre-fetch IDs for a "settlement pair"
long idA = em.createNativeQuery("SELECT nextval('account_seq')").getSingleResult();
long idB = em.createNativeQuery("SELECT nextval('account_seq')").getSingleResult();

// Create both accounts and a mutual transfer in one (large) transaction
Account a = new Account(idA, "Bank A");
Account b = new Account(idB, "Bank B");
em.persist(a); em.persist(b);
em.persist(new Transfer(a, b, amount));
em.persist(new Transfer(b, a, amount));
```

### With Hibernate 4 (INCREMENT BY 1) — no deadlock

```
Thread-1: nextval() → 1,  nextval() → 2   idA=1, idB=2   (adjacent)
Thread-2: nextval() → 3,  nextval() → 4   idA=3, idB=4   (adjacent)

Thread-1 TX: INSERT Account(1), INSERT Account(2), INSERT Transfer(1→2)
Thread-2 TX: INSERT Account(3), INSERT Account(4), INSERT Transfer(3→4)

FK checks reference rows in the SAME transaction → no cross-TX lock wait.
```

Because the IDs were adjacent (gap = 1) the application correctly kept both
accounts in the **same transaction**. FK checks never had to wait for a row
held by another transaction.

### With Hibernate 5 (INCREMENT BY 50) — DEADLOCK

```
Thread-1: nextval() → 50,  nextval() → 100   idA=50,  idB=100   (50 apart)
Thread-2: nextval() → 150, nextval() → 200   idA=150, idB=200   (50 apart)
```

The application's "adjacent ID → same batch" heuristic no longer applies.
The system now runs the two accounts in **concurrent transactions**, producing
cross-transaction FK references:

```
Thread-1 TX:
  INSERT account(id=50)             → EXCLUSIVE row lock on account(50)
  INSERT transfer(from=50, to=100)  → FK check: SELECT … FROM account WHERE id=100 FOR SHARE
                                       account(100) is held exclusively by Thread-2 → BLOCKED

Thread-2 TX:
  INSERT account(id=100)            → EXCLUSIVE row lock on account(100)
  INSERT transfer(from=100, to=50)  → FK check: SELECT … FROM account WHERE id=50  FOR SHARE
                                       account(50)  is held exclusively by Thread-1 → BLOCKED

Thread-1 waits for Thread-2.
Thread-2 waits for Thread-1.
PostgreSQL detects the cycle → rolls back one transaction → org.postgresql.util.PSQLException: ERROR: deadlock detected
```

### Why PostgreSQL blocks on the FK check

When you INSERT a child row that references a parent being inserted
(uncommitted) by another transaction, PostgreSQL's RI trigger issues:

```sql
SELECT 1 FROM account WHERE id = $1 FOR SHARE
```

A `FOR SHARE` lock is incompatible with the `FOR UPDATE` / exclusive lock the
inserting transaction holds on that row. So the FK check **waits** until the
other transaction commits or rolls back. When both transactions are waiting for
each other, PostgreSQL's deadlock detector fires (default timeout: 1 s).

---

## Running the reproducer

```bash
# 1. Start PostgreSQL
docker compose up -d

# 2. Run the demo (drops and recreates the schema automatically)
mvn compile exec:java -Dexec.mainClass=com.pivovarit.deadlock.DeadlockReproducer
```

Expected output:
```
--- SCENARIO 1: Hibernate 5 defaults (allocationSize=50, INCREMENT BY 50) ---
    Expected: DEADLOCK

[pre-fetch] Thread-1 will use Account id=50
[pre-fetch] Thread-2 will use Account id=100
[pre-fetch] ID gap = 50 (50 apart → concurrent TXs → DEADLOCK)
[T1] Inserted Account{id=50, name='Settlement-Account-T1'} — holding exclusive row lock
[T2] Inserted Account{id=100, name='Settlement-Account-T2'} — holding exclusive row lock
[T2] Transaction rolled back: PSQLException: ERROR: deadlock detected
  Detail: Process … waits for ShareLock on transaction …; blocked by process …
  …

  RESULT: DEADLOCK DETECTED
  PostgreSQL rolled back one of the transactions to break the cycle.

--- SCENARIO 2: Hibernate 4-compatible (allocationSize=1, INCREMENT BY 1) ---
    Expected: both transactions commit successfully

[pre-fetch H4] Thread-1 will use Account id=1
[pre-fetch H4] Thread-2 will use Account id=2
[pre-fetch H4] ID gap = 1 → SAME TX
[H4] Both accounts persisted in the same transaction — no deadlock possible
[H4] Committed successfully

  RESULT: both transactions committed — no deadlock
```

---

## Fixes

### Option A — set `allocationSize = 1` (safe, slow)

```java
@SequenceGenerator(name = "account_gen", sequenceName = "account_seq",
                   allocationSize = 1)
```

Reverts to Hibernate 4 behaviour. Each `nextval()` advances by 1 so manual
calls don't consume entire batches.  The trade-off is one DB round-trip per
INSERT instead of one per 50.

### Option B — remove manual `nextval()` calls (recommended)

Let Hibernate manage ID assignment exclusively.  Never call `nextval()` on a
Hibernate-managed sequence from application or legacy JDBC code.

### Option C — use a separate sequence for manual IDs

If you genuinely need to pre-generate IDs outside Hibernate, use a
**dedicated sequence** that Hibernate doesn't know about:

```sql
CREATE SEQUENCE audit_seq INCREMENT BY 1;
```

Keep `account_seq` (with `INCREMENT BY 50`) exclusively for Hibernate.

---

## Project structure

```
src/main/java/com/pivovarit/deadlock/
├── entity/
│   ├── Account.java          — Hibernate 5 entity (allocationSize=50)
│   ├── AccountH4.java        — Hibernate 4-compatible entity (allocationSize=1)
│   └── Transfer.java         — child entity with two FK columns
└── DeadlockReproducer.java   — main class, full scenario walkthrough
```

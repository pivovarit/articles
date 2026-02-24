package com.pivovarit.deadlock.entity;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * Inter-account transfer.  The two FK columns ({@code from_account_id},
 * {@code to_account_id}) are the crux of the deadlock:
 *
 * <p>PostgreSQL enforces FK constraints at INSERT time by acquiring a
 * {@code ROW SHARE} lock on the referenced parent row.  If that parent row is
 * being inserted by a <em>concurrent, uncommitted</em> transaction, the FK
 * check blocks until the concurrent transaction commits or rolls back.
 *
 * <p>When two transactions each insert a Transfer that cross-references the
 * other's Account row, they block each other → classic AB-BA deadlock.
 */
@Entity
@Table(name = "transfer")
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transfer_gen")
    @SequenceGenerator(
            name          = "transfer_gen",
            sequenceName  = "transfer_seq",
            allocationSize = 50
    )
    private Long id;

    /** The account sending funds — FK that triggers a ROW SHARE lock at INSERT. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "from_account_id", nullable = false)
    private Account fromAccount;

    /** The account receiving funds — FK that triggers a ROW SHARE lock at INSERT. */
    @ManyToOne(optional = false)
    @JoinColumn(name = "to_account_id", nullable = false)
    private Account toAccount;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    protected Transfer() {}

    public Transfer(Account fromAccount, Account toAccount, BigDecimal amount) {
        this.fromAccount = fromAccount;
        this.toAccount   = toAccount;
        this.amount      = amount;
    }

    public Long getId() { return id; }

    @Override
    public String toString() {
        return "Transfer{id=" + id +
               ", from=" + fromAccount.getId() +
               ", to="   + toAccount.getId()   +
               ", amount=" + amount + "}";
    }
}

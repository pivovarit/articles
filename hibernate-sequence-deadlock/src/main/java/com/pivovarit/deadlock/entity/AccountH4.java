package com.pivovarit.deadlock.entity;

import javax.persistence.*;

/**
 * Settlement account — Hibernate 4-compatible variant.
 *
 * <p>The only difference from {@link Account} is {@code allocationSize = 1},
 * which mirrors Hibernate 4 behaviour: the database sequence has
 * {@code INCREMENT BY 1}, so every {@code nextval()} call (including manual
 * ones) advances the counter by exactly 1.
 *
 * <p>With consecutive IDs (1, 2, 3 …) both accounts in a settlement pair
 * receive adjacent IDs and are therefore processed inside the <em>same</em>
 * transaction, eliminating the cross-transaction FK lock conflict.
 */
@Entity
@Table(name = "account_h4")
public class AccountH4 {

    /**
     * allocationSize = 1 → DDL:  CREATE SEQUENCE account_h4_seq START 1 INCREMENT 1
     *
     * A manual SELECT nextval('account_h4_seq') advances the counter by 1 only,
     * so two sequential nextval() calls return IDs that differ by 1 (e.g. 1 and 2).
     * Both accounts land in the same transaction — no deadlock.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_h4_gen")
    @SequenceGenerator(
            name          = "account_h4_gen",
            sequenceName  = "account_h4_seq",
            allocationSize = 1           // Hibernate 4 default — matches INCREMENT BY 1
    )
    private Long id;

    @Column(nullable = false)
    private String name;

    protected AccountH4() {}

    public AccountH4(String name) {
        this.name = name;
    }

    public Long getId()   { return id; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return "AccountH4{id=" + id + ", name='" + name + "'}";
    }
}

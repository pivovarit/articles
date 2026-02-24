package com.pivovarit.deadlock.entity;

import javax.persistence.*;

/**
 * Settlement account — used with the Hibernate 5 persistence unit.
 *
 * <p>Key detail: {@code allocationSize = 50} (the Hibernate 5 default).
 * Hibernate will create the backing sequence with {@code INCREMENT BY 50}.
 * One manual {@code SELECT nextval('account_seq')} therefore advances the
 * database sequence by 50, consuming an entire in-memory batch at once.
 */
@Entity
@Table(name = "account")
public class Account {

    /**
     * Hibernate 5 default: allocationSize = 50.
     *
     * The generated DDL will be:
     *   CREATE SEQUENCE account_seq START 1 INCREMENT 50
     *
     * Consequence: calling nextval() outside Hibernate jumps the counter by 50
     * instead of 1, so IDs assigned to two concurrent transactions that each
     * call nextval() once are 50 apart (e.g. 50 and 100) rather than adjacent.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "account_gen")
    @SequenceGenerator(
            name          = "account_gen",
            sequenceName  = "account_seq",
            allocationSize = 50          // Hibernate 5 default — matches INCREMENT BY 50
    )
    private Long id;

    @Column(nullable = false)
    private String name;

    protected Account() {}

    public Account(String name) {
        this.name = name;
    }

    public Long getId()   { return id; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return "Account{id=" + id + ", name='" + name + "'}";
    }
}

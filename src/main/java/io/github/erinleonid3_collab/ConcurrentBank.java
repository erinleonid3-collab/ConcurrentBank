package io.github.erinleonid3_collab;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ConcurrentBank {
    private final Map<Integer, BankAccount> accounts = new ConcurrentHashMap<>();
    private final AtomicInteger nextAccountId = new AtomicInteger(1);

    public BankAccount createAccount(long initialBalance) {
        int id = nextAccountId.getAndIncrement();
        BankAccount account = new BankAccount(id, initialBalance);
        accounts.put(id, account);
        return account;
    }

    public boolean transfer(BankAccount from, BankAccount to, long amount) {
        if (from == null || to == null) {
            throw new IllegalArgumentException("Accounts must not be null");
        }
        if (from == to) {
            throw new IllegalArgumentException("Cannot transfer to the same account");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }

        BankAccount first = from.getAccountNumber() < to.getAccountNumber() ? from : to;
        BankAccount second = from.getAccountNumber() < to.getAccountNumber() ? to : from;

        first.getLock().lock();
        try {
            second.getLock().lock();
            try {
                if (!from.debit(amount)) {
                    return false;
                }
                to.credit(amount);
                return true;
            } finally {
                second.getLock().unlock();
            }
        } finally {
            first.getLock().unlock();
        }
    }

    public long getTotalBalance() {
        BankAccount[] snapshot = accounts.values().stream()
                .sorted((a, b) -> Integer.compare(a.getAccountNumber(), b.getAccountNumber()))
                .toArray(BankAccount[]::new);

        for (BankAccount acc : snapshot) {
            acc.getLock().lock();
        }
        try {
            long total = 0;
            for (BankAccount acc : snapshot) {
                total += acc.getBalance();
            }
            return total;
        } finally {
            for (int i = snapshot.length - 1; i >= 0; i--) {
                snapshot[i].getLock().unlock();
            }
        }
    }
}
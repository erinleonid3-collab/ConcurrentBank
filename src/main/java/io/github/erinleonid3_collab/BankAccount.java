package io.github.erinleonid3_collab;

import java.util.concurrent.locks.ReentrantLock;

public class BankAccount {
    private final int accountNumber;
    private long balance;
    private final ReentrantLock lock = new ReentrantLock();

    public BankAccount(int accountNumber, long initialBalance) {
        this.accountNumber = accountNumber;
        this.balance = initialBalance;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public long getBalance() {
        lock.lock();
        try {
            return balance;
        } finally {
            lock.unlock();
        }
    }

    public void deposit(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        lock.lock();
        try {
            balance += amount;
        } finally {
            lock.unlock();
        }
    }

    public boolean withdraw(long amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        lock.lock();
        try {
            if (balance < amount) {
                return false;
            }
            balance -= amount;
            return true;
        } finally {
            lock.unlock();
        }
    }

    ReentrantLock getLock() {
        return lock;
    }

    void credit(long amount) {
        balance += amount;
    }

    boolean debit(long amount) {
        if (balance < amount) {
            return false;
        }
        balance -= amount;
        return true;
    }
}

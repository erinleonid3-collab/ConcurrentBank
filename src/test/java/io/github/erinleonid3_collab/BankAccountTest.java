package io.github.erinleonid3_collab;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class BankAccountTest {

    @Test
    void newAccountHasInitialBalance() {
        BankAccount acc = new BankAccount(1, 1000);
        assertEquals(1000, acc.getBalance());
        assertEquals(1, acc.getAccountNumber());
    }

    @Test
    void depositIncreasesBalance() {
        BankAccount acc = new BankAccount(1, 100);
        acc.deposit(50);
        assertEquals(150, acc.getBalance());
    }

    @Test
    void withdrawSucceedsWhenEnoughFunds() {
        BankAccount acc = new BankAccount(1, 100);
        assertTrue(acc.withdraw(40));
        assertEquals(60, acc.getBalance());
    }

    @Test
    void withdrawFailsWhenInsufficientFunds() {
        BankAccount acc = new BankAccount(1, 100);
        assertFalse(acc.withdraw(200));
        assertEquals(100, acc.getBalance());
    }

    @Test
    void withdrawExactBalance() {
        BankAccount acc = new BankAccount(1, 100);
        assertTrue(acc.withdraw(100));
        assertEquals(0, acc.getBalance());
    }

    @Test
    void depositRejectsNonPositiveAmount() {
        BankAccount acc = new BankAccount(1, 100);
        assertThrows(IllegalArgumentException.class, () -> acc.deposit(0));
        assertThrows(IllegalArgumentException.class, () -> acc.deposit(-10));
    }

    @Test
    void withdrawRejectsNonPositiveAmount() {
        BankAccount acc = new BankAccount(1, 100);
        assertThrows(IllegalArgumentException.class, () -> acc.withdraw(0));
        assertThrows(IllegalArgumentException.class, () -> acc.withdraw(-10));
    }
}
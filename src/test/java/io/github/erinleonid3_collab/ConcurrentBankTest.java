package io.github.erinleonid3_collab;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class ConcurrentBankTest {

    @Test
    void transferMovesMoney() {
        ConcurrentBank bank = new ConcurrentBank();
        BankAccount a = bank.createAccount(1000);
        BankAccount b = bank.createAccount(500);

        assertTrue(bank.transfer(a, b, 200));
        assertEquals(800, a.getBalance());
        assertEquals(700, b.getBalance());
    }

    @Test
    void transferRollsBackOnInsufficientFunds() {
        ConcurrentBank bank = new ConcurrentBank();
        BankAccount a = bank.createAccount(100);
        BankAccount b = bank.createAccount(50);

        assertFalse(bank.transfer(a, b, 200));
        assertEquals(100, a.getBalance());
        assertEquals(50, b.getBalance());
    }

    @Test
    void totalBalanceSumsAll() {
        ConcurrentBank bank = new ConcurrentBank();
        bank.createAccount(1000);
        bank.createAccount(500);
        assertEquals(1500, bank.getTotalBalance());
    }

    @Test
    void concurrentDepositsDoNotLoseUpdates() throws Exception {
        BankAccount acc = new BankAccount(1, 0);
        int threads = 10, perThread = 1000;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    for (int j = 0; j < perThread; j++) acc.deposit(1);
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                finally { done.countDown(); }
            });
        }
        start.countDown();
        assertTrue(done.await(10, TimeUnit.SECONDS));
        pool.shutdown();

        assertEquals(10_000, acc.getBalance());
    }

    @Test
    void concurrentTransfersPreserveTotal() throws Exception {
        ConcurrentBank bank = new ConcurrentBank();
        List<BankAccount> accs = new ArrayList<>();
        for (int i = 0; i < 5; i++) accs.add(bank.createAccount(10_000));
        long expected = 50_000;

        int threads = 10, ops = 1000;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                try {
                    start.await();
                    ThreadLocalRandom rnd = ThreadLocalRandom.current();
                    for (int i = 0; i < ops; i++) {
                        int from = rnd.nextInt(5), to = rnd.nextInt(5);
                        if (from != to) bank.transfer(accs.get(from), accs.get(to), rnd.nextInt(1, 50));
                    }
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                finally { done.countDown(); }
            });
        }
        start.countDown();
        assertTrue(done.await(30, TimeUnit.SECONDS));
        pool.shutdown();

        assertEquals(expected, bank.getTotalBalance());
    }

    @Test
    void bidirectionalTransfersDoNotDeadlock() throws Exception {
        ConcurrentBank bank = new ConcurrentBank();
        BankAccount a = bank.createAccount(1_000_000);
        BankAccount b = bank.createAccount(1_000_000);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch done = new CountDownLatch(2);
        pool.submit(() -> { for (int i = 0; i < 10_000; i++) bank.transfer(a, b, 1); done.countDown(); });
        pool.submit(() -> { for (int i = 0; i < 10_000; i++) bank.transfer(b, a, 1); done.countDown(); });

        assertTrue(done.await(10, TimeUnit.SECONDS), "deadlock!");
        pool.shutdownNow();
    }
}

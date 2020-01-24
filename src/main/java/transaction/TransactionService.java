package transaction;

import account.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TransactionService {

    private static final Logger LOG = LoggerFactory.getLogger(TransactionService.class);
    private static final int LOW_TIME_LIMIT_BORDER_MS = 1000;
    private static final int HIGH_TIME_LIMIT_BORDER_MS = 2000;
    private static SecureRandom secureRandom = new SecureRandom();
    private final ScheduledExecutorService executorService;
    private ScheduledFuture firstTransactionScheduleFuture;
    private ScheduledFuture secondTransactionScheduleFuture;
    private AtomicInteger transactionsCountLimit;

    public TransactionService(ScheduledExecutorService executorService, int transactionsCountLimit) {
        this.executorService = executorService;
        this.transactionsCountLimit = new AtomicInteger(transactionsCountLimit);
    }

    public void startTransactions(Account firstAccount, Account secondAccount) {
        Transaction firstTransaction = new Transaction(firstAccount, secondAccount, firstTransactionScheduleFuture);
        firstTransactionScheduleFuture =
                executorService.scheduleWithFixedDelay(
                        firstTransaction, createTimeDelay(), createTimeDelay(), TimeUnit.MILLISECONDS);

        Transaction secondTransaction = new Transaction(secondAccount, firstAccount, secondTransactionScheduleFuture);
        secondTransactionScheduleFuture =
                executorService.scheduleWithFixedDelay(
                        secondTransaction, createTimeDelay(), createTimeDelay(), TimeUnit.MILLISECONDS);
    }

    private long createTimeDelay() {
        return LOW_TIME_LIMIT_BORDER_MS +
                secureRandom.nextInt((HIGH_TIME_LIMIT_BORDER_MS - LOW_TIME_LIMIT_BORDER_MS + 1));
    }

    private void makeTransaction(Account debitAccount, Account creditAccount) {
        int debitValue = secureRandom.nextInt(debitAccount.getBalance() + 1);
        debitValue = debitAccount.debit(debitValue);
        creditAccount.credit(debitValue);
    }

    private void changeScheduleTime(ScheduledFuture future, Runnable runnable) {
        if (future != null) {
            future.cancel(true);
        }
        executorService.scheduleWithFixedDelay(runnable, createTimeDelay(), createTimeDelay(), TimeUnit.MILLISECONDS);
    }

    private class Transaction implements Runnable {

        private final Account debitAccount;
        private final Account creditAccount;
        private ScheduledFuture transactionScheduleFuture;

        private Transaction(Account debitAccount, Account creditAccount, ScheduledFuture transactionScheduleFuture) {
            this.debitAccount = debitAccount;
            this.creditAccount = creditAccount;
            this.transactionScheduleFuture = transactionScheduleFuture;
        }

        @Override
        public void run() {
            if (transactionsCountLimit.get() != 0) {
                makeTransaction(debitAccount, creditAccount);
                transactionsCountLimit.decrementAndGet();
                LOG.info("Транзакций осталось: " + transactionsCountLimit.get());
                changeScheduleTime(transactionScheduleFuture, this);
            } else {
                executorService.shutdown();
            }
        }
    }
}

import account.Account;
import transaction.TransactionService;

import java.util.concurrent.*;

public class Main {

    private static final int THREAD_COUNT = 2;
    private static final int TRANSACTIONS_COUNT = 30;
    private static final int MONEY = 10000;

    public static void main(String[] args) {
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(THREAD_COUNT);
        TransactionService transactionService = new TransactionService(executorService, TRANSACTIONS_COUNT);

        Account bobAccount = new Account(MONEY);
        Account samAccount = new Account(MONEY);

        transactionService.startTransactions(bobAccount, samAccount);
    }
}

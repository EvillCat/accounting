package account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Account {

    private static final Logger LOG = LoggerFactory.getLogger(Account.class);

    private static final String ID_CREATED_MESSAGE =
            "Создан аккаунт с id: {}. Начальный баланс средств: {}.";
    private static final String DEBIT_OPERATION_MESSAGE =
            "Попытка снять {} со счета {}. Снято {}. Баланс: {}";
    private static final String DEBIT_OPERATION_NOT_ENOUGH_MESSAGE =
            "Попытка снять {} со счета {}. Недостаточно средств. Средств на счете {}. Снято {}.";
    private static final String CREDIT_OPERATION =
            "Счет {} пополнен на сумму {}. Баланс: {}.";

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_ACCOUNT_ID_VALUE = 1000;

    private static ArrayList<Integer> idList = new ArrayList<>();
    private final int id;
    private AtomicInteger money;

    public Account(int money) {
        id = generateId();
        this.money = new AtomicInteger(money);
        LOG.info(ID_CREATED_MESSAGE, id, money);
    }

    private int generateId() {
        int id = RANDOM.nextInt(MAX_ACCOUNT_ID_VALUE + 1);
        if (idList.size() == 0) {
            idList.add(id);
            return id;
        }

        while (idList.contains(id)) {
            id = RANDOM.nextInt(MAX_ACCOUNT_ID_VALUE + 1);
        }
        idList.add(id);
        return id;
    }

    public int debit(int value) {
        if (money.get() >= value) {
            money.set(money.get() - value);
            LOG.info(DEBIT_OPERATION_MESSAGE, value, id, value, money.get());
            return value;
        } else {
            int balance = money.get();
            LOG.info(DEBIT_OPERATION_NOT_ENOUGH_MESSAGE, value, id, balance, balance);
            value = balance;
            money.set(0);
            return value;
        }
    }

    public void credit(int value) {
        money.set(money.get() + value);
        LOG.info(CREDIT_OPERATION, id, value, money.get());
    }

    public int getBalance() {
        return money.get();
    }
}

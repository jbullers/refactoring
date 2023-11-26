package di;

import java.math.BigDecimal;

/**
 * An individual user's account.
 */
public interface Account {

    String username();

    void deposit(BigDecimal amount);

    void withdraw(BigDecimal amount);

    BigDecimal balance();
}

package ru.mentee.power.fintrack.service;


import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static ru.mentee.power.fintrack.cli.Transaction.Category.GOODS;
import static ru.mentee.power.fintrack.cli.Transaction.Category.SALARY;
import static ru.mentee.power.fintrack.cli.Transaction.Category.TRANSPORT;
import static ru.mentee.power.fintrack.cli.Transaction.TransactionType.EXPENSE;
import static ru.mentee.power.fintrack.cli.Transaction.TransactionType.INCOME;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.mentee.power.fintrack.cli.Transaction;

class FinanceServiceTest {

  private TransactionService transactionService;
  private Transaction testIncome;
  private Transaction testExpense;

  @BeforeEach
  void setUp() {
    transactionService = new TransactionService();

    testIncome = transactionService.addTransaction(
        INCOME,
        new BigDecimal("1000.00"),
        SALARY,
        "Monthly salary"
    );

    testExpense = transactionService.addTransaction(
        EXPENSE,
        new BigDecimal("200.50"),
        GOODS,
        "Groceries"
    );
  }

  @Test
  @DisplayName("Добавление валидной транзакции")
  void addTransaction_ValidData_ReturnsCreatedTransaction() {
    Transaction transaction = transactionService.addTransaction(
        EXPENSE,
        new BigDecimal("150.00"),
        TRANSPORT,
        "Taxi"
    );

    assertThat(transaction)
        .isNotNull()
        .satisfies(t -> {
          assertThat(t.getId()).isPositive();
          assertThat(t.getTransactionType()).isEqualTo(EXPENSE);
          assertThat(t.getSum()).isEqualByComparingTo("150.00");
          assertThat(t.getCategory()).isEqualTo(TRANSPORT);
        });
  }

  @Test
  @DisplayName("Добавление транзакции с нулевой суммой вызывает исключение")
  void addTransaction_ZeroAmount_ThrowsException() {
    assertThatThrownBy(() -> transactionService.addTransaction(
        INCOME,
        BigDecimal.ZERO,
        SALARY,
        "Invalid"
    ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Сумма должна быть положительной");
  }

  @Test
  @DisplayName("Добавление транзакции с отрицательной суммой вызывает исключение")
  void addTransaction_NegativeAmount_ThrowsException() {
    assertThatThrownBy(() -> transactionService.addTransaction(
        EXPENSE,
        new BigDecimal("-100.00"),
        GOODS,
        "Invalid"
    ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Сумма должна быть положительной");
  }

  @Test
  @DisplayName("Получение всех транзакций")
  void getAllTransactions_ReturnsAllTransactionsSorted() {
    List<Transaction> transactions = transactionService.getAllTransactions();

    assertThat(transactions)
        .hasSize(2)
        .extracting(Transaction::getId)
        .containsExactly( testIncome.getId(),testExpense.getId());
  }

  @Test
  @DisplayName("Получение транзакций по типу")
  void getTransactionsByType_ReturnsFilteredResults() {
    List<Transaction> incomes = transactionService.getTransactionsByType(INCOME);
    List<Transaction> expenses = transactionService.getTransactionsByType(EXPENSE);

    assertThat(incomes)
        .hasSize(1)
        .allMatch(t -> t.getTransactionType() == INCOME);

    assertThat(expenses)
        .hasSize(1)
        .allMatch(t -> t.getTransactionType() == EXPENSE);
  }

  @Test
  @DisplayName("Получение транзакций по несуществующему типу возвращает пустой список")
  void getTransactionsByType_NonExistingType_ReturnsEmpty() {
    transactionService = new TransactionService(); // Reset service

    assertThat(transactionService.getTransactionsByType(INCOME))
        .isEmpty();
  }

  @Test
  @DisplayName("Получение транзакций по категории")
  void getTransactionsByCategory_ReturnsFilteredResults() {
    List<Transaction> goodsTransactions = transactionService.getTransactionsByCategory(GOODS);

    assertThat(goodsTransactions)
        .hasSize(1)
        .allMatch(t -> t.getCategory() == GOODS);
  }

  @Test
  @DisplayName("Расчет баланса с несколькими транзакциями")
  void calculateBalance_WithTransactions_ReturnsCorrectBalance() {
    BigDecimal balance = transactionService.calculateBalance();

    assertThat(balance)
        .isEqualByComparingTo("799.50");
  }

  @Test
  @DisplayName("Расчет баланса без транзакций возвращает ноль")
  void calculateBalance_NoTransactions_ReturnsZero() {
    transactionService = new TransactionService();

    assertThat(transactionService.calculateBalance())
        .isEqualByComparingTo(BigDecimal.ZERO);
  }

  @Test
  @DisplayName("Получение итогов по категориям расходов")
  void getExpensesByCategory_ReturnsCorrectSummary() {
    transactionService.addTransaction(
        EXPENSE,
        new BigDecimal("300.00"),
        GOODS,
        "Electronics"
    );

    Map<Transaction.Category, BigDecimal> summary = transactionService.getExpensesByCategory();

    assertThat(summary)
        .hasSize(1)
        .containsEntry(GOODS, new BigDecimal("500.50"))
        .doesNotContainKeys(SALARY, TRANSPORT);
  }

  @Test
  @DisplayName("Удаление существующей транзакции")
  void deleteTransaction_ExistingId_ReturnsTrueAndRemovesTransaction() {
    boolean result = transactionService.deleteTransaction(testIncome.getId());

    assertThat(result).isTrue();
    assertThat(transactionService.getAllTransactions())
        .hasSize(1)
        .noneMatch(t -> t.getId() == testIncome.getId());
  }

  @Test
  @DisplayName("Удаление несуществующей транзакции возвращает false")
  void deleteTransaction_NonExistingId_ReturnsFalse() {
    boolean result = transactionService.deleteTransaction(999);

    assertThat(result).isFalse();
    assertThat(transactionService.getAllTransactions()).hasSize(2);
  }

  @Test
  @DisplayName("Добавление транзакции с null-категорией вызывает исключение")
  void addTransaction_NullCategory_ThrowsException() {
    assertThatThrownBy(() -> transactionService.addTransaction(
        INCOME,
        new BigDecimal("100.00"),
        null,
        "Invalid"
    ))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Категория обязательна");
  }

  @Test
  @DisplayName("Добавление транзакции с пустым описанием разрешено")
  void addTransaction_EmptyDescription_Allowed() {
    Transaction transaction = transactionService.addTransaction(
        INCOME,
        new BigDecimal("500.00"),
        SALARY,
        ""
    );

    assertThat(transaction.getDescription()).isEmpty();
  }

  @Test
  @DisplayName("Граничный случай: минимально допустимая сумма (0.01)")
  void addTransaction_MinimumAmount_Allowed() {
    Transaction transaction = transactionService.addTransaction(
        EXPENSE,
        new BigDecimal("0.01"),
        TRANSPORT,
        "Minimum amount"
    );

    assertThat(transaction.getSum()).isEqualByComparingTo("0.01");
  }

  @Test
  @DisplayName("Проверка порядка сортировки по дате")
  void getAllTransactions_ReturnsTransactionsInCorrectOrder() {
    Transaction oldest = transactionService.addTransaction(
        INCOME,
        new BigDecimal("500.00"),
        SALARY,
        "Oldest"
    );

    List<Transaction> transactions = transactionService.getAllTransactions();

    assertThat(transactions)
        .extracting(Transaction::getId)
        .startsWith(testIncome.getId(), testExpense.getId(), oldest.getId());
  }
}
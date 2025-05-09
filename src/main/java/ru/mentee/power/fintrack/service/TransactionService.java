package ru.mentee.power.fintrack.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import ru.mentee.power.fintrack.cli.Transaction;
import ru.mentee.power.fintrack.cli.Transaction.Category;

/**
 * Описываем основное поведение для объектов класса Transaction.
 */
public class TransactionService {

  private List<Transaction> transactions = new ArrayList<>();
  private int lastId = 0;

  /**
   * Метод для добавления новой транзакции в transactions с валидацией ввода.
   */
  public Transaction addTransaction(Transaction.TransactionType type,
      BigDecimal sum, Transaction.Category category, String description) {

    if (sum.compareTo(BigDecimal.ZERO) <= 0) {
      throw new IllegalArgumentException("Сумма должна быть положительной");
    }
    if (category == null) {
      throw new IllegalArgumentException("Категория обязательна");
    }

    int newId = lastId++;
    LocalDate currentDate = LocalDate.now();

    Transaction transaction = new Transaction(newId, description, sum, currentDate, category,
        type);

    transactions.add(transaction);
    return transaction;
  }

  /**
   * Возвращаем все транзакции по дате от самой новой к самой старой.
   */
  public List<Transaction> getAllTransactions() {
    return transactions.stream()
        .sorted(Comparator.comparing(Transaction::getDate).reversed())
        .collect(Collectors.toList());
  }

  /**
   * Возвращаем транзакции по типу и фильтруем по дате от самой новой к самой старой.
   */
  public List<Transaction> getTransactionsByType(Transaction.TransactionType type) {
    return transactions.stream()
        .filter(t -> t.getTransactionType() == type)
        .sorted(Comparator.comparing(Transaction::getDate).reversed())
        .collect(Collectors.toList());
  }

  /**
   * Возвращаем транзакции по типу и фильтруем по дате от самой новой к самой старой.
   */
  public List<Transaction> getTransactionsByCategory(Transaction.Category category) {
    return transactions.stream()
        .filter(t -> t.getCategory() == category)
        .sorted(Comparator.comparing(Transaction::getDate).reversed())
        .collect(Collectors.toList());
  }

  /**
   * Проходим по коллекции находим доходы/расходы суммируем и вычитаем из доходов расходы.
   */
  public BigDecimal calculateBalance() {
    BigDecimal income = transactions.stream()
        .filter(t -> t.getTransactionType() == Transaction.TransactionType.INCOME)
        .map(Transaction::getSum)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal expense = transactions.stream()
        .filter(t -> t.getTransactionType() == Transaction.TransactionType.EXPENSE)
        .map(Transaction::getSum)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    return income.subtract(expense);
  }

  /**
   * Возвращаем транзакции по категории.
   */
  public Map<Category, BigDecimal> getExpensesByCategory() {
    return transactions.stream()
        .filter(t -> t.getTransactionType() == Transaction.TransactionType.EXPENSE)
        .collect(Collectors.groupingBy(
            Transaction::getCategory,
            Collectors.reducing(
                BigDecimal.ZERO,
                Transaction::getSum,
                BigDecimal::add
            )
        ));
  }

  /**
   * Удалить транзакцию по id.
   */
  public boolean deleteTransaction(int id) {
    return transactions.removeIf(t -> t.getId() == id);
  }

}
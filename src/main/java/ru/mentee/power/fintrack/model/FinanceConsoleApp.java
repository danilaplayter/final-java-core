package ru.mentee.power.fintrack.model;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import ru.mentee.power.fintrack.cli.Transaction;
import ru.mentee.power.fintrack.service.TransactionService;

/**
 * Класс для демонстрации работы классов Transaction и TransactionService.
 */
public class FinanceConsoleApp {

  private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private static final String COMMAND_PROMPT = "\n> ";
  private static final String INVALID_COMMAND_MESSAGE = "Неизвестная команда. "
      + "Введите 'help' для списка команд.";

  private final TransactionService transactionService = new TransactionService();
  private final Scanner inputScanner = new Scanner(System.in);

  public static void main(String[] args) {
    new FinanceConsoleApp().startApplication();
  }

  private void startApplication() {
    printWelcomeMessage();
    runCommandLoop();
    shutdownApplication();
  }

  private void runCommandLoop() {
    boolean isRunning = true;

    while (isRunning) {
      System.out.print(COMMAND_PROMPT);
      String userInput = inputScanner.nextLine().trim();

      if (userInput.isEmpty()) {
        continue;
      }

      String[] inputParts = userInput.split("\\s+", 2);
      String mainCommand = inputParts[0].toLowerCase();
      String commandArguments = inputParts.length > 1 ? inputParts[1] : "";

      try {
        isRunning = processUserCommand(mainCommand, commandArguments);
      } catch (IllegalArgumentException e) {
        System.out.println("Ошибка: " + e.getMessage());
      }
    }
  }

  private boolean processUserCommand(String command, String arguments) {
    switch (command) {
      case "add" -> handleAddCommand(arguments);
      case "list" -> handleListCommand(arguments);
      case "balance" -> displayCurrentBalance();
      case "summary" -> displayExpenseSummary();
      case "delete" -> handleDeleteCommand(arguments);
      case "exit" -> {
        return false;
      }
      default -> System.out.println(INVALID_COMMAND_MESSAGE);
    }
    return true;
  }

  private void handleAddCommand(String arguments) {
    String[] addCommandParts = arguments.split("\\s+", 4);

    if (addCommandParts.length < 3) {
      throw new IllegalArgumentException(
          "Формат команды: add [income/expense] [сумма] [категория] [описание]"
      );
    }

    Transaction.TransactionType transactionType = parseTransactionType(addCommandParts[0]);
    BigDecimal amount = parseTransactionAmount(addCommandParts[1]);
    Transaction.Category category = parseTransactionCategory(addCommandParts[2]);
    String description = addCommandParts.length > 3 ? addCommandParts[3] : "";

    transactionService.addTransaction(transactionType, amount, category, description);
    System.out.println("✅ Транзакция успешно добавлена");
  }

  private void handleListCommand(String arguments) {
    String[] listCommandParts = arguments.split("\\s+");
    if (listCommandParts.length < 1) {
      throw new IllegalArgumentException("Укажите тип списка: all, income или expense");
    }

    switch (listCommandParts[0].toLowerCase()) {
      case "all" -> displayTransactions(transactionService.getAllTransactions());
      case "income" -> displayTransactions(transactionService
          .getTransactionsByType(Transaction.TransactionType.INCOME));
      case "expense" -> displayTransactions(transactionService
          .getTransactionsByType(Transaction.TransactionType.EXPENSE));
      default -> throw new IllegalArgumentException("Неподдерживаемый тип списка");
    }
  }

  private void handleDeleteCommand(String arguments) {
    if (arguments.isEmpty()) {
      throw new IllegalArgumentException("Укажите ID транзакции");
    }

    int transactionId = Integer.parseInt(arguments);
    boolean isDeleted = transactionService.deleteTransaction(transactionId);
    System.out.println(isDeleted ? "✅ Транзакция удалена" : "⚠️ Транзакция не найдена");
  }

  private void displayTransactions(List<Transaction> transactions) {
    if (transactions.isEmpty()) {
      System.out.println("\nНет данных для отображения");
      return;
    }

    System.out
        .println("\n┌─────┬────────────┬──────────┬────────────┬──────────┬────────────────┐");
    System.out.println("│ ID │    Дата    │   Тип    │ Категория  │  Сумма   │   Описание     │");
    System.out.println("├─────┼────────────┼──────────┼────────────┼──────────┼────────────────┤");

    for (Transaction transaction : transactions) {
      System.out.printf(
          "│ %-3d │ %-10s │ %-8s │ %-10s │ %8.2f │ %-14s │%n",
          transaction.getId(),
          transaction.getDate().format(DATE_FORMATTER),
          transaction.getTransactionType(),
          transaction.getCategory(),
          transaction.getSum(),
          transaction.getDescription()
      );
    }
    System.out.println("└─────┴────────────┴──────────┴────────────┴──────────┴────────────────┘");
  }

  private void displayCurrentBalance() {
    BigDecimal balance = transactionService.calculateBalance();
    System.out.printf("\nТекущий баланс: %+.2f\n", balance);
  }

  private void displayExpenseSummary() {
    Map<Transaction.Category, BigDecimal> summary = transactionService.getExpensesByCategory();

    if (summary.isEmpty()) {
      System.out.println("\nНет данных о расходах");
      return;
    }

    System.out.println("\nКатегория расходов │ Сумма");
    System.out.println("───────────────────┼──────────");
    summary.forEach((category, amount) ->
        System.out.printf(" %-17s │ %8.2f%n", category, amount)
    );
  }

  private Transaction.TransactionType parseTransactionType(String typeInput) {
    return switch (typeInput.toLowerCase()) {
      case "income" -> Transaction.TransactionType.INCOME;
      case "expense" -> Transaction.TransactionType.EXPENSE;
      default -> throw new IllegalArgumentException("Неподдерживаемый тип транзакции");
    };
  }

  private Transaction.Category parseTransactionCategory(String categoryInput) {
    return Transaction.Category.valueOf(categoryInput.toUpperCase());
  }

  private BigDecimal parseTransactionAmount(String amountInput) {
    try {
      BigDecimal amount = new BigDecimal(amountInput);
      if (amount.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException("Сумма должна быть больше нуля");
      }
      return amount;
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Неверный формат суммы");
    }
  }

  private void printWelcomeMessage() {
    String helpText = """
        ====================================
        🏦 Финансовый менеджер - Версия 1.0
        ====================================
        
        Доступные команды:
        
        📥 Добавление транзакций:
          add income <сумма> <категория> [описание]
          add expense <сумма> <категория> [описание]
        
        📊 Просмотр данных:
          list all      - Все транзакции
          list income   - Доходы
          list expense  - Расходы
          balance       - Текущий баланс
          summary       - Статистика по расходам
        
        🗑 Управление:
          delete <id>   - Удалить транзакцию
          exit          - Выход
        
        📋 Доступные категории: 
          • GOODS (Товары)
          • TRANSPORT (Транспорт)
          • SALARY (Зарплата)
          • ENTERTAINMENT (Развлечения)
        """;
    System.out.println(helpText);
  }

  private void shutdownApplication() {
    inputScanner.close();
    System.out.println("\nРабота приложения завершена. До свидания! 👋");
  }
}
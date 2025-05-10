package ru.mentee.power.fintrack.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Данный класс описывает характеристики и поведение транзакций.
 */
public class Transaction {

  private int id;
  private String description;
  private BigDecimal sum;
  private LocalDate date;
  private Category category;
  private TransactionType transactionType;

  /**
   * Описание основных категорий для трат/расходов.
   */
  public enum Category {
    GOODS, TRANSPORT, SALARY, ENTERTAINMENT
  }

  /**
   * Описание вида транзакции доход/расход.
   */
  public enum TransactionType {
    INCOME, EXPENSE
  }

  /**
   * Конструктор для транзакции по всем полям.
   */
  public Transaction(int id, String description, BigDecimal sum, LocalDate date, Category category,
      TransactionType transactionType) {
    this.id = id;
    this.description = description;
    this.sum = sum;
    this.date = date;
    this.category = category;
    this.transactionType = transactionType;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public BigDecimal getSum() {
    return sum;
  }

  public void setSum(BigDecimal sum) {
    this.sum = sum;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  public Category getCategory() {
    return category;
  }

  public void setCategory(Category category) {
    this.category = category;
  }

  public TransactionType getTransactionType() {
    return transactionType;
  }

  public void setTransactionType(TransactionType transactionType) {
    this.transactionType = transactionType;
  }
}
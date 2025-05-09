# final-java-core
CoinKeeper — это интуитивное приложение для управления личными финансами, разработанное для упрощения контроля над доходами, расходами.

1. Реализованные методы :
   
Добавление дохода	add income <сумма> <категория> [описание]	add income 5000 SALARY Зарплата
Добавление расхода	add expense <сумма> <категория> [описание]	add expense 1500 GOODS Продукты
Просмотр всех транзакций	list all	list all
Просмотр доходов	list income	list income
Просмотр расходов	list expense	list expense
Показать баланс	balance	balance
Сводка по категориям расходов	summary	summary
Удаление транзакции	delete <ID>	delete 3
Выход	exit	exit

3. Тестовая стратегия:

Классы/методы, покрытые тестами:

Класс FinanceService:

addTransaction(): добавление транзакций.
getAllTransactions(): получение всех транзакций.
getTransactionsByType(): фильтрация по типу (INCOME/EXPENSE).
getTransactionsByCategory(): фильтрация по категории.
calculateBalance(): расчёт баланса.
getExpensesByCategory(): сводка расходов по категориям.
deleteTransaction(): удаление транзакции.

Класс FinanceService:
Покрытие инструкций (Line Coverage): 100%(34/34).
Покрытие ветвей (Branch Coverage): 100%(16/16).

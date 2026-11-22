# Help Desk — практическая работа №1 по ООП на Java 17

Консольная система поддержки пользователей: клиент создаёт заявку, специалист
берёт её в работу, решает и закрывает. Заявка сама контролирует допустимые
переходы статуса, уведомления доставляются через интерфейс, заявки хранятся
в репозитории.

## Требования

- JDK 17 (проверено на OpenJDK 17.0.12);
- внешние библиотеки не нужны: сборка обычным `javac`.

## Структура проекта

```
oop/
├── src/main/java/helpdesk/
│   ├── Main.java                        точка входа и демонстрационный сценарий
│   ├── console/ConsoleMenu.java         команда overdue (просроченные заявки)
│   ├── exception/
│   │   ├── InvalidTicketStateException.java
│   │   └── TicketNotFoundException.java
│   ├── model/
│   │   ├── Ticket.java                  заявка: состояние, переходы, история, SLA
│   │   ├── TicketStatus.java            NEW, IN_PROGRESS, RESOLVED, CLOSED, CANCELLED
│   │   ├── TicketPriority.java          LOW, MEDIUM, HIGH, CRITICAL + срок SLA
│   │   ├── TicketCategory.java          категория проблемы + приоритет по умолчанию
│   │   ├── StatusChange.java            запись истории (старый статус, новый, время)
│   │   ├── User.java                    абстрактный пользователь
│   │   ├── Customer.java                клиент
│   │   ├── SupportAgent.java            специалист (назначенные заявки)
│   │   ├── Administrator.java           администратор
│   │   └── Department.java              отдел пользователя (вариант D)
│   ├── repository/
│   │   ├── TicketRepository.java        интерфейс хранилища
│   │   └── InMemoryTicketRepository.java реализация в памяти
│   └── service/
│       ├── NotificationService.java     интерфейс уведомлений
│       ├── ConsoleNotificationService.java
│       ├── EmailNotificationService.java
│       ├── TelegramNotificationService.java
│       ├── TicketService.java           координация работы с заявками
│       ├── PriorityCalculator.java      стратегия выбора приоритета
│       ├── DefaultPriorityCalculator.java
│       ├── VipPriorityCalculator.java
│       └── AuditService.java            журнал действий пользователей
├── src/test/java/helpdesk/TicketTest.java  5 unit-тестов модели
└── build.cmd / run.cmd / test.cmd       сборка, запуск, тесты
```

## Сборка и запуск

Из папки проекта (cmd или двойной щелчок по файлу):

```bat
build.cmd    :: компиляция main + tests в target\
run.cmd      :: запуск приложения
test.cmd     :: unit-тесты
```

Вручную:

```powershell
$files = Get-ChildItem -Recurse src\main\java -Filter *.java | ForEach-Object FullName
javac -encoding UTF-8 -d target\classes $files
java -cp target\classes helpdesk.Main
```

В IntelliJ IDEA: открыть папку проекта, отметить `src/main/java` как Sources Root,
`src/test/java` как Test Sources Root, запустить `helpdesk.Main` или `helpdesk.TicketTest`.

## Ожидаемый вывод обязательного сценария

```
=== HELP DESK ===
Клиент: Анна Петрова
Заявка #1: Не работает Wi-Fi | NEW
[УВЕДОМЛЕНИЕ] Заявка №1 принята в работу
Статус: IN_PROGRESS
[УВЕДОМЛЕНИЕ] По заявке №1 найдено решение
Статус: RESOLVED
[УВЕДОМЛЕНИЕ] Заявка №1 закрыта
Статус: CLOSED
```

## Команды консольного меню

| Команда   | Действие                              |
|-----------|---------------------------------------|
| `overdue` | показать просроченные заявки          |
| `exit`    | выход                                 |

## Что реализовано

- Обязательная часть (задания 1–12): `Ticket`, enum `TicketStatus`, иерархия
  `User`, полиморфизм, `NotificationService`, `TicketService`, `TicketRepository`.
- Самостоятельная работа: уровень 1 — 5 заданий, уровень 2 — 6 заданий
  (включая обязательное №10), уровень 3 — 8 заданий.
- Творческое задание, вариант D: отдел пользователя и автоматический выбор
  приоритета по категории проблемы.

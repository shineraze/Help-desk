# Help Desk + PostgreSQL (практическая работа №2)

Консольное Java 17-приложение Help Desk, работающее с PostgreSQL через JDBC
(без Spring и Hibernate). Продолжение практической работы №1: данные заявок,
пользователей, комментариев и истории статусов хранятся в базе.

Вариант большого самостоятельного задания — **A «База знаний»**
(статьи, категории статей, связь статьи с решённой заявкой).

## Требования

- JDK 17;
- PostgreSQL 16/17/18;
- Maven 3.9+ (рядом с проектом лежит портативный `..\..\tools\apache-maven-3.9.9`;
  можно использовать и установленный `mvn`).

Перед сборкой задайте `JAVA_HOME`, если он ещё не настроен:

```bat
set JAVA_HOME=C:\Program Files\Java\jdk-17
set PATH=%JAVA_HOME%\bin;..\..\tools\apache-maven-3.9.9\bin;%PATH%
```

Драйвер `org.postgresql:postgresql:42.7.13` Maven скачает из Maven Central.

## 1. База данных

Создайте роль и базу (от имени `postgres`):

```sql
CREATE ROLE helpdesk_app
WITH LOGIN PASSWORD 'ваш_пароль';

CREATE DATABASE helpdesk_db
    OWNER helpdesk_app
    ENCODING 'UTF8';
```

Примените схему и данные (psql из `C:\Program Files\PostgreSQL\18\bin`):

```bat
psql -h localhost -U helpdesk_app -d helpdesk_db -f sql/schema.sql
psql -h localhost -U helpdesk_app -d helpdesk_db -f sql/data.sql
psql -h localhost -U helpdesk_app -d helpdesk_db -f sql/knowledge_base.sql
```

## 2. Переменные окружения

Пароль в коде не хранится, он читается из `DB_PASSWORD`:

```bat
set DB_PASSWORD=ваш_пароль
set DB_URL=jdbc:postgresql://localhost:5432/helpdesk_db
set DB_USER=helpdesk_app
```

`DB_URL` и `DB_USER` можно не задавать — используются значения по умолчанию.

## 3. Сборка и запуск

```bat
mvn clean package
java -cp "target/classes;%USERPROFILE%\.m2\repository\org\postgresql\postgresql\42.7.13\postgresql-42.7.13.jar" ru.example.helpdesk.Main
```

В IntelliJ IDEA: открыть `pom.xml` как проект, указать `DB_PASSWORD`
в Run/Debug Configuration → Environment variables и запустить `Main`.

## Структура

```
src/main/java/ru/example/helpdesk/
├── Main.java                 обязательный сценарий + демонстрация варианта A
├── config/DatabaseConfig.java
├── model/                    User, Ticket, Category, Department, комментарии, история, статьи
├── repository/               интерфейсы + jdbc/ реализации
└── service/                  TicketService, KnowledgeBaseService

sql/schema.sql                типы, 7 таблиц, индексы
sql/data.sql                  тестовые данные и первая заявка
sql/knowledge_base.sql        таблицы базы знаний (вариант A)
```

## Что демонстрирует Main

1. подключение к PostgreSQL;
2. поиск клиента по email;
3. список категорий;
4. создание заявки;
5. id из `RETURNING`;
6. `findById`;
7. назначение сотрудника;
8. клиентский комментарий;
9. `IN_PROGRESS -> RESOLVED`;
10. внутренний комментарий;
11. `RESOLVED -> CLOSED`;
12. история статусов;
13. JOIN-карточка с именами клиента и исполнителя;
14. список заявок из БД (данные сохраняются между запусками).

Дополнительно: три отчёта (по статусам, категориям, нагрузке сотрудников)
и сценарий базы знаний — публикация статьи со связью с закрытой заявкой,
выборка по категории, поиск и карточка статьи.

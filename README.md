# Task Manager

### Hexlet tests and linter status:
[![Actions Status](https://github.com/nodirbek9/java-project-99/actions/workflows/hexlet-check.yml/badge.svg)](https://github.com/nodirbek9/java-project-99/actions)

[![SonarQube](https://github.com/nodirbek9/java-project-99/actions/workflows/build.yml/badge.svg)](https://github.com/nodirbek9/java-project-99/actions/workflows/build.yml)

[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=nodirbek9_java-project-99&metric=coverage)](https://sonarcloud.io/summary/new_code?id=nodirbek9_java-project-99)

## О проекте

Task Manager — система управления задачами по мотивам Redmine. Позволяет ставить задачи,
назначать исполнителей, менять статусы и группировать задачи метками.
Для работы требуется регистрация и аутентификация.

### Возможности

- регистрация и аутентификация по email с выдачей JWT;
- CRUD пользователей: редактировать и удалять свою запись может только сам пользователь;
- CRUD статусов, задач и меток;
- связь задач и меток многие-к-многим;
- фильтрация задач по названию, исполнителю, статусу и метке;
- интерактивная документация API;
- сбор ошибок продакшена в Bugsink.

### Стек

Java 21, Spring Boot 3.5, Spring Security (JWT), Spring Data JPA, MapStruct, Gradle,
PostgreSQL в продакшене и H2 в разработке, React Admin на фронте.

## Демо

Задеплоенное приложение: <ВСТАВЬ ССЫЛКУ НА RENDER>

### Данные для входа

| Поле | Значение |
|---|---|
| Username | `hexlet@example.com` |
| Password | `qwerty` |

Администратор создаётся автоматически при старте приложения. Вместе с ним создаются
статусы `draft`, `to_review`, `to_be_fixed`, `to_publish`, `published` и метки `feature`, `bug`.

## Запуск

### Требования

- JDK 21
- Gradle 8.7 или выше (есть wrapper, отдельная установка не нужна)
- Node.js 18 или выше, если нужно пересобрать фронтенд

### Локально

```bash
git clone https://github.com/nodirbek9/java-project-99.git
cd java-project-99
./gradlew bootRun
```

Приложение поднимется на http://localhost:8080 с базой H2 в памяти.

| Адрес | Назначение |
|---|---|
| http://localhost:8080 | фронтенд |
| http://localhost:8080/swagger-ui.html | документация API |
| http://localhost:8080/h2-console | консоль БД |

### Сборка и тесты

```bash
./gradlew build     # компиляция, checkstyle, тесты
./gradlew test      # только тесты
./gradlew checkstyleMain
```

### Продакшен

Профиль `prod` использует PostgreSQL. Настройки берутся из переменных окружения:

| Переменная | Назначение |
|---|---|
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `JDBC_DATABASE_URL` | адрес вида `jdbc:postgresql://host/db` |
| `JDBC_DATABASE_USERNAME` | пользователь БД |
| `JDBC_DATABASE_PASSWORD` | пароль БД |
| `JWT_SECRET` | секрет подписи токена, минимум 32 символа |
| `SENTRY_DSN` | DSN проекта Bugsink |

## API

Все маршруты, кроме `POST /api/login` и `POST /api/users`, требуют заголовок
`Authorization: Bearer <токен>`.

| Метод | Маршрут | Описание |
|---|---|---|
| POST | `/api/login` | аутентификация, возвращает JWT |
| GET | `/api/users` | список пользователей |
| POST | `/api/users` | регистрация |
| GET PUT DELETE | `/api/users/{id}` | чтение, правка и удаление пользователя |
| GET POST | `/api/task_statuses` | статусы задач |
| GET PUT DELETE | `/api/task_statuses/{id}` | операции со статусом |
| GET POST | `/api/tasks` | задачи и фильтрация |
| GET PUT DELETE | `/api/tasks/{id}` | операции с задачей |
| GET POST | `/api/labels` | метки |
| GET PUT DELETE | `/api/labels/{id}` | операции с меткой |

### Пример: вход и запрос с токеном

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/login \
  -H "Content-Type: application/json" \
  -d '{"username":"hexlet@example.com","password":"qwerty"}')

curl http://localhost:8080/api/users -H "Authorization: Bearer $TOKEN"
```

### Пример: фильтрация задач

```text
GET /api/tasks?titleCont=create&assigneeId=1&status=to_be_fixed&labelId=1
```

Любой из параметров необязателен и может быть опущен.

## Архитектура

```text
src/main/java/hexlet/code
├── component        — инициализация данных при старте
├── config           — security, JWT, OpenAPI, валидация, Sentry
├── controller/api   — REST-контроллеры
├── dto              — объекты передачи данных с валидацией
├── entity           — доменные модели
├── exception        — собственные исключения
├── handler          — глобальная обработка ошибок
├── mapper          — преобразование сущностей в DTO
├── repository       — доступ к данным
├── service          — интерфейсы сервисов и их реализации
├── specification    — фильтрация задач
└── util             — вспомогательные классы
```

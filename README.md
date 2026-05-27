# Платформа для перепродажи вещей

Бэкенд-часть платформы для размещения объявлений о продаже вещей с возможностью оставлять комментарии, загружать изображения и управлять пользователями.
Дипломная работа
https://github.com/EvgenyEVS
https://github.com/EvgenyEVS/diploma_platform_for_reselling_items_evs


## 📋 Оглавление

- [Технологии](#технологии)
- [Функциональность](#функциональность)
- [Требования](#требования)
- [Установка и запуск](#установка-и-запуск)
- [Конфигурация](#конфигурация)
- [API Документация](#api-документация)
- [Структура проекта](#структура-проекта)
- [База данных](#база-данных)

## 🚀 Технологии

| Технология | Версия | Назначение |
|------------|--------|------------|
| Java | 11 | Основной язык |
| Spring Boot | 2.7.15 | Основной фреймворк |
| Spring Security | 5.7.10 | Аутентификация и авторизация |
| Spring Data JPA | 2.7.15 | Работа с БД |
| PostgreSQL | 42.3.8 | База данных |
| Liquibase | 4.9.1 | Миграции БД |
| MapStruct | 1.5.5 | Маппинг DTO/Entity |
| Lombok | 1.18.28 | Генерация кода |
| OpenAPI (Swagger) | 1.8.0 | Документация API |
| Hibernate Validator | 6.2.5 | Валидация данных |

## ✨ Функциональность

### Пользователи
- ✅ Регистрация новых пользователей
- ✅ Аутентификация (Basic Auth)
- ✅ Просмотр и редактирование профиля
- ✅ Смена пароля
- ✅ Загрузка аватара

### Объявления
- ✅ Просмотр всех объявлений (без авторизации)
- ✅ Просмотр деталей объявления
- ✅ Создание объявления с картинкой
- ✅ Редактирование объявления
- ✅ Удаление объявления
- ✅ Просмотр своих объявлений
- ✅ Обновление картинки объявления

### Комментарии
- ✅ Просмотр комментариев к объявлению
- ✅ Добавление комментария
- ✅ Редактирование комментария
- ✅ Удаление комментария

### Роли и права
- **USER** - обычный пользователь (CRUD своих объявлений и комментариев)
- **ADMIN** - администратор (CRUD любых объявлений и комментариев)

## 📦 Требования

- Java 11 или выше
- Maven 3.8+
- PostgreSQL 14+
- Docker (опционально, для фронтенда)

## 🛠 Установка и запуск

### 1. Клонирование репозитория

```bash
git clone <repository-url>
cd diploma_platform_for_reselling_items_evs
2. Настройка базы данных
Запустите PostgreSQL и создайте базу данных:

sql
CREATE DATABASE relising_items_db;
CREATE USER relising_items_admin WITH PASSWORD 'diploma123';
GRANT ALL PRIVILEGES ON DATABASE relising_items_db TO relising_items_admin;
3. Настройка конфигурации
Отредактируйте src/main/resources/application.properties:

properties
# База данных
spring.datasource.url=jdbc:postgresql://localhost:5432/relising_items_db
spring.datasource.username=relising_items_admin
spring.datasource.password=diploma123

# Hibernate (Liquibase управляет схемой)
spring.jpa.hibernate.ddl-auto=none

# Liquibase
spring.liquibase.enabled=true
spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml

# Сервер
server.port=8080

# Размер файлов
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
4. Сборка проекта
bash
mvn clean package
5. Запуск приложения
bash
mvn spring-boot:run
Или через JAR:

bash
java -jar target/ads-0.0.1-SNAPSHOT.jar
6. Запуск фронтенда (Docker)
bash
docker run -p 3000:3000 --rm ghcr.io/dmitry-bizin/front-react-avito:v1.21
Фронтенд будет доступен по адресу: http://localhost:3000
```

## ⚙️ Конфигурация
Основные настройки
Параметр	Значение по умолчанию	Описание
```
server.port	8080	Порт сервера
spring.datasource.url	jdbc:postgresql://localhost:5432/relising_items_db	URL БД
spring.jpa.hibernate.ddl-auto	none	Стратегия создания схемы
spring.servlet.multipart.max-file-size	10MB	Максимальный размер файла
CORS настройки
Фронтенд работает на http://localhost:3000, CORS настроен в WebSecurityConfig.
```

### 📚 API Документация
После запуска приложения документация Swagger доступна по адресу:

```
text
http://localhost:8080/swagger-ui.html
Основные эндпоинты
Метод	URL	Описание	Доступ
POST	/register	Регистрация	Все
POST	/login	Авторизация	Все
GET	/users/me	Профиль	USER/ADMIN
PATCH	/users/me	Обновление профиля	USER/ADMIN
POST	/users/set_password	Смена пароля	USER/ADMIN
PATCH	/users/me/image	Загрузка аватара	USER/ADMIN
GET	/ads	Все объявления	Все
GET	/ads/{id}	Детали объявления	Все
POST	/ads	Создание объявления	USER/ADMIN
DELETE	/ads/{id}	Удаление объявления	USER/ADMIN
PATCH	/ads/{id}	Обновление объявления	USER/ADMIN
GET	/ads/{id}/comments	Комментарии	Все
POST	/ads/{id}/comments	Добавить комментарий	USER/ADMIN
DELETE	/ads/{id}/comments/{commentId}	Удалить комментарий	USER/ADMIN
```

## 📁 Структура проекта
```text
src/
├── main/
│   ├── java/ru/skypro/homework/
│   │   ├── config/          # Конфигурации (Security, CORS)
│   │   ├── controller/      # REST контроллеры
│   │   ├── dto/             # Data Transfer Objects
│   │   │   ├── ads/         # DTO для объявлений
│   │   │   ├── comments/    # DTO для комментариев
│   │   │   └── user/        # DTO для пользователей
│   │   ├── exception/       # Обработчики исключений
│   │   ├── filter/          # Кастомные фильтры
│   │   ├── mapper/          # MapStruct мапперы
│   │   ├── model/           # JPA сущности
│   │   ├── repository/      # Spring Data репозитории
│   │   └── service/         # Бизнес-логика
│   │       └── impl/        # Реализации сервисов
│   └── resources/
│       ├── db/changelog/    # Liquibase миграции
│       │   ├── v1/          # Версия 1: создание таблиц
│       │   └── v2/          # Версия 2: индексы
│       └── application.properties
└── test/                    # Тесты
    ├── controller/          # Интеграционные тесты
    └── service/impl/        # Unit-тесты
```

## 💾 База данных
Схема БД
```
sql
-- Таблица пользователей
users (
    id         SERIAL PRIMARY KEY,
    username   VARCHAR(255) UNIQUE NOT NULL,
    password   VARCHAR(255) NOT NULL,
    first_name VARCHAR(16) NOT NULL,
    last_name  VARCHAR(16) NOT NULL,
    phone      VARCHAR(50) NOT NULL,
    role       VARCHAR(10) NOT NULL,
    image      VARCHAR(255)
)

-- Таблица объявлений
ads (
    pk          SERIAL PRIMARY KEY,
    author_id   INTEGER REFERENCES users(id),
    title       VARCHAR(32) NOT NULL,
    price       INTEGER NOT NULL,
    description VARCHAR(255) NOT NULL,
    image       VARCHAR(255)
)

-- Таблица комментариев
comments (
    pk         SERIAL PRIMARY KEY,
    author_id  INTEGER REFERENCES users(id),
    ad_id      INTEGER REFERENCES ads(pk),
    text       VARCHAR(64) NOT NULL,
    created_at BIGINT NOT NULL
)
```
### Миграции (Liquibase)
Проект использует Liquibase для управления схемой БД. Миграции находятся в src/main/resources/db/changelog/:

v1/ - Создание таблиц

v2/ - Добавление индексов для производительности

После запуска приложения таблица databasechangelog будет отслеживать выполненные миграции.

Примечание: Для работы с приложением используйте фронтенд на порту 3000. Бэкенд должен быть запущен на порту 8080.

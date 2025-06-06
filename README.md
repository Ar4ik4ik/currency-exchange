# Currency Exchange API

**Проект:** REST API для обмена валют  
**Технологии:** Java (Servlet API), SQLite, Tomcat, MVC, JSON  
**Цель:** Научиться проектировать REST API с нуля без фреймворков, углубиться в архитектуру и работу с базой данных.

## Описание

Этот проект реализует API для обмена валют, который включает в себя базовую логику получения, добавления и обновления курсов валют. API поддерживает операции GET и POST для обменных курсов и добавления новых.

## Архитектура

Проект основан на архитектуре **MVC** (Model-View-Controller):

- **Model** — логика обработки данных и взаимодействие с базой данных.
- **View** — JSON-ответы, возвращаемые клиенту.
- **Controller** — сервлеты, которые обрабатывают запросы и возвращают ответы.

API предоставляет несколько эндпоинтов для работы с валютами и обменными курсами:

- **`GET /exchangeRate/{base}{target}`** — Получение конкретного обменного курса. Валютная пара задаётся идущими подряд кодами валют в адресе запроса.
- **`POST /exchangeRates`** — Добавление нового обменного курса в базу.
- **`GET /currencies`** — Получение списка всех обменных курсов.
- **`POST /currencies`** — Добавление новой валюты в базу.
- **`GET /currency/EUR`** — Получение конкретной валюты.
- **`PATCH /exchangeRate/{base}{target}`** — Обновление существующего в базе обменного курса. Валютная пара задаётся идущими подряд кодами валют в адресе запроса.
- **`GET /exchange?from=BASE_CURRENCY_CODE&to=TARGET_CURRENCY_CODE&amount=$AMOUNT`** — Выполнить обмен валют с указанной суммой.

## Технологии

- **Java** — основной язык разработки.
- **SQLite** — база данных для хранения курсов валют.
- **Tomcat** — веб-сервер для запуска сервлетов.
- **JDBC** — для подключения к базе данных.
- **JSON** — формат ответа API.

## Описание классов

### ConnectionManager
Управляет подключениями к базе данных SQLite.

```java
public final class ConnectionManager {
    public static Connection open() { ... }
}
```

## Сервлеты
- **CurrencyServlet** - Обрабатывает запросы на получение списка валют (GET) и создание нового курса (POST).
- **ExchangeRateServlet** — Обрабатывает запросы на получение курса валют (GET) и обновление курса (PATCH).
- **ExchangeRatesServlet** — Обрабатывает запросы на получение всех курсов валют (GET) и создание нового обменного курса (POST).
- **ExchangeServlet** — Обрабатывает запросы на выполнение обмена валют (GET).

## Обработка ошибок
Ошибки обрабатываются централизованно с помощью классов ErrorDto и ErrorCode, а также метода respondWithError(), который отправляет стандартные ошибки в JSON-формате.
```java
public static void respondWithError(ErrorCode errorCode, HttpServletResponse resp, Class<?> clazz) { ... }
```

## Валидация
Валидация входных данных (например, коды валют и сумма обмена) проводится с помощью простых проверок на null, blank, и формат чисел.

## Установка
**Для запуска проекта потребуется:**
- Java 11+
- Tomcat для развертывания сервлетов.
- SQLite для хранения данных.

**Клонируйте репозиторий:**

```bash
git clone https://github.com/yourusername/currency-exchange-api.git
```
1. Настройте Tomcat на вашем компьютере.
2. Разверните проект на сервере.
3. Настройте базу данных SQLite и подключите её через ConnectionManager.

## Лицензия
Проект распространяется под лицензией MIT. См. [LICENSE](LICENSE) для подробностей.

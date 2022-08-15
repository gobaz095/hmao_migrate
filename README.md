# Сервис для миграции данных Oracle-Postgres #


## Конфигурации запуска ##

#### Запуск тестов локально

#### Создание образа Docker ####

`podman build -t migrate .`

#### Запуск сервиса с помощью Docker Compose

`podman-compose -p migrate up -d`

`podman-compose -p migrate down`

swagger: localhost:8089/swagger-ui/index.html?configUrl=/docs/swagger-config
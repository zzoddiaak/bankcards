1. Клонируйте репозиторий

2. Соберите проект:
   mvn clean install

3. Запустите Docker-контейнеры:
   docker-compose up --build

4. После запуска откройте Swagger UI в браузере:
   http://localhost:8080/swagger-ui/index.html

Важно! При регистрации пользователя указывайте роль:
   - ID 1 (user) - для обычного пользователя
   - ID 2 (admin) - для администратора

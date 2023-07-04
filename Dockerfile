FROM openjdk:11
ENV MYSQL_HOST=db \
     MYSQL_USER=root \
     MYSQL_PASSWORD=yonghwan1107
WORKDIR /app
COPY . .
ARG JAR_FILE=./build/libs/*.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-Dspring.profiles.active=prod","-jar","app.jar"]

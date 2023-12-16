FROM openjdk:17-jdk-alpine
EXPOSE 8080:8080
ADD target/signal-xakaton.jar app/signal-xakaton.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseContainerSupport", "-jar","app/signal-xakaton.jar"]
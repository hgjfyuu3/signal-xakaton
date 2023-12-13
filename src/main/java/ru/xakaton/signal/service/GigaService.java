package ru.xakaton.signal.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class GigaService {

    private final WebClient webClient;
    private String accessToken;

    @Value("${giga.authorization-token}")
    private String authorizationToken;

    @PostConstruct
    public void init() throws InterruptedException {
        accessToken = getAccessToken(authorizationToken);
        log.info(accessToken);
        Thread.sleep(1000L);
        System.err.println(requestGiga("Кто убил Марка?"));
    }

    private String getAccessToken(String authorizationToken) {
        String apiUrl = "https://ngw.devices.sberbank.ru:9443/api/v2/oauth";

        String scope = "GIGACHAT_API_PERS";
        try {
            Mono<RequestAccessToken> responseMono = webClient.post()
                .uri(apiUrl)
                .header("RqUID", UUID.randomUUID().toString())
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .header(HttpHeaders.AUTHORIZATION, authorizationToken)
                .body(BodyInserters.fromFormData("scope", scope))
                .retrieve()
                .bodyToMono(RequestAccessToken.class);
            return Objects.requireNonNull(responseMono.block()).getAccessToken();
        } catch (Exception e) {
            log.error("Произошла ошибка при получение токена.", e);
        }
        return "";
    }

    public ChatResponse requestGiga(String question) {

        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setModel("GigaChat:latest");
        chatRequest.setTemperature(0.87);
        chatRequest.setN(1);
        chatRequest.setRepetition_penalty(1.07);

        ChatRequest.ChatMessage systemMessage = new ChatRequest.ChatMessage();
        systemMessage.setRole("system");
        systemMessage.setContent("Отвечай как научный сотрудник");

        ChatRequest.ChatMessage userMessage = new ChatRequest.ChatMessage();
        userMessage.setRole("user");
        userMessage.setContent(question);

        chatRequest.setMessages(List.of(systemMessage, userMessage));

        try {
            Mono<String> response = webClient.post()
                .uri(new URI("https://gigachat.devices.sberbank.ru/api/v1/chat/completions"))
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .body(BodyInserters.fromValue(chatRequest))
                .retrieve()
                .bodyToMono(String.class);
            System.err.println(response.block());
            return null;
        } catch (Exception e) {
            log.error("Произошла ошибка при задавании вопроса.", e);
        }
        return null;
    }

    @Data
    static class RequestAccessToken {
        @JsonProperty("access_token")
        private String AccessToken;
        @JsonProperty("expires_at")
        private Long expiresAt;
    }

    @Data
    static class ChatRequest {
        private String model;
        private double temperature;
        private int n;
        private double repetition_penalty;
        private List<ChatMessage> messages;

        @Data
        public static class ChatMessage {
            private String role;
            private String content;
        }
    }

    @Data
    static class ChatResponse {
        private List<Choice> choices;
        private long created;
        private String model;
        private String object;
        private Usage usage;

        @Data
        static class Choice {
            private String finish_reason;
            private int index;
            private Message message;
        }

        @Data
        static class Message {
            private String content;
            private String role;
        }

        @Data
        public static class Usage {
            private int completion_tokens;
            private int prompt_tokens;
            private int total_tokens;
        }
    }
}

package ru.xakaton.signal.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class GigaService {

    private final WebClient webClient;
    private String accessToken;

    @Value("${giga.authorization-token}")
    private String authorizationToken;

    @Getter
    private Map<Integer, List<ChatMessage>> chats = new ConcurrentHashMap<>();

    private static final String GIGA_OAUTH_API_URL = "https://ngw.devices.sberbank.ru:9443/api/v2/oauth";
    private static final String GIGA_CHAT_API_URL = "https://gigachat.devices.sberbank.ru/api/v1/chat/completions";
    private static final String GIGA_SCOPE = "GIGACHAT_API_PERS";

    @PostConstruct
    public void init() {
        getAccessToken();
    }

    @Scheduled(fixedDelay = 600000)
    private void getAccessToken() {
        webClient.post()
            .uri(GIGA_OAUTH_API_URL)
            .header("RqUID", UUID.randomUUID().toString())
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .header(HttpHeaders.AUTHORIZATION, authorizationToken)
            .body(BodyInserters.fromFormData("scope", GIGA_SCOPE))
            .retrieve()
            .bodyToMono(RequestAccessToken.class)
            .doOnNext(response -> {
                log.info("Access token expires at: {}", response.getExpiresAt());
                accessToken = response.getAccessToken();
            })
            .doOnError(error -> log.error("Error while obtaining token.", error))
            .subscribe();
    }

    public String requestGiga(String question, Integer userId, String role) {
        ChatRequest chatRequest = getChatRequest(question, userId, role);
        System.err.println(BodyInserters.fromValue(chatRequest));
        return webClient.post()
            .uri(GIGA_CHAT_API_URL)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .body(BodyInserters.fromValue(chatRequest))
            .retrieve()
            .bodyToMono(ChatResponse.class)
            .onErrorResume(e -> {
                log.error("Error while asking question.", e);
                return Mono.empty();
            })
            .block().getChoices().get(0).getMessage().getContent();
    }

    @NotNull
    private ChatRequest getChatRequest(String question, Integer userId, String role) {
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setModel("GigaChat:latest");
        chatRequest.setTemperature(0.87);
        chatRequest.setN(1);
        chatRequest.setRepetition_penalty(1.07);

        List<ChatMessage> chatMessageList = chats.get(userId);
        ChatMessage systemMessage = new ChatMessage();

        ChatMessage userMessage = new ChatMessage();
        userMessage.setRole("user");
        userMessage.setContent(question);

        systemMessage.setRole("system");
        systemMessage.setContent(role);

        if(chatMessageList == null){
            chatMessageList = new ArrayList<>();
        }
        chatMessageList.add(userMessage);
        chatMessageList.add(systemMessage);
        chatMessageList.add(userMessage);
        chatMessageList.add(systemMessage);
        chatRequest.setMessages(chatMessageList);
        return chatRequest;
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
    }

    @Data
    public static class ChatMessage {
        private String role;
        private String content;
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

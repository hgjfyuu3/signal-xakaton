package ru.xakaton.signal.service;

import api.longpoll.bots.LongPollBot;
import api.longpoll.bots.exceptions.VkApiException;
import api.longpoll.bots.model.events.messages.MessageNew;
import api.longpoll.bots.model.objects.additional.Keyboard;
import api.longpoll.bots.model.objects.additional.buttons.Button;
import api.longpoll.bots.model.objects.additional.buttons.TextButton;
import api.longpoll.bots.model.objects.basic.Message;
import com.google.gson.JsonObject;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.xakaton.signal.model.UserState;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class VkBot extends LongPollBot {


    private final GigaService gigaService;

    private final Map<Integer, UserState> userStateMap = new ConcurrentHashMap<>();

    @Override
    public void onMessageNew(MessageNew messageNew) {
        try {
            Message message = messageNew.getMessage();
            Integer userId = message.getPeerId();
            if (userStateMap.get(message.getPeerId()) == UserState.START) {
                processUserMessage(message);
                return;
            }

            if (userStateMap.get(message.getPeerId()) == null) {
                String welcomeMessage = "Привет! Добро пожаловать в информационного помощника районной администрации! Я готов помочь вам узнать о планах развития нашего района. Выберите, интересующую вас тему, и я постараюсь предоставить вам соответствующую информацию. Если у вас есть конкретные вопросы, не стесняйтесь задавать!";
                userStateMap.put(message.getPeerId(), UserState.START);
                send(userId, welcomeMessage, getStartedKeyboard());
            }

            if (userStateMap.get(message.getPeerId()) == UserState.ASK_QUESTION) {
                if (message.getText().equals("Закончить диалог и вернуться назад")) {
                    send(userId, getStartedKeyboard());
                    gigaService.getChats().remove(userId);
                    userStateMap.put(message.getPeerId(), UserState.START);
                }
                if (message.getText().equals("Закончить диалог и задать новый вопрос")) {
                    send(userId, getStartedKeyboard());
                    gigaService.getChats().remove(userId);
                } else {
                    System.err.println("DVA");
                    send(userId, gigaService.requestGiga(message.getText(), userId, "Представь что ты искусственный интеллект администрации и ответь на вопрос"), createAskQuestionKeyboard());
                    System.err.println("RAZ");
                }
            }

            if (userStateMap.get(message.getPeerId()) == UserState.DOCUMENTS) {
                //todo добавить обработку вопроса по документам
            }

            if (userStateMap.get(message.getPeerId()) == UserState.NITING) {
                send(userId, gigaService.requestGiga(message.getText(), userId, "Представь что ты искусственный интеллект администрации и ответь на обращение "), getStartedKeyboard());
                userStateMap.put(message.getPeerId(), UserState.START);
            }

            if (userStateMap.get(message.getPeerId()) == UserState.NITING) {
                send(userId, gigaService.requestGiga(message.getText(), userId, "Представь что ты искусственный интеллект администрации и ответь на обращение "), getStartedKeyboard());
                userStateMap.put(message.getPeerId(), UserState.START);
            }
        } catch (VkApiException e) {
            e.printStackTrace();
        }
    }

    private void processUserMessage(Message message) throws VkApiException {
        String userText = message.getText().toLowerCase();

        if (userText.contains("1")) {
            userStateMap.put(message.getPeerId(), UserState.DOCUMENTS);
            send(message.getPeerId(), "Напишите ваш вопрос по нормативной документации");
        } else if (userText.contains("2")) {
            userStateMap.put(message.getPeerId(), UserState.ASK_QUESTION);
            send(message.getPeerId(), "Напишите ваш вопрос");
        } else if (userText.contains("3")) {
            userStateMap.put(message.getPeerId(), UserState.NITING);
            send(message.getPeerId(), "Напишите ваше обращение");
        } else if (userText.contains("4")) {
            String faqText = "1. Как получить помощь по использованию ресурсов/сервисов?\n" +
                "   - Ответ: Обращайтесь в нашу службу поддержки по указанным контактам, где опытные специалисты готовы помочь вам с любыми вопросами.\n" +
                "\n" +
                "2. Как оформить заявку на предоставление услуг?\n" +
                "   - Ответ: Заявки принимаются через наш онлайн-портал или путем обращения в соответствующий отдел. Пожалуйста, предоставьте необходимую информацию для более быстрого и точного обслуживания.\n" +
                "\n" +
                "3. Какие методы оплаты поддерживаются?\n" +
                "   - Ответ: Мы принимаем платежи различными способами, включая кредитные карты, электронные переводы и другие. Пожалуйста, проверьте раздел \"Оплата\" на нашем сайте для подробной информации.\n" +
                "\n" +
                "4. Кто стоял в комнате?\n" +
                "   - Ответ: В комнате стояло трое: он, она и у него\n" +
                "\n" +
                "5. Что делать при возникновении проблем с услугами?\n" +
                "   - Ответ: Первым шагом является обращение в службу поддержки. Опишите проблему максимально подробно, приложите скриншоты (если применимо). Наша команда постарается решить вопрос в кратчайшие сроки.\n" +
                "\n" +
                "6. Как изменить личные данные в учетной записи?\n" +
                "   - Ответ: Войдите в свою учетную запись на нашем сайте и перейдите в раздел \"Личная информация\". Там вы сможете внести необходимые изменения.\n" +
                "\n" +
                "7. Как оценить качество предоставляемых услуг?\n" +
                "   - Ответ: Мы ценим ваше мнение. Оставляйте отзывы на нашем сайте или в социальных сетях. Мы также периодически проводим опросы, чтобы узнать ваше мнение о качестве обслуживания.\n" +
                "\n" +
                "8. Как получить справку или подтверждение об оказанных услугах?\n" +
                "   - Ответ: Вы можете запросить справку через личный кабинет на сайте или обратиться в соответствующий отдел. Укажите необходимую информацию для быстрого оформления документа.\n" +
                "\n" +
                "9. Как подписаться на новостную рассылку?\n" +
                "   - Ответ: Перейдите на наш сайт и найдите раздел \"Рассылка\" или \"Новости\". Там вы сможете оформить подписку, указав свой электронный адрес.\n" +
                "\n" +
                "10. Каковы основные преимущества наших услуг по сравнению с конкурентами?\n" +
                "    - Ответ: Наши услуги отличаются высоким качеством, конкурентоспособными ценами и индивидуальным подходом к каждому клиенту. Подробнее о преимуществах вы можете узнать на нашем сайте или у представителей компании.";
            send(message.getPeerId(), faqText, getStartedKeyboard());
        }
    }

    public void send(Integer peerId, String message, Keyboard keyboard) throws VkApiException {
        vk.messages.send()
            .setPeerId(peerId)
            .setMessage(message)
            .setKeyboard(keyboard)
            .execute();
    }

    public void send(Integer peerId, String message) throws VkApiException {
        vk.messages.send()
            .setPeerId(peerId)
            .setMessage(message)
            .execute();
    }

    public void send(Integer peerId, Keyboard keyboard) throws VkApiException {
        vk.messages.send()
            .setPeerId(peerId)
            .setKeyboard(keyboard)
            .execute();
    }

    private Keyboard createAskQuestionKeyboard() {
        List<Button> buttons = Arrays.asList(
            createButton("Закончить диалог и вернуться назад", Button.Color.POSITIVE),
            createButton("Закончить диалог и задать новый вопрос", Button.Color.POSITIVE)
        );

        List<List<Button>> rows = List.of(buttons);

        return new Keyboard(rows).setInline(true);
    }

    private Keyboard getStartedKeyboard() {
        List<Button> buttons = Arrays.asList(
            createButton("1", Button.Color.POSITIVE),//дока
            createButton("2", Button.Color.POSITIVE),//вопрос
            createButton("3", Button.Color.POSITIVE), // обращение
            createButton("4", Button.Color.POSITIVE)// топ вопросов
        );

        List<List<Button>> rows = List.of(buttons);

        return new Keyboard(rows).setInline(true);
    }

    private Button createButton(String label, Button.Color color) {
        return new TextButton(color, new TextButton.Action(label));
    }

    @Override
    public String getAccessToken() {
        return "vk1.a.yUV_JB0A1I2tANyu0kOvEy8rO3TWeyze0Jnw6jTjhfB_Pyce2P82ny1essU2sCEHhrodYeDyT6eRrZ-E6EfiC-bR1OID2YExYjnE_aM7FXPnhfDfIRoq6qqzHhrZlZULE_vw93KdZYuVfdODARYpxZxHrFMqg3YLe0tuwSRhR9qRp0LQtEL0p_cPc5L49v8UxPUIanxG8wDt4zacvTznRw";
    }

    @PostConstruct
    protected void createBotServer() {
        CompletableFuture.runAsync(() -> {
            try {
                this.startPolling();
            } catch (VkApiException e) {
                System.err.println(e);
            }
        });
    }
}
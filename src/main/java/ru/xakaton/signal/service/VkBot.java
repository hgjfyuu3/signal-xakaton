package ru.xakaton.signal.service;

import api.longpoll.bots.LongPollBot;
import api.longpoll.bots.exceptions.VkApiException;
import api.longpoll.bots.model.events.messages.MessageNew;
import api.longpoll.bots.model.objects.additional.Keyboard;
import api.longpoll.bots.model.objects.additional.buttons.Button;
import api.longpoll.bots.model.objects.additional.buttons.TextButton;
import api.longpoll.bots.model.objects.basic.Message;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.xakaton.signal.model.UserState;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
@RequiredArgsConstructor
public class VkBot extends LongPollBot {

    @Value(value = "${vk.api.access-token}")
    private String vkAccessToken;

    private final GigaService gigaService;

    private final Map<Integer, UserState> userStateMap = new ConcurrentHashMap<>();

    @Override
    public void onMessageNew(MessageNew messageNew) {
        try {
            Message message = messageNew.getMessage();
//            Integer userId = message.getPeerId();
            Integer peerId = message.getPeerId();
            if (userStateMap.get(peerId) == UserState.START) {
                processUserMessage(message);
                return;
            }

            if (userStateMap.get(peerId) == null) {
                String welcomeMessage = "Привет! Добро пожаловать в информационного помощника районной администрации! Я готов помочь вам узнать о планах развития нашего района. Выберите, интересующую вас тему, и я постараюсь предоставить вам соответствующую информацию. Если у вас есть конкретные вопросы, не стесняйтесь задавать!";
                userStateMap.put(peerId, UserState.START);
                send(peerId, welcomeMessage, getStartedKeyboard());
            }

            if (userStateMap.get(peerId) == UserState.ASK_QUESTION) {
                if (message.getText().equals("Закончить диалог и вернуться назад")) {
                    send(peerId, getStartedKeyboard());
                    gigaService.getChats().remove(peerId);
                    userStateMap.put(peerId, UserState.START);
                }
                if (message.getText().equals("Закончить диалог и задать новый вопрос")) {
                    send(peerId, getStartedKeyboard());
                    gigaService.getChats().remove(peerId);
                } else {
                    send(peerId, gigaService.requestGiga(message.getText(), peerId, "Представь что ты искусственный интеллект администрации и ответь на вопрос"), createAskQuestionKeyboard());
                }
            }

            if (userStateMap.get(peerId) == UserState.DOCUMENTS) {
                //todo добавить обработку вопроса по документам
            }

            if (userStateMap.get(peerId) == UserState.NITING) {
                send(peerId, gigaService.requestGiga(message.getText(), peerId, "Представь что ты искусственный интеллект администрации и ответь на обращение "), getStartedKeyboard());
                userStateMap.put(peerId, UserState.START);
            }

            if (userStateMap.get(peerId) == UserState.NITING) {
                send(peerId, gigaService.requestGiga(message.getText(), peerId, "Представь что ты искусственный интеллект администрации и ответь на обращение "), getStartedKeyboard());
                userStateMap.put(peerId, UserState.START);
            }
        } catch (VkApiException e) {
            log.error("Ошибка при обращении к апи вк", e);
        }
    }

    private void processUserMessage(Message message) throws VkApiException {
        String userText = message.getText().toLowerCase();

        if (userText.equalsIgnoreCase("Вопрос по нормативной документации")) {
            userStateMap.put(message.getPeerId(), UserState.DOCUMENTS);
            send(message.getPeerId(), "Напишите ваш вопрос по нормативной документации");
        } else if (userText.equalsIgnoreCase("Задать другой вопрос")) {
            userStateMap.put(message.getPeerId(), UserState.ASK_QUESTION);
            send(message.getPeerId(), "Напишите ваш вопрос");
        } else if (userText.equalsIgnoreCase("Создать обращение")) {
            userStateMap.put(message.getPeerId(), UserState.NITING);
            send(message.getPeerId(), "Напишите ваше обращение");
        } else if (userText.equalsIgnoreCase("Самые популярные вопросы и ответы на них")) {
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
        List<Button> buttons1 = Arrays.asList(
                createButton("Вопрос по нормативной документации", Button.Color.POSITIVE) //дока
        );
        List<Button> buttons2 = Arrays.asList(
                createButton("Задать другой вопрос", Button.Color.POSITIVE) //вопрос
        );
        List<Button> buttons3 = Arrays.asList(
                createButton("Создать обращение", Button.Color.POSITIVE) // обращение
        );
        List<Button> buttons4 = Arrays.asList(
                createButton("Самые популярные вопросы и ответы на них", Button.Color.POSITIVE) // топ вопросов
        );
        return new Keyboard(List.of(buttons1, buttons2, buttons3, buttons4)).setInline(true);
    }

    private Button createButton(String label, Button.Color color) {
        return new TextButton(color, new TextButton.Action(label));
    }

    @Override
    public String getAccessToken() {
        return vkAccessToken;
    }

    @PostConstruct
    protected void createBotServer() {
        CompletableFuture.runAsync(() -> {
            try {
                this.startPolling();
            } catch (VkApiException e) {
                log.error("Ошибка запуска вк апи", e);
            }
        });
    }
}
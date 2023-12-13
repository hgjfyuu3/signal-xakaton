package ru.xakaton.signal;

import api.longpoll.bots.LongPollBot;
import api.longpoll.bots.exceptions.VkApiException;
import api.longpoll.bots.model.events.messages.MessageNew;
import api.longpoll.bots.model.objects.basic.Message;

public class VkBot extends LongPollBot {

    @Override
    public void onMessageNew(MessageNew messageNew) {
        try {
            Message message = messageNew.getMessage();
            if (message !=null && message.hasText()) {
                String response = "Hello! Received your message: " + message.getText();
                vk.messages.send()
                        .setPeerId(message.getPeerId())
                        .setMessage(response)
                        .execute();
            }
        } catch (VkApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getAccessToken() {
        return "vk1.a.yUV_JB0A1I2tANyu0kOvEy8rO3TWeyze0Jnw6jTjhfB_Pyce2P82ny1essU2sCEHhrodYeDyT6eRrZ-E6EfiC-bR1OID2YExYjnE_aM7FXPnhfDfIRoq6qqzHhrZlZULE_vw93KdZYuVfdODARYpxZxHrFMqg3YLe0tuwSRhR9qRp0LQtEL0p_cPc5L49v8UxPUIanxG8wDt4zacvTznRw";
    }

}
package io.osvaldas.notifications.domain.emails;

public interface EmailSender {

    void send(String receiverEmail, String content);
}

package helpdesk.service;

import java.util.Objects;

/**
 * Имитация отправки уведомлений в Telegram (задание 10 уровня 2).
 */
public class TelegramNotificationService implements NotificationService {

    private final String chatId;

    public TelegramNotificationService(String chatId) {
        this.chatId = Objects.requireNonNull(chatId, "Идентификатор чата не может быть null");
    }

    @Override
    public void send(String message) {
        System.out.println("[TELEGRAM -> " + chatId + "] " + message);
    }
}

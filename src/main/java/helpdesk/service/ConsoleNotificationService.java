package helpdesk.service;

/**
 * Отправка уведомлений в консоль — реализация по умолчанию.
 */
public class ConsoleNotificationService implements NotificationService {

    @Override
    public void send(String message) {
        System.out.println("[УВЕДОМЛЕНИЕ] " + message);
    }
}

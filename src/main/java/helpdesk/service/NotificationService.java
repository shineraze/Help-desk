package helpdesk.service;

/**
 * Контракт отправки уведомлений.
 *
 * <p>Сервис заявок знает, что уведомление отправить можно, но не знает,
 * как именно: консоль, email или Telegram. Это и есть зависимость от
 * абстракции (задание 11).</p>
 */
public interface NotificationService {

    void send(String message);
}

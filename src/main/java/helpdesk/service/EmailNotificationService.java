package helpdesk.service;

import java.util.Objects;

/**
 * Имитация отправки уведомлений на email (задание 10 уровня 2).
 *
 * <p>Настоящая отправка письма здесь не нужна: важен полиморфизм —
 * TicketService работает с интерфейсом, а канал выбирается при сборке
 * приложения.</p>
 */
public class EmailNotificationService implements NotificationService {

    private final String address;

    public EmailNotificationService(String address) {
        this.address = Objects.requireNonNull(address, "Адрес получателя не может быть null");
    }

    @Override
    public void send(String message) {
        System.out.println("[EMAIL -> " + address + "] " + message);
    }
}

package helpdesk.model;

/**
 * Клиент — пользователь, который сообщает о проблеме и создаёт заявку.
 *
 * <p>Клиент не умеет выполнять действия специалиста: у него нет методов
 * взятия в работу, решения и закрытия заявки. Это проверяется не строкой
 * роли, а типами: сервисные методы требуют {@link SupportAgent}.</p>
 */
public class Customer extends User {

    private final boolean vip;

    public Customer(long id, String name, String email, Department department, boolean vip) {
        super(id, name, email, department);
        this.vip = vip;
    }

    /** VIP-клиенту приоритет заявки повышается (стратегия VipPriorityCalculator). */
    public boolean isVip() {
        return vip;
    }

    @Override
    public void performAction() {
        System.out.println(getName() + " создаёт заявку");
    }
}

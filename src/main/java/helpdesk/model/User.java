package helpdesk.model;

import java.util.Objects;

/**
 * Общая модель пользователя системы.
 *
 * <p>Абстрактный класс: пользователя «вообще» не существует, существует
 * конкретная роль (клиент, специалист, администратор), поэтому нельзя
 * создать {@code new User(...)}.</p>
 */
public abstract class User {

    private final long id;
    private final String name;
    private final String email;
    private final Department department;

    protected User(long id, String name, String email, Department department) {
        this.id = id;
        this.name = requireText(name, "Имя пользователя");
        this.email = requireText(email, "Email пользователя");
        this.department = Objects.requireNonNull(department, "Отдел не может быть null");
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Department getDepartment() {
        return department;
    }

    /**
     * Действие, которое пользователь выполняет в системе.
     * Реализация определяется конкретной ролью (полиморфизм).
     */
    public abstract void performAction();

    protected static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " не может быть пустым");
        }
        return value.trim();
    }
}

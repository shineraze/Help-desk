package helpdesk.model;

import java.util.Objects;

/**
 * Отдел, в котором работает пользователь.
 *
 * <p>Композиция: пользователь содержит отдел как часть своего состояния
 * (творческое задание, вариант D «Корпоративный Help Desk»).</p>
 */
public class Department {

    private final String name;

    public Department(String name) {
        this.name = Objects.requireNonNull(name, "Название отдела не может быть null").trim();
    }

    public String getName() {
        return name;
    }
}

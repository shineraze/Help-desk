package helpdesk.model;

/**
 * Администратор — пользователь, который управляет системой.
 *
 * <p>Отдельная роль со своим поведением: администратор не обрабатывает
 * заявки, поэтому у него нет методов специалиста.</p>
 */
public class Administrator extends User {

    public Administrator(long id, String name, String email, Department department) {
        super(id, name, email, department);
    }

    @Override
    public void performAction() {
        System.out.println(getName() + " управляет системой");
    }
}

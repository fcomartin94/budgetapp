import repository.TransaccionRepository;
import service.BudgetService;
import ui.ConsolaMenu;

/**
 * Entry point for Finanz Core.
 *
 * <p>Wires the application using manual dependency injection: the repository is
 * created first, injected into the service, and the service injected into the menu.
 * Each layer only knows the one immediately below it — this is the layered
 * architecture pattern used in every professional project, here implemented
 * without any framework.</p>
 */
public class Main {
    public static void main(String[] args) {
        TransaccionRepository repositorio = new TransaccionRepository();
        BudgetService servicio = new BudgetService(repositorio);
        ConsolaMenu menu = new ConsolaMenu(servicio);

        menu.iniciar();
    }
}
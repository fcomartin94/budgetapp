/* Este es el séptimo paso, el módulo 5. Aquí conectamos todo lo que hemos hecho hasta ahora.
Aquí se ve claramente la cadena de dependencias que hemos construido a lo largo de todos los módulos:
el repositorio se crea primero, se inyecta en el servicio, y el servicio se inyecta en el menú. Cada
capa solo conoce a la capa inmediatamente por debajo de ella, nunca salta niveles. Esto es exactamente
el patrón de arquitectura en capas que encontraremos en cualquier proyecto profesional. Para acabar,
añadiremos algunas líneas de reportes a algunos de los archivos ya creados. Vamos primero a
BudgetService.java */

import repository.TransaccionRepository;
import service.BudgetService;
import ui.ConsolaMenu;

public class Main {
    public static void main(String[] args) {
        TransaccionRepository repositorio = new TransaccionRepository();
        BudgetService servicio = new BudgetService(repositorio);
        ConsolaMenu menu = new ConsolaMenu(servicio);

        menu.iniciar();
    }
}
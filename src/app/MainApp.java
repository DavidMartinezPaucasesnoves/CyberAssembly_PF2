package app;

import java.util.Scanner;
import model.concurrency.CentralSoldadura;
import model.concurrency.CintaTransportadora;
import model.concurrency.SupervisorCentral;
import tasks.AGVTask;
import tasks.RobotTask;
import view.ui.DashboardTask;

// Clase principal que configura la simulacion, lanza los hilos con start()
// y realiza el cierre ordenado con interrupt() y join()
public class MainApp {

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        System.out.println("=== CONFIGURACION CYBERASSEMBLY ===");
        int numEstaciones = pedirNumero(sc, "Numero de estaciones de soldadura", 3);
        int capacidadCinta = pedirNumero(sc, "Capacidad de la cinta transportadora", 5);
        int numRobots = pedirNumero(sc, "Numero de robots", 4);
        int numAgvs = pedirNumero(sc, "Numero de AGVs", 2);

        // 1. Instanciamos los recursos compartidos (monitores y semaforos)
        SupervisorCentral supervisor = new SupervisorCentral(numRobots, numAgvs);
        CintaTransportadora cinta = new CintaTransportadora(capacidadCinta);
        CentralSoldadura soldadura = new CentralSoldadura(numEstaciones);

        // 2. Instanciamos el Dashboard como hilo demonio
        DashboardTask dashboard = new DashboardTask(soldadura, cinta, supervisor, numRobots, numAgvs);
        Thread hiloDashboard = new Thread(dashboard);
        hiloDashboard.setDaemon(true);

        // 3. Creamos los arrays de hilos para AGVs y Robots
        Thread[] hilosAgv = new Thread[numAgvs];
        for (int i = 0; i < numAgvs; i++) {
            hilosAgv[i] = new Thread(new AGVTask(i, cinta, supervisor));
        }

        Thread[] hilosRobot = new Thread[numRobots];
        for (int i = 0; i < numRobots; i++) {
            hilosRobot[i] = new Thread(new RobotTask(i, cinta, soldadura, supervisor));
        }

        // 4. Iniciamos todos los hilos con start()
        hiloDashboard.start();
        for (int i = 0; i < numAgvs; i++) {
            hilosAgv[i].start();
        }
        for (int i = 0; i < numRobots; i++) {
            hilosRobot[i].start();
        }

        try {
            // Fase 1: Produccion normal durante 4 segundos
            Thread.sleep(4000);

            // Fase 2: Simulamos alerta de sobrecalentamiento durante 3 segundos
            supervisor.activaAlerta();
            Thread.sleep(3000);
            supervisor.desactivaAlerta();

            // Fase 3: Produccion reanudada durante 4 segundos
            Thread.sleep(4000);

            // 5. Detenemos los hilos con interrupt()
            for (int i = 0; i < numAgvs; i++) {
                hilosAgv[i].interrupt();
            }
            for (int i = 0; i < numRobots; i++) {
                hilosRobot[i].interrupt();
            }
            hiloDashboard.interrupt();

            // 6. Esperamos a que todos los hilos terminen con join()
            for (int i = 0; i < numAgvs; i++) {
                hilosAgv[i].join();
            }
            for (int i = 0; i < numRobots; i++) {
                hilosRobot[i].join();
            }
            hiloDashboard.join();

            // 7. Mostramos el resumen final de la simulacion
            dashboard.mostrarResumenFinal();

        } catch (InterruptedException e) {
            System.out.println("Simulacion principal interrumpida.");
            Thread.currentThread().interrupt();
        } finally {
            sc.close();
        }
    }

    // Lee un numero por teclado o devuelve el valor por defecto si se pulsa Enter
    private static int pedirNumero(Scanner sc, String mensaje, int defecto) {
        System.out.print(mensaje + " [Enter = " + defecto + "]: ");
        String texto = sc.nextLine().trim();
        if (texto.isEmpty()) {
            return defecto;
        }
        try {
            int num = Integer.parseInt(texto);
            return num > 0 ? num : defecto;
        } catch (NumberFormatException e) {
            return defecto;
        }
    }
}
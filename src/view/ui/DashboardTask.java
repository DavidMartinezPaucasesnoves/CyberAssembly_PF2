package view.ui;

import model.concurrency.CentralSoldadura;
import model.concurrency.CintaTransportadora;
import model.concurrency.SupervisorCentral;

// Hilo encargado de mostrar por consola el estado de la planta cada segundo
public class DashboardTask implements Runnable {

    private final CentralSoldadura soldadura;
    private final CintaTransportadora cinta;
    private final SupervisorCentral supervisor;
    private final int numRobots;
    private final int numAgvs;

    public DashboardTask(CentralSoldadura soldadura, CintaTransportadora cinta,
                         SupervisorCentral supervisor, int numRobots, int numAgvs) {
        this.soldadura = soldadura;
        this.cinta = cinta;
        this.supervisor = supervisor;
        this.numRobots = numRobots;
        this.numAgvs = numAgvs;
    }


    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                mostrarEstado();
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
    

    // Imprime el estado actual de todos los recursos y hilos
    public void mostrarEstado() {
        System.out.println("\n====================================================");
        System.out.println("          DASHBOARD PLANTA CYBERASSEMBLY            ");
        System.out.println("====================================================");

        if (supervisor.isAlertaActiva()) {
            System.out.println(" SEGURIDAD PLANTA : [!] ALERTA SOBRECALENTAMIENTO");
        } else {
            System.out.println(" SEGURIDAD PLANTA : [OK] Operativa normal");
        }

        System.out.println(" CINTA BATERIAS   : " + cinta.getBateriasEnCinta() + " / " + cinta.getCapacidad()
                + " (Cargadas: " + cinta.getBateriasCargadas() + " | Usadas: " + cinta.getBateriasRetiradas() + ")");

        int firmando = soldadura.getRobotFirmando();
        String estadoTablero = (firmando == -1) ? "Libre" : "Ocupado por Robot-" + firmando;
        System.out.println(" TABLERO CENTRAL  : " + estadoTablero
                + " (Certificados: " + soldadura.getCertificadosFirmados() + ")");

        System.out.println("----------------------------------------------------");
        System.out.println(" ESTACIONES DE SOLDADURA (Permisos libres: " + soldadura.getPermisosLibres()
                + " | Criticos esperando: " + soldadura.getRobotsCriticosEsperando() + "):");
        for (int i = 0; i < soldadura.getNumEstaciones(); i++) {
            if (soldadura.isEstacionOcupada(i)) {
                System.out.println("   Estacion " + i + ": OCUPADA (Robot-" + soldadura.getRobotEnEstacion(i) + ")");
            } else {
                System.out.println("   Estacion " + i + ": LIBRE");
            }
        }

        System.out.println("----------------------------------------------------");
        System.out.println(" ROBOTS:");
        for (int i = 0; i < numRobots; i++) {
            System.out.println("   Robot-" + i + ": " + supervisor.getEstadoRobot(i));
        }

        System.out.println(" AGVs:");
        for (int i = 0; i < numAgvs; i++) {
            System.out.println("   AGV-" + i + ": " + supervisor.getEstadoAgv(i));
        }

        System.out.println("----------------------------------------------------");
        System.out.println(" ULTIMO EVENTO: " + supervisor.getUltimoEvento());
        System.out.println("====================================================");
    }

    // Resumen estadistico al finalizar la simulacion
    public void mostrarResumenFinal() {
        System.out.println("\n====================================================");
        System.out.println("             RESUMEN FINAL DE PRODUCCION            ");
        System.out.println("====================================================");
        System.out.println(" Coches soldados y certificados : " + soldadura.getCertificadosFirmados());
        System.out.println(" Baterias cargadas en cinta     : " + cinta.getBateriasCargadas());
        System.out.println(" Baterias retiradas de cinta    : " + cinta.getBateriasRetiradas());
        System.out.println(" Prioridades criticas (<10%)    : " + soldadura.getPrioridadesAtendidas());
        System.out.println(" Alertas de sobrecalentamiento  : " + supervisor.getTotalAlertas());
        System.out.println("====================================================");
    }
}
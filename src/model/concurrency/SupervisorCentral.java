package model.concurrency;

// Monitor central que controla la parada de emergencia por sobrecalentamiento
// y guarda el estado de los robots y AGVs para mostrarlo en el dashboard
public class SupervisorCentral {

    private boolean alertaActiva = false;
    private int totalAlertas = 0;
    private final String[] estadoRobots;
    private final String[] estadoAgvs;
    private String ultimoEvento = "Planta iniciada";

    public SupervisorCentral(int numRobots, int numAgvs) {
        this.estadoRobots = new String[numRobots];
        this.estadoAgvs = new String[numAgvs];

        for (int i = 0; i < numRobots; i++) {
            estadoRobots[i] = "Iniciando";
        }
        for (int i = 0; i < numAgvs; i++) {
            estadoAgvs[i] = "Iniciando";
        }
    }

    // Los robots llaman a este metodo antes de cada operacion.
    // si hay alerta activa, sueltan el monitor y esperan con wait() dentro de un while.
    public synchronized void verificaEstat(int idRobot) throws InterruptedException {
        while (alertaActiva) {
            estadoRobots[idRobot] = "PAUSADO (Alerta)";
            wait();
        }
        estadoRobots[idRobot] = "Activo";
    }



    // Activa la parada de emergencia por sobrecalentamiento
    public synchronized void activaAlerta() {
        alertaActiva = true;
        totalAlertas++;
        ultimoEvento = "ALERTA: Sobrecalentamiento activado";
    }

    // Desactiva la alerta y despierta a todos los robots con notifyAll()
    public synchronized void desactivaAlerta() {
        alertaActiva = false;
        ultimoEvento = "Alerta resuelta: produccion reanudada";
        notifyAll();
    }


    
    public synchronized boolean isAlertaActiva() {
        return alertaActiva;
    }

    public synchronized void setEstadoRobot(int idRobot, String estado) {
        estadoRobots[idRobot] = estado;
    }

    public synchronized String getEstadoRobot(int idRobot) {
        return estadoRobots[idRobot];
    }

    public synchronized void setEstadoAgv(int idAgv, String estado) {
        estadoAgvs[idAgv] = estado;
    }

    public synchronized String getEstadoAgv(int idAgv) {
        return estadoAgvs[idAgv];
    }

    public synchronized void setUltimoEvento(String evento) {
        this.ultimoEvento = evento;
    }

    public synchronized String getUltimoEvento() {
        return ultimoEvento;
    }

    public synchronized int getTotalAlertas() {
        return totalAlertas;
    }
}
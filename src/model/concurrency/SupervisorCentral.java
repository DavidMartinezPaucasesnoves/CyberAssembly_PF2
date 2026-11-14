package model.concurrency;

import java.util.concurrent.atomic.AtomicInteger;

// Monitor central para controlar la parada de emergencia por sobrecalentamiento
// y guardar el estado actual de los robots y AGVs para mostrarlo en el dashboard
public class SupervisorCentral {

    private boolean alertaSobrecalentamiento = false;
    private final AtomicInteger totalAlertas = new AtomicInteger(0);
    private final String[] estadoRobots;
    private final String[] estadoAgvs;
    private String ultimoEvento = "Planta iniciada correctamente";

    
    public SupervisorCentral(int numRobots, int numAgvs) {
        this.estadoRobots = new String[numRobots];
        this.estadoAgvs = new String[numAgvs];

        for (int i = 0; i < numRobots; i++) {
            estadoRobots[i] = "Preparado";
        }
        for (int i = 0; i < numAgvs; i++) {
            estadoAgvs[i] = "Preparado";
        }
    }

    // Si hay alerta activa, los robots se quedan esperando con wait()
    public synchronized void comprobarAlerta(int idRobot) throws InterruptedException {
        while (alertaSobrecalentamiento) {
            estadoRobots[idRobot] = "PAUSADO (Alerta)";
            wait();
        }
    }

    // Activa la emergencia para detener a los robots
    public synchronized void activarAlerta(String motivo) {
        alertaSobrecalentamiento = true;
        totalAlertas.incrementAndGet();
        ultimoEvento = "ALERTA: " + motivo;
    }

    // Desactiva la emergencia y despierta a todos los robots con notifyAll()
    public synchronized void desactivarAlerta() {
        alertaSobrecalentamiento = false;
        ultimoEvento = "Alerta resuelta, robots reanudados";
        notifyAll();
    }

    public synchronized boolean isAlertaActiva() {
        return alertaSobrecalentamiento;
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

    public int getTotalAlertas() {
        return totalAlertas.get();
    }
}
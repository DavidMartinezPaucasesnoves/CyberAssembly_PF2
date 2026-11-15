package tasks;

import java.util.Random;
import model.concurrency.CentralSoldadura;
import model.concurrency.CintaTransportadora;
import model.concurrency.SupervisorCentral;

// Hilo consumidor que extrae baterias, suelda coches y firma certificados
public class RobotTask implements Runnable {

    private final int id;
    private final CintaTransportadora cinta;
    private final CentralSoldadura soldadura;
    private final SupervisorCentral supervisor;
    private int nivelBateria;
    private final Random random = new Random();

    public RobotTask(int id, CintaTransportadora cinta, CentralSoldadura soldadura, SupervisorCentral supervisor) {
        this.id = id;
        this.cinta = cinta;
        this.soldadura = soldadura;
        this.supervisor = supervisor;
        // Inicia cada robot con un nivel de bateria entre 30% y 100%
        this.nivelBateria = 30 + random.nextInt(71);
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {


                // 1. Comprobar si hay alerta de sobrecalentamiento
                supervisor.verificaEstat(id);

                // 2. extraer una bateria de la cinta transportadora
                supervisor.setEstadoRobot(id, "Esperando bateria (Bat: " + nivelBateria + "%)");
                String bateria = cinta.extreuBateria(id);
                supervisor.setUltimoEvento("Robot-" + id + " coge " + bateria);

                // 3. Solicitar estacion de soldadura (con prioridad si bateria < 10%)
                supervisor.verificaEstat(id);
                if (nivelBateria < 10) {
                    supervisor.setEstadoRobot(id, "PRIORIDAD CRITICA (Bat: " + nivelBateria + "%)");
                } else {
                    supervisor.setEstadoRobot(id, "Esperando estacion (Bat: " + nivelBateria + "%)");
                }

                int numEstacion = soldadura.agafa(id, nivelBateria);
                try {
                    supervisor.setEstadoRobot(id, "Soldando en Est." + numEstacion + " (Bat: " + nivelBateria + "%)");
                    supervisor.setUltimoEvento("Robot-" + id + " suelda en Estacion " + numEstacion);
                    Thread.sleep(600 + random.nextInt(401));
                } finally {
                    soldadura.allibera(id, numEstacion);
                }

                // 4. Firmar el certificado de calidad en el tablero central
                supervisor.verificaEstat(id);
                supervisor.setEstadoRobot(id, "Firmando certificado");
                soldadura.registreCentral(id);
                supervisor.setUltimoEvento("Robot-" + id + " firma certificado en tablero central");

                // 5. Consumo de bateria tras completar el ciclo (si baja de 10%, recarga despues de usarlo)
                if (nivelBateria < 10) {
                    nivelBateria = 100;
                } else {
                    nivelBateria -= (25 + random.nextInt(15));
                    if (nivelBateria < 0) {
                        nivelBateria = 5;
                    }
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        supervisor.setEstadoRobot(id, "Detenido");
    }
}


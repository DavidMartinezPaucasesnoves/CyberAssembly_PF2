package tasks;

import java.util.Random;
import model.concurrency.CintaTransportadora;
import model.concurrency.SupervisorCentral;

// Hilo productor que carga paquetes de baterias en la cinta transportadora
public class AGVTask implements Runnable {

    private final int id;
    private int comptadorBateria = 0;
    private final CintaTransportadora cinta;
    private final SupervisorCentral supervisor;
    private final Random random = new Random();

    public AGVTask(int id, CintaTransportadora cinta, SupervisorCentral supervisor) {
        this.id = id;
        this.cinta = cinta;
        this.supervisor = supervisor;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                supervisor.setEstadoAgv(id, "Cargando en cinta");
                cinta.afegirBateria(id, comptadorBateria);
                supervisor.setUltimoEvento("AGV-" + id + " deja bateria #" + comptadorBateria);
                comptadorBateria++;

                supervisor.setEstadoAgv(id, "Transportando");
                // Tiempo aleatorio entre 500 y 900 ms
                Thread.sleep(500 + random.nextInt(401));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            supervisor.setEstadoAgv(id, "Detenido");
        }
    }
}
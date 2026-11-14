package model.concurrency;

// Buffer compartido entre los AGVs (productores) y los Robots (consumidores)
// Implementado como monitor nativo con synchronized, wait() y notifyAll()
public class CintaTransportadora {

    private final String[] buffer;
    private int inicio = 0;
    private int fin = 0;
    private int contador = 0;

    private int bateriasCargadas = 0;
    private int bateriasRetiradas = 0;

    public CintaTransportadora(int capacidad) {
        this.buffer = new String[capacidad];
    }

    // Los AGVs llaman a este metodo para dejar una bateria en la cinta
    public synchronized void afegirBateria(int idAgv, int numBateria) throws InterruptedException {
        // Si la cinta esta llena, el AGV libera el monitor y espera
        while (contador == buffer.length) {
            wait();
        }

        String bateria = "BAT-" + idAgv + "-" + numBateria;
        buffer[fin] = bateria;
        fin = (fin + 1) % buffer.length;
        contador++;
        bateriasCargadas++;

        // Avisamos a los robots de que hay una bateria disponible
        notifyAll();
    }

    // Los Robots llaman a este metodo para coger una bateria de la cinta
    public synchronized String extreuBateria(int idRobot) throws InterruptedException {
        // Si la cinta esta vacia, el robot libera el monitor y espera
        while (contador == 0) {
            wait();
        }

        String bateria = buffer[inicio];
        buffer[inicio] = null;
        inicio = (inicio + 1) % buffer.length;
        contador--;
        bateriasRetiradas++;

        // Avisamos a los AGVs de que hay un hueco libre en la cinta
        notifyAll();
        return bateria;
    }


    
    public synchronized int getBateriasEnCinta() {
        return contador;
    }

    public int getCapacidad() {
        return buffer.length;
    }

    public synchronized int getBateriasCargadas() {
        return bateriasCargadas;
    }

    public synchronized int getBateriasRetiradas() {
        return bateriasRetiradas;
    }
}
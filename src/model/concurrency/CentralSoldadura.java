package model.concurrency;

import java.util.concurrent.Semaphore;

// Recurso compartido para las estaciones de soldadura, el tablero central
// y la prioridad de acceso para robots con bateria critica (< 10%)
public class CentralSoldadura {

    // semaforo contador para controlar el aforo de las N estaciones
    private final Semaphore semaforoEstaciones;
    private final boolean[] estacionOcupada;
    private final int[] robotEnEstacion;

    // variables para la prioridad por bateria critica (< 10%)
    private int estacionesLibres;
    private int robotsCriticosEsperando = 0;
    private int prioridadesAtendidas = 0;

    // semaforo binario (mutex) para el tablero de registro central
    private final Semaphore mutexTablero = new Semaphore(1, true);
    private int certificadosFirmados = 0;
    private int robotFirmando = -1;

    
    public CentralSoldadura(int numEstaciones) {
        this.semaforoEstaciones = new Semaphore(numEstaciones, true);
        this.estacionOcupada = new boolean[numEstaciones];
        this.robotEnEstacion = new int[numEstaciones];
        this.estacionesLibres = numEstaciones;

        for (int i = 0; i < numEstaciones; i++) {
            estacionOcupada[i] = false;
            robotEnEstacion[i] = -1;
        }
    }

    // el robot pide estacion respetando la prioridad por bateria critica
    public int agafa(int idRobot, int nivelBateria) throws InterruptedException {
        esperarTurnoPrioridad(nivelBateria);
        try {
            semaforoEstaciones.acquire();
        } catch (InterruptedException e) {
            devolverCupoPrioridad();
            throw e;
        }
        return asignarEstacion(idRobot);
    }

    // Cola de prioridad sincronizada: los robots con < 10% de bateria pasan antes
    private synchronized void esperarTurnoPrioridad(int nivelBateria) throws InterruptedException {
        boolean esCritico = nivelBateria < 10;

        if (esCritico) {
            robotsCriticosEsperando++;
        }

        try {
            if (esCritico) {
                while (estacionesLibres == 0) {
                    wait();
                }
                prioridadesAtendidas++;
            } else {
                while (estacionesLibres == 0 || robotsCriticosEsperando > 0) {
                    wait();
                }
            }
            estacionesLibres--;
        } finally {
            if (esCritico) {
                robotsCriticosEsperando--;
            }
        }
    }

    private synchronized void devolverCupoPrioridad() {
        estacionesLibres++;
        notifyAll();
    }

    // Exclusion mutua rapida para asignar un indice libre del array sin colisiones
    private synchronized int asignarEstacion(int idRobot) {
        for (int i = 0; i < estacionOcupada.length; i++) {
            if (!estacionOcupada[i]) {
                estacionOcupada[i] = true;
                robotEnEstacion[i] = idRobot;
                return i;
            }
        }
        return -1;
    }

    // El robot sale de la estacion, devuelve el permiso y avisa a los que esperan
    public void allibera(int idRobot, int numEstacion) {
        semaforoEstaciones.release();
        liberarEstacionArray(numEstacion);
    }

    private synchronized void liberarEstacionArray(int numEstacion) {
        estacionOcupada[numEstacion] = false;
        robotEnEstacion[numEstacion] = -1;
        estacionesLibres++;
        notifyAll();
    }


    // Firma el certificado de calidad en exclusion mutua estricta (Semaphore(1))
    public void registreCentral(int idRobot) throws InterruptedException {
        mutexTablero.acquire();
        try {
            setRobotFirmando(idRobot);
            Thread.sleep(300);
            incrementarCertificados();
        } finally {
            setRobotFirmando(-1);
            mutexTablero.release();
        }
    }



    private synchronized void setRobotFirmando(int idRobot) {
        this.robotFirmando = idRobot;
    }

    private synchronized void incrementarCertificados() {
        this.certificadosFirmados++;
    }

    public synchronized boolean isEstacionOcupada(int i) {
        return estacionOcupada[i];
    }

    public synchronized int getRobotEnEstacion(int i) {
        return robotEnEstacion[i];
    }

    public int getNumEstaciones() {
        return estacionOcupada.length;
    }

    public int getPermisosLibres() {
        return semaforoEstaciones.availablePermits();
    }

    public synchronized int getRobotsCriticosEsperando() {
        return robotsCriticosEsperando;
    }

    public synchronized int getRobotFirmando() {
        return robotFirmando;
    }

    public synchronized int getCertificadosFirmados() {
        return certificadosFirmados;
    }

    public synchronized int getPrioridadesAtendidas() {
        return prioridadesAtendidas;
    }
}

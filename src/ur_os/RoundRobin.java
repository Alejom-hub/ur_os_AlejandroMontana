package ur_os;

/**
 * @author prestamour
 */
public class RoundRobin extends Scheduler {

    int q;
    int cont;
    boolean multiqueue;

    RoundRobin(OS os) {
        super(os);
        q = 4; // Quantum por defecto
        cont = 0;
    }

    RoundRobin(OS os, int q) {
        this(os);
        this.q = q;
    }

    RoundRobin(OS os, int q, boolean multiqueue) {
        this(os);
        this.q = q;
        this.multiqueue = multiqueue;
    }

    void resetCounter() {
        cont = 0;
    }

    @Override
    public void getNext(boolean cpuEmpty) {
        if (cpuEmpty) {
            if (!processes.isEmpty()) {
                Process next = processes.poll();
                resetCounter();
                os.interrupt(InterruptType.SCHEDULER_RQ_TO_CPU, next);
            }
        } else {
            cont++;
            if (cont >= q) {
                resetCounter();
                if (!processes.isEmpty()) {
                    os.interrupt(InterruptType.SCHEDULER_CPU_TO_RQ, null);
                    Process next = processes.poll();
                    os.interrupt(InterruptType.SCHEDULER_RQ_TO_CPU, next);
                }
            }
        }
    }

    @Override
    public void newProcess(boolean cpuEmpty) {} // No preventivo en este evento

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {} // No preventivo en este evento
}
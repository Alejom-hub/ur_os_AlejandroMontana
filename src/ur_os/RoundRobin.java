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
                addContextSwitch();
                markFirstExecution(next);
                os.interrupt(InterruptType.SCHEDULER_RQ_TO_CPU, next);
            }
        } else {
            cont++;
            if (cont >= q) {
                resetCounter();
                // Al expirar el quantum, se devuelve el proceso en CPU a la ReadyQueue
                os.interrupt(InterruptType.SCHEDULER_CPU_TO_RQ, null);
                
                // Si esta misma subcola tiene otro proceso esperando, se asigna a la CPU de inmediato
                if (!processes.isEmpty()) {
                    Process next = processes.poll();
                    addContextSwitch();
                    markFirstExecution(next);
                    os.interrupt(InterruptType.SCHEDULER_RQ_TO_CPU, next);
                }
            }
        }
    }

    @Override
    public void newProcess(boolean cpuEmpty) {} // No preventivo en este evento

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {} // No preventivo en este evento

    private void markFirstExecution(Process process) {
        if (process != null && process.getFirstExecutionTime() == -1) {
            process.setFirstExecutionTime(os.system.getTime());
        }
    }
}
package ur_os;

public class FAIR extends Scheduler {

    private final int baseQuantum;
    private int cont;

    public FAIR(OS os) {
        this(os, 4);
    }

    public FAIR(OS os, int baseQuantum) {
        super(os);
        this.baseQuantum = Math.max(1, baseQuantum);
        this.cont = 0;
    }

    private int getQuantum(Process p) {
        return Math.max(1, baseQuantum - p.getPriority());
    }

    @Override
    public void newProcess(boolean cpuEmpty) {
    }

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {
    }

    @Override
    public void getNext(boolean cpuEmpty) {
        if (cpuEmpty) {
            dispatchNext();
        } else {
            Process running = os.getProcessInCPU();
            cont++;
            if (cont >= getQuantum(running) && !processes.isEmpty()) {
                os.interrupt(InterruptType.SCHEDULER_CPU_TO_RQ, null);
                dispatchNext();
            }
        }
    }

    private void dispatchNext() {
        if (!processes.isEmpty()) {
            Process next = processes.poll();
            cont = 0;
            addContextSwitch();
            markFirstExecution(next);
            os.interrupt(InterruptType.SCHEDULER_RQ_TO_CPU, next);
        }
    }

    private void markFirstExecution(Process process) {
        if (process.getFirstExecutionTime() == -1) {
            process.setFirstExecutionTime(os.system.getTime());
        }
    }
}
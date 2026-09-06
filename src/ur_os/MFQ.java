package ur_os;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Multilevel Feedback Queue (MFQ)
 */
public class MFQ extends Scheduler {

    int currentScheduler;
    private ArrayList<Scheduler> schedulers;

    public MFQ(OS os) {
        super(os);
        currentScheduler = -1;
        schedulers = new ArrayList<>();
    }

    public MFQ(OS os, Scheduler... s) {
        this(os);
        schedulers.addAll(Arrays.asList(s));
        if (s.length > 0) {
            currentScheduler = 0;
        }
    }

    @Override
    public void addProcess(Process p) {
        int priority = p.getPriority();

        if (p.getState() == ProcessState.NEW) {
            // Regla 1: Todo proceso nuevo inicia en la cola de máxima prioridad (Cola 0)
            priority = 0;
            p.setPriority(priority);
            newProcess(os.isCPUEmpty());

        } else if (p.getState() == ProcessState.IO) {
            // Regla 2: Si regresa de I/O, conserva su prioridad actual (premio a proceso interactivo)
            IOReturningProcess(os.isCPUEmpty());

        } else if (p.getState() == ProcessState.CPU) {
            // Regla 3: Si viene de ser expulsado de la CPU por agotar su quantum -> DEGRADACIÓN (Castigo)
            if (priority < schedulers.size() - 1) {
                priority++;
                p.setPriority(priority);
            }
            checkPreemption();
        }

        p.setState(ProcessState.READY);

        // Añadir el proceso a la subcola asignada a su prioridad actual
        if (priority >= 0 && priority < schedulers.size()) {
            schedulers.get(priority).addProcess(p);
        } else {
            schedulers.get(schedulers.size() - 1).addProcess(p);
        }
    }

    private int findHighestPriorityScheduler() {
        for (int i = 0; i < schedulers.size(); i++) {
            if (!schedulers.get(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void newProcess(boolean cpuEmpty) {
        checkPreemption();
    }

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {
        checkPreemption();
    }

    private void checkPreemption() {
        if (!os.isCPUEmpty()) {
            Process cpuProcess = os.getProcessInCPU();
            if (cpuProcess != null) {
                int currentPriority = cpuProcess.getPriority();
                int highestPriorityAvailable = findHighestPriorityScheduler();

                // Si un proceso llega a una cola de MAYOR prioridad que el que está ejecutándose en la CPU
                if (highestPriorityAvailable != -1 && highestPriorityAvailable < currentPriority) {
                    os.interrupt(InterruptType.SCHEDULER_CPU_TO_RQ, null);
                }
            }
        }
    }

    @Override
    public void getNext(boolean cpuEmpty) {
        int highestAvailable = findHighestPriorityScheduler();

        if (cpuEmpty) {
            if (highestAvailable != -1) {
                currentScheduler = highestAvailable;
                schedulers.get(currentScheduler).getNext(true);
            }
        } else {
            checkPreemption();

            if (!os.isCPUEmpty()) {
                Process cpuProcess = os.getProcessInCPU();
                if (cpuProcess != null) {
                    int activePriority = cpuProcess.getPriority();
                    if (activePriority >= 0 && activePriority < schedulers.size()) {
                        schedulers.get(activePriority).getNext(false);
                    }
                }
            }
        }
    }

    @Override
    public boolean isEmpty() {
        for (Scheduler s : schedulers) {
            if (!s.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Process removeProcess(Process p) {
        int priority = p.getPriority();
        if (priority >= 0 && priority < schedulers.size()) {
            return schedulers.get(priority).removeProcess(p);
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < schedulers.size(); i++) {
            sb.append("Queue ").append(i).append(":\n");
            sb.append(schedulers.get(i).toString());
        }
        return sb.toString();
    }
}
package ur_os;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
 * @author prestamour
 */
public class PriorityQueue extends Scheduler {

    int currentScheduler;

    private ArrayList<Scheduler> schedulers;

    PriorityQueue(OS os) {
        super(os);
        currentScheduler = -1;
        schedulers = new ArrayList();
    }

    PriorityQueue(OS os, Scheduler... s) {
        this(os);
        schedulers.addAll(Arrays.asList(s));
        if (s.length > 0) {
            currentScheduler = 0;
        }
    }

    @Override
    public void addProcess(Process p) {
        if (p.getState() == ProcessState.NEW) {
            newProcess(os.isCPUEmpty());
        } else if (p.getState() == ProcessState.IO) {
            IOReturningProcess(os.isCPUEmpty());
        }

        p.setState(ProcessState.READY);

        int queueIndex = p.getPriority();
        schedulers.get(queueIndex).addProcess(p);
    }

    void defineCurrentScheduler() {
        //Se usa SOLO cuando la CPU está vacía: busca la primera cola (mayor prioridad) con procesos esperando
        for (int i = 0; i < schedulers.size(); i++) {
            if (!schedulers.get(i).isEmpty()) {
                currentScheduler = i;
                return;
            }
        }
        currentScheduler = -1;
    }

    private int findHigherPriorityWaiting() {
        //Busca si existe una cola de MAYOR prioridad (índice menor) que 'currentScheduler'
        //con al menos un proceso esperando en su lista interna
        for (int i = 0; i < currentScheduler; i++) {
            if (!schedulers.get(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void getNext(boolean cpuEmpty) {
        if (cpuEmpty) {
            //CPU libre: se elige la cola de mayor prioridad con procesos esperando
            defineCurrentScheduler();
            if (currentScheduler == -1) {
                return;
            }
            schedulers.get(currentScheduler).getNext(true);
        } else {
            //CPU ocupada: currentScheduler ya identifica la cola dueña del proceso en ejecución.
            //Solo se cambia si aparece una cola de mayor prioridad con procesos esperando.
            int higher = findHigherPriorityWaiting();

            if (higher != -1) {
                //Preemption: se expulsa el proceso actual (conserva su ráfaga restante) y se
                //entrega la CPU a la cola de mayor prioridad
                os.interrupt(InterruptType.SCHEDULER_CPU_TO_RQ, null);
                currentScheduler = higher;
                schedulers.get(currentScheduler).getNext(true);
            } else {
                //Ninguna cola de mayor prioridad tiene procesos esperando:
                //se delega el control del quantum a la propia cola activa
                schedulers.get(currentScheduler).getNext(false);
            }
        }
    }

    @Override
    public void newProcess(boolean cpuEmpty) {
    } //Non-preemptive in this event

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {
    } //Non-preemptive in this event

}
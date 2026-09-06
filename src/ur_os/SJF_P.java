package ur_os;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author prestamour
 */
public class SJF_P extends Scheduler {

    SJF_P(OS os) {
        super(os);
    }

    @Override
    public void newProcess(boolean cpuEmpty) {
        // When a NEW process enters the queue, process in CPU, if any, is extracted to compete with the rest
        if (!cpuEmpty) {
            os.interrupt(InterruptType.SCHEDULER_CPU_TO_RQ, null);
        }
    } 

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {
        // When a process return from IO and enters the queue, process in CPU, if any, is extracted to compete with the rest
        if (!cpuEmpty) {
            os.interrupt(InterruptType.SCHEDULER_CPU_TO_RQ, null);
        }
    } 
   
    @Override
    public void getNext(boolean cpuEmpty) {
        // Insert code here
        if (!processes.isEmpty() && cpuEmpty) {
            Process shortest = processes.get(0);

            for (Process p : processes) {
                if (p.getRemainingTimeInCurrentBurst() < shortest.getRemainingTimeInCurrentBurst()) {
                    shortest = p;
                } else if (p.getRemainingTimeInCurrentBurst() == shortest.getRemainingTimeInCurrentBurst()) {
                    shortest = tieBreaker(shortest, p);
                }
            }

            processes.remove(shortest);
            addContextSwitch();
            markFirstExecution(shortest);
            os.interrupt(InterruptType.SCHEDULER_RQ_TO_CPU, shortest);
        }
    }

    private void markFirstExecution(Process process) {
        if (process != null && process.getFirstExecutionTime() == -1) {
            process.setFirstExecutionTime(os.system.getTime());
        }
    }
}
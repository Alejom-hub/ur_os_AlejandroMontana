package ur_os;

/**
 * @author prestamour
 */
public class SJF_P extends Scheduler {

    SJF_P(OS os) {
        super(os);
    }

    @Override
    public void newProcess(boolean cpuEmpty) {
        if (!cpuEmpty) {
            os.interrupt(InterruptType.SCHEDULER_CPU_TO_RQ, null);
        }
    } 

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {
        if (!cpuEmpty) {
            os.interrupt(InterruptType.SCHEDULER_CPU_TO_RQ, null);
        }
    } 

    @Override
    public void getNext(boolean cpuEmpty) {
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
        if (process.getFirstExecutionTime() == -1) {
            process.setFirstExecutionTime(os.system.getTime());
        }
    }
}
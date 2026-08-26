package ur_os;

public class SJF_NP extends Scheduler {

    SJF_NP(OS os) {
        super(os);
    }

    @Override
    public void getNext(boolean cpuEmpty) {
        if (!processes.isEmpty() && cpuEmpty) {
            Process shortest = processes.get(0);

            for (int i = 1; i < processes.size(); i++) {
                Process current = processes.get(i);
                int shortestRemaining = shortest.getRemainingTimeInCurrentBurst();
                int currentRemaining = current.getRemainingTimeInCurrentBurst();

                if (currentRemaining < shortestRemaining) {
                    shortest = current;
                } else if (currentRemaining == shortestRemaining) {
                    shortest = tieBreaker(shortest, current);
                }
            }

            processes.remove(shortest);
            os.interrupt(InterruptType.SCHEDULER_RQ_TO_CPU, shortest);
        }
    }

    @Override
    public void newProcess(boolean cpuEmpty) {} // Non-preemptive

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {} // Non-preemptive
}
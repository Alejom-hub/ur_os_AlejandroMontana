package ur_os;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Multilevel Feedback Queue (MFQ)
 */
public class MFQ extends Scheduler {

    int currentScheduler;
    private ArrayList<Scheduler> schedulers;
    private HashMap<Integer, Integer> processLevels;
    private int activeCycles;
    private boolean quantumExpired;

    public MFQ(OS os) {
        super(os);
        currentScheduler = -1;
        schedulers = new ArrayList<>();
        processLevels = new HashMap<>();
        activeCycles = 0;
        quantumExpired = false;
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
        if (schedulers.isEmpty()) {
            return;
        }

        int level;
        if (p.getState() == ProcessState.NEW) {
            level = 0;
        } else if (p.getState() == ProcessState.IO) {
            level = 0;
        } else {
            level = processLevels.getOrDefault(p.getPid(), 0);
            if (p.getState() == ProcessState.CPU && quantumExpired) {
                level = Math.min(level + 1, schedulers.size() - 1);
            }
        }

        processLevels.put(p.getPid(), level);
        p.setState(ProcessState.READY);
        schedulers.get(level).processes.add(p);
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
    public void newProcess(boolean cpuEmpty) {}

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {}

    @Override
    public void getNext(boolean cpuEmpty) {
        if (cpuEmpty) {
            dispatchNext();
            return;
        }

        if (currentScheduler < 0 || currentScheduler >= schedulers.size()) {
            dispatchNext();
            return;
        }

        int quantum = getQuantum(currentScheduler);
        if (quantum == Integer.MAX_VALUE) {
            return;
        }

        activeCycles++;
        if (activeCycles >= quantum) {
            quantumExpired = true;
            os.interrupt(InterruptType.SCHEDULER_CPU_TO_RQ, null);
            quantumExpired = false;
            activeCycles = 0;
            dispatchNext();
        }
    }

    private void dispatchNext() {
        int nextLevel = findHighestPriorityScheduler();
        if (nextLevel == -1) {
            currentScheduler = -1;
            activeCycles = 0;
            return;
        }

        currentScheduler = nextLevel;
        activeCycles = 0;
        Process next = schedulers.get(nextLevel).processes.poll();
        os.interrupt(InterruptType.SCHEDULER_RQ_TO_CPU, next);
    }

    private int getQuantum(int level) {
        Scheduler scheduler = schedulers.get(level);
        if (scheduler instanceof RoundRobin) {
            return ((RoundRobin) scheduler).q;
        }
        return Integer.MAX_VALUE;
    }

    private int findHigherPriorityWaiting() {
        for (int i = 0; i < currentScheduler; i++) {
            if (!schedulers.get(i).isEmpty()) {
                return i;
            }
        }
        return -1;
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
        Integer level = processLevels.get(p.getPid());
        if (level != null && level >= 0 && level < schedulers.size()) {
            return schedulers.get(level).removeProcess(p);
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
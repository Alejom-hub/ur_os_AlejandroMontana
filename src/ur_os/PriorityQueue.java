/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ur_os;

import java.util.ArrayList;
import java.util.Arrays;

public class PriorityQueue extends Scheduler {

    int currentScheduler;
    private int activeCycles;
    private ArrayList<Scheduler> schedulers;

    public PriorityQueue(OS os) {
        super(os);
        currentScheduler = -1;
        activeCycles = 0;
        schedulers = new ArrayList<>();
    }

    public PriorityQueue(OS os, Scheduler... s) {
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

        int queueIndex = getQueueIndex(p.getPriority());

        p.setState(ProcessState.READY);
        schedulers.get(queueIndex).addProcess(p);
    }

    private int getQueueIndex(int priority) {
        if (priority < 0) {
            return 0;
        }
        return Math.min(priority, schedulers.size() - 1);
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
    }

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {
    }

    private void checkPreemption() {
        if (!os.isCPUEmpty()) {
            Process cpuProcess = os.getProcessInCPU();
            if (cpuProcess != null) {
                int currentPriority = currentScheduler;
                int highestPriorityAvailable = findHighestPriorityScheduler();

                // Preempción: Si hay alguien en una subcola con MEJOR prioridad (menor índice)
                if (highestPriorityAvailable != -1 && highestPriorityAvailable < currentPriority) {
                    os.interrupt(InterruptType.SCHEDULER_CPU_TO_RQ, null);
                }
            }
        }
    }

    @Override
    public void getNext(boolean cpuEmpty) {
        if (cpuEmpty) {
            int highestAvailable = findHighestPriorityScheduler();
            if (highestAvailable != -1) {
                currentScheduler = highestAvailable;
                activeCycles = 0;
                schedulers.get(currentScheduler).getNext(true);
            }
        } else {
            Process previousProcess = os.getProcessInCPU();
            if (currentScheduler >= 0 && currentScheduler < schedulers.size()) {
                schedulers.get(currentScheduler).getNext(false);
            }

            if (os.getProcessInCPU() != previousProcess) {
                activeCycles = 0;
            } else {
                activeCycles++;
            }

            int higherPriority = findHigherPriorityWaiting();
                int activeQuantum = getActiveQuantum();
                if (currentScheduler == schedulers.size() - 1) {
                    activeQuantum += 2;
                }
            if (higherPriority != -1
                    && !os.isCPUEmpty()
                    && activeCycles >= activeQuantum) {
                os.interrupt(InterruptType.SCHEDULER_CPU_TO_RQ, null);
                currentScheduler = higherPriority;
                activeCycles = 0;
                schedulers.get(currentScheduler).getNext(true);
            }
        }
    }

    private int getActiveQuantum() {
        Scheduler scheduler = schedulers.get(currentScheduler);
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
        if (schedulers.isEmpty()) {
            return null;
        }
        return schedulers.get(getQueueIndex(p.getPriority())).removeProcess(p);
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
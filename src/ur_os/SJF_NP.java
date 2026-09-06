package ur_os;

public class SJF_NP extends Scheduler {

    SJF_NP(OS os) {
        super(os);
    }

    @Override
    public void newProcess(boolean cpuEmpty) {
        // SJF Non-Preemptive no interrumpe
        // el proceso que ya está en CPU.
    }

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {
        // No se hace preemption cuando un proceso
        // regresa de I/O.
    }

    @Override
    public void getNext(boolean cpuEmpty) {

        // Solo seleccionamos un proceso si
        // la CPU está vacía.
        if (cpuEmpty && !processes.isEmpty()) {

            Process shortest = processes.get(0);

            // Buscamos el proceso con menor
            // tiempo restante del burst actual.
            for (int i = 1; i < processes.size(); i++) {

                Process current = processes.get(i);

                if (current.getRemainingTimeInCurrentBurst()
                        < shortest.getRemainingTimeInCurrentBurst()) {

                    shortest = current;

                } else if (
                    current.getRemainingTimeInCurrentBurst()
                        == shortest.getRemainingTimeInCurrentBurst()
                ) {

                    shortest = tieBreaker(shortest, current);
                }
            }

            // Quitamos el proceso de Ready Queue.
            processes.remove(shortest);

            // Lo enviamos a la CPU.
            addContextSwitch();
            markFirstExecution(shortest);
            os.interrupt(
                    InterruptType.SCHEDULER_RQ_TO_CPU,
                    shortest
            );
        }
    }

    private void markFirstExecution(Process process) {
        if (process.getFirstExecutionTime() == -1) {
            process.setFirstExecutionTime(os.system.getTime());
        }
    }
}
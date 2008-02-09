package org.jmock.lib.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;


public class SynchronousExecutor implements Executor {
    private List<Runnable> commands = new ArrayList<Runnable>();

    public SynchronousExecutor() {
        super();
    }

    /**
     * Returns whether this executor is idle -- has no pending background tasks waiting to be run.
     * 
     * @return true if there are no background tasks to be run, false otherwise.
     * @see #runPendingCommands()
     * @see #runUntilIdle()
     */
    public boolean isIdle() {
        return commands.isEmpty();
    }

    /**
     * Runs all commands that are currently pending. If those commands also
     * schedule commands for execution, the scheduled commands will <em>not</em>
     * be executed until the next call to {@link #runPendingCommands()} or
     * {@link #runUntilIdle()}.
     */
    public void runPendingCommands() {
        List<Runnable> commandsToRun = commands;
        commands = new ArrayList<Runnable>();
    
        for (Runnable command: commandsToRun) {
            command.run();
        }
    }

    /**
     * Runs executed commands until there are no commands pending execution, but
     * does not tick time forward.
     */
    public void runUntilIdle() {
        while (!isIdle()) {
            runPendingCommands();
        }
    }

    public void execute(Runnable command) {
        commands.add(command);
    }

}
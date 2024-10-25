package org.gmarques.util.actions;

import java.util.List;

public class WorkflowExecutor {
    private final List<Action> actions;

    public WorkflowExecutor(List<Action> actions) {
        this.actions = actions;
    }

    public void execute() throws Exception {
        for (Action action : actions) {
            action.execute();
            Thread.sleep(500); // Espera meio segundo entre as ações
        }
    }
}
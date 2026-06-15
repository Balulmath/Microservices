package com.example.treasury.messaging;

import com.example.treasury.service.WorkflowProcessor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Profile("!kafka & !rabbit")
public class LocalWorkflowEventListener {

    private final WorkflowProcessor processor;

    public LocalWorkflowEventListener(WorkflowProcessor processor) {
        this.processor = processor;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onWorkflowEvent(WorkflowEvent event) {
        processor.process(event);
    }
}

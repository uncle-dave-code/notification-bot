package com.uncledavecode.notificator.ui.components;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.EventData;
import com.vaadin.flow.component.dialog.Dialog;

public class AlertEvent extends ComponentEvent<Dialog> {
    private final String message;

    public AlertEvent(Dialog source, boolean fromClient,
            @EventData("event.message") String message) {
        super(source, fromClient);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}

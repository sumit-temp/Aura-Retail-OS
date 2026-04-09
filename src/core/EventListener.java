package core;

import core.events.SystemEvent;

public interface EventListener {
    void onEvent(SystemEvent event);
}

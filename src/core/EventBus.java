package core;

import core.events.SystemEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventBus {
    private static EventBus instance;
    private Map<String, List<EventListener>> listeners;

    private EventBus() {
        listeners = new HashMap<>();
    }

    public static synchronized EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    public void subscribe(String eventType, EventListener listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }

    public void publish(SystemEvent event) {
        System.out.println("[EventBus] Publishing event: " + event.getEventType());
        List<EventListener> eventListeners = listeners.getOrDefault(event.getEventType(), new ArrayList<>());
        for (EventListener listener : eventListeners) {
            listener.onEvent(event);
        }
    }
}

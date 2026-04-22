package core;

import core.events.SystemEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class EventBus {
    private static EventBus instance;
    private Map<String, List<EventListener>> listeners;
    private final ReentrantLock lock;

    private EventBus() {
        listeners = new HashMap<>();
        this.lock = new ReentrantLock();
    }

    public static synchronized EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }

    public void subscribe(String eventType, EventListener listener) {
        lock.lock();
        try {
            listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
        } finally {
            lock.unlock();
        }
    }

    public void publish(SystemEvent event) {
        System.out.println("[EventBus] Publishing event: " + event.getEventType());
        lock.lock();
        try {
            List<EventListener> eventListeners = new ArrayList<>(
                listeners.getOrDefault(event.getEventType(), new ArrayList<>())
            );
            // Release lock before invoking listeners to avoid deadlock
            lock.unlock();
            for (EventListener listener : eventListeners) {
                listener.onEvent(event);
            }
            lock.lock();
        } finally {
            lock.unlock();
        }
    }
}

package core;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class CentralRegistry {
    private static CentralRegistry instance;
    private Map<String, Object> configuration;
    private final ReentrantLock lock;
    
    private CentralRegistry() {
        configuration = new HashMap<>();
        this.lock = new ReentrantLock();
    }
    
    public static synchronized CentralRegistry getInstance() {
        if (instance == null) {
            instance = new CentralRegistry();
        }
        return instance;
    }
    
    public void setConfig(String key, Object value) {
        lock.lock();
        try {
            configuration.put(key, value);
        } finally {
            lock.unlock();
        }
    }
    
    public Object getConfig(String key) {
        lock.lock();
        try {
            return configuration.get(key);
        } finally {
            lock.unlock();
        }
    }
    
    public boolean hasConfig(String key) {
        lock.lock();
        try {
            return configuration.containsKey(key);
        } finally {
            lock.unlock();
        }
    }
}

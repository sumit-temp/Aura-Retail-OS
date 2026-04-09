package core;

import java.util.HashMap;
import java.util.Map;

public class CentralRegistry {
    private static CentralRegistry instance;
    private Map<String, Object> configuration;
    
    private CentralRegistry() {
        configuration = new HashMap<>();
    }
    
    public static synchronized CentralRegistry getInstance() {
        if (instance == null) {
            instance = new CentralRegistry();
        }
        return instance;
    }
    
    public void setConfig(String key, Object value) {
        configuration.put(key, value);
    }
    
    public Object getConfig(String key) {
        return configuration.get(key);
    }
    
    public boolean hasConfig(String key) {
        return configuration.containsKey(key);
    }
}

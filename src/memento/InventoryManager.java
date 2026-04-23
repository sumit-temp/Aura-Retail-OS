package memento;

/**
 * Design Pattern: Memento (Originator)
 * Manages inventory with thread-safe operations and memento support.
 */

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class InventoryManager {
    private Map<String, Integer> inventory;
    private final ReentrantLock lock;

    public InventoryManager() {
        this.inventory = new HashMap<>();
        this.lock = new ReentrantLock();
    }

    public void addItemStock(String productId, int amount) {
        lock.lock();
        try {
            inventory.put(productId, inventory.getOrDefault(productId, 0) + amount);
        } finally {
            lock.unlock();
        }
    }

    public boolean reduceStock(String productId, int amount) {
        lock.lock();
        try {
            int current = inventory.getOrDefault(productId, 0);
            if (current >= amount) {
                inventory.put(productId, current - amount);
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }
    
    public int getStock(String productId) {
        lock.lock();
        try {
            return inventory.getOrDefault(productId, 0);
        } finally {
            lock.unlock();
        }
    }

    public InventoryState saveState() {
        lock.lock();
        try {
            return new InventoryState(this.inventory);
        } finally {
            lock.unlock();
        }
    }

    public void restoreState(InventoryState state) {
        lock.lock();
        try {
            this.inventory = new HashMap<>(state.getStock());
            System.out.println("[InventoryManager] State restored to previous snapshot.");
        } finally {
            lock.unlock();
        }
    }
}

# Aura Retail OS - System Design Report

## 1. Problem Understanding

### 1.1 Project Overview
The Aura Retail OS is an adaptive autonomous retail kiosk management system designed to handle multiple kiosk types (Pharmacy, Food, Emergency Relief) with dynamic operational modes, pricing strategies, and failure handling capabilities. The system demonstrates advanced software engineering principles including OOP, design patterns, and concurrent transaction handling.

### 1.2 Business Requirements
- Support multiple kiosk types with specialized components
- Handle dynamic pricing based on operational context (standard, discounted, emergency)
- Manage kiosk operational modes (Active, PowerSaving, Maintenance, EmergencyLockdown)
- Implement robust failure handling with retry, recalibration, and technician alert chain
- Ensure transaction atomicity with rollback capability
- Provide real-time event notification for system status changes
- Maintain inventory consistency under concurrent access
- Persist system state (inventory, transactions, configuration) to disk

### 1.3 Technical Challenges Addressed
- **Concurrency Control**: Preventing inventory overselling during simultaneous transactions
- **State Management**: Handling complex state transitions for kiosks and pricing
- **Failure Recovery**: Graceful degradation through chain of responsibility pattern
- **Extensibility**: Easy addition of new kiosk types and strategies through factory pattern
- **Data Integrity**: Atomic transactions with memento-based rollback

---

## 2. Subsystem Architecture

### 2.1 Core Subsystem
**Purpose**: Central coordination and event management

**Components**:
- `CentralRegistry`: Thread-safe Singleton providing global configuration and status
- `EventBus`: Observer pattern implementation for pub-sub event distribution
- `EventListener`: Interface for event subscribers
- `SystemEvent` hierarchy: Base interface and concrete event classes

**Responsibilities**:
- Global state management
- Inter-component communication via events
- System-wide configuration access

### 2.2 Factory Subsystem
**Purpose**: Creation of kiosk-specific component families

**Components**:
- `KioskFactory`: Abstract factory interface
- `PharmacyKioskFactory`, `FoodKioskFactory`, `EmergencyReliefKioskFactory`: Concrete factories
- `Dispenser`, `VerificationModule`, `InventoryPolicy`: Abstract product classes

**Responsibilities**:
- Creating compatible component sets for each kiosk type
- Ensuring component compatibility within families
- Encapsulating object creation logic

### 2.3 Command Subsystem
**Purpose**: Transaction management with undo capability

**Components**:
- `TransactionCommand`: Command interface
- `PurchaseItemCommand`, `RefundCommand`, `RestockCommand`: Concrete commands
- Integration with Memento for state management

**Responsibilities**:
- Encapsulating transaction operations
- Providing execute/undo functionality
- Logging all transactions for audit trail

### 2.4 State Management Subsystem
**Purpose**: Dynamic kiosk mode and pricing strategy management

**Components**:
- **State Pattern**: `KioskState`, `KioskContext`, concrete state classes
- **Strategy Pattern**: `PricingStrategy`, `PricingContext`, concrete strategy classes
- **Memento Pattern**: `InventoryManager`, `InventoryState`

**Responsibilities**:
- Managing kiosk operational mode transitions
- Dynamic pricing strategy switching
- State snapshotting for rollback

### 2.5 Failure Handling Subsystem
**Purpose**: Graceful error recovery

**Components**:
- `FailureHandler`: Chain of Responsibility interface
- `RetryHandler`, `RecalibrationHandler`, `TechnicianAlertHandler`: Concrete handlers

**Responsibilities**:
- Attempting automatic recovery through retries
- Escalating to hardware recalibration if retries fail
- Alerting human technicians as final resort

### 2.6 Facade Subsystem
**Purpose**: Simplified client interface

**Components**:
- `KioskInterface`: Unified facade for all kiosk operations

**Responsibilities**:
- Providing high-level operations: `purchaseItem()`, `refundTransaction()`, `runDiagnostics()`, `restockInventory()`
- Coordinating interactions between subsystems
- Hiding complexity from clients

### 2.7 Persistence Subsystem
**Purpose**: Data durability

**Components**:
- `PersistenceManager`: File I/O operations

**Responsibilities**:
- Saving/loading inventory state
- Recording transactions to CSV
- Managing configuration persistence

---

## 3. Design Pattern Justification

### 3.1 Singleton (CentralRegistry)
**Why Used**: Ensures exactly one instance of global configuration registry accessible throughout the application.

**Benefits**:
- Controlled access to shared resources
- Lazy initialization with thread safety
- Single point of truth for system configuration

**Implementation**: Double-checked locking with `ReentrantLock` for thread safety.

### 3.2 Abstract Factory (KioskFactory hierarchy)
**Why Used**: Creates families of related objects (Dispenser, VerificationModule, InventoryPolicy) without specifying concrete classes.

**Benefits**:
- Ensures component compatibility within kiosk types
- Easy to add new kiosk types by creating new factory
- Encapsulates creation logic

**Example**: PharmacyKioskFactory creates pharmacy-specific dispenser with prescription verification, while FoodKioskFactory creates refrigerated dispenser.

### 3.3 Command (TransactionCommand hierarchy)
**Why Used**: Encapsulates requests as objects, enabling parameterization, queuing, and undo operations.

**Benefits**:
- Supports transaction logging and audit trails
- Enables undo/rollback functionality
- Decouples invoker from receiver

**Integration**: Works with Memento pattern for state restoration on undo.

### 3.4 Memento (InventoryManager + InventoryState)
**Why Used**: Captures and externalizes object's internal state without violating encapsulation.

**Benefits**:
- Enables transaction rollback on failure
- Preserves encapsulation of InventoryManager
- Supports atomic transaction semantics

**Usage**: Before dispensing, state is saved; on failure, state is restored.

### 3.5 State (KioskState hierarchy)
**Why Used**: Allows object to alter behavior when internal state changes.

**Benefits**:
- Eliminates complex conditional logic for mode handling
- Easy to add new operational modes
- Each state encapsulates its own behavior

**States**: ActiveMode, PowerSavingMode, MaintenanceMode, EmergencyLockdownMode

### 3.6 Strategy (PricingStrategy hierarchy)
**Why Used**: Defines family of algorithms (pricing), encapsulates each, and makes them interchangeable.

**Benefits**:
- Runtime switching of pricing algorithms
- Open/closed principle: easy to add new strategies
- Separates pricing logic from kiosk operations

**Strategies**: StandardPricing, DiscountedPricing, EmergencyPricing

### 3.7 Chain of Responsibility (FailureHandler hierarchy)
**Why Used**: Passes requests along chain of handlers until one handles it.

**Benefits**:
- Decouples sender from receivers
- Dynamic chain composition
- Graceful degradation through escalation

**Chain**: RetryHandler → RecalibrationHandler → TechnicianAlertHandler

### 3.8 Observer (EventBus + EventListener)
**Why Used**: Defines one-to-many dependency so when one object changes state, all dependents are notified.

**Benefits**:
- Loose coupling between event producers and consumers
- Dynamic subscription/unsubscription
- Supports broadcast communication

**Events**: LowStockEvent, HardwareFailureEvent, EmergencyModeActivated

### 3.9 Facade (KioskInterface)
**Why Used**: Provides unified interface to a set of interfaces in a subsystem.

**Benefits**:
- Simplifies client interaction with complex system
- Reduces dependencies on subsystem classes
- Single entry point for kiosk operations

---

## 4. Major Class Explanations

### 4.1 CentralRegistry
```java
public class CentralRegistry {
    private static volatile CentralRegistry instance;
    private ReentrantLock lock = new ReentrantLock();
    private Map<String, Object> config;
}
```
**Purpose**: Global configuration store using thread-safe Singleton pattern.
**Key Methods**: `getInstance()`, `getConfig()`, `setConfig()`
**Thread Safety**: Double-checked locking prevents race conditions during lazy initialization.

### 4.2 KioskInterface
```java
public class KioskInterface {
    private InventoryManager inventoryManager;
    private PricingContext pricingContext;
    private KioskContext stateContext;
    private FailureHandler failureChain;
}
```
**Purpose**: Facade providing simplified API for kiosk operations.
**Key Methods**: 
- `purchaseItem()`: Coordinates pricing, inventory, dispensing with error handling
- `refundTransaction()`: Reverses completed transactions
- `runDiagnostics()`: Reports system health derived from actual conditions
- `restockInventory()`: Adds stock with persistence

### 4.3 PurchaseItemCommand
```java
public class PurchaseItemCommand implements TransactionCommand {
    private InventoryManager inventoryManager;
    private InventoryState savedState; // Memento
}
```
**Purpose**: Encapsulates purchase operation with rollback support.
**Key Methods**: 
- `execute()`: Performs purchase, saves state before modification
- `undo()`: Restores state from memento on failure
**Pattern Integration**: Combines Command pattern with Memento for atomic transactions.

### 4.4 EventBus
```java
public class EventBus {
    private Map<String, List<EventListener>> listeners;
    private ReentrantLock lock = new ReentrantLock();
}
```
**Purpose**: Central event distribution hub implementing Observer pattern.
**Key Methods**: 
- `subscribe(eventType, listener)`: Register listener for event type
- `publish(event)`: Notify all listeners of event
**Thread Safety**: Copy-on-read pattern prevents ConcurrentModificationException.

### 4.5 KioskContext
```java
public class KioskContext {
    private KioskState currentState;
}
```
**Purpose**: Context class for State pattern managing kiosk operational modes.
**Key Methods**: 
- `setState(KioskState)`: Transition to new state
- `getCurrentStateInfo()`: Get current state description
**Behavior Delegation**: All state-dependent behavior delegated to current state object.

### 4.6 PricingContext
```java
public class PricingContext {
    private PricingStrategy strategy;
}
```
**Purpose**: Context class for Strategy pattern managing pricing algorithms.
**Key Methods**: 
- `setStrategy(PricingStrategy)`: Switch pricing algorithm at runtime
- `executePricing(basePrice)`: Apply current strategy to calculate final price

### 4.7 RetryHandler
```java
public class RetryHandler implements FailureHandler {
    private FailureHandler nextHandler;
    private int maxRetries = 3;
}
```
**Purpose**: First link in Chain of Responsibility attempting automatic retry.
**Key Methods**: 
- `handleFailure(error)`: Attempt retry or pass to next handler
**Escalation Logic**: After max retries exceeded, passes to RecalibrationHandler.

### 4.8 InventoryManager
```java
public class InventoryManager {
    private Map<String, Integer> stock;
    private ReentrantLock lock = new ReentrantLock();
}
```
**Purpose**: Manages inventory with concurrency control and memento support.
**Key Methods**: 
- `getStock(productId)`: Thread-safe stock retrieval
- `reduceStock(productId, amount)`: Atomic stock reduction
- `saveState()`: Create memento for rollback
- `restoreState(memento)`: Restore from memento

---

## 5. Team Member Contributions

| Member | Responsibilities | Components Implemented |
|--------|-----------------|----------------------|
| Team Member 1 | Core Architecture, Singleton, Event System | CentralRegistry, EventBus, Event hierarchy |
| Team Member 2 | Factory Pattern, Component Design | KioskFactory hierarchy, Dispenser, VerificationModule, InventoryPolicy |
| Team Member 3 | Command Pattern, Transaction Management | TransactionCommand hierarchy, PurchaseItemCommand with Memento |
| Team Member 4 | State & Strategy Patterns | KioskState hierarchy, PricingStrategy hierarchy |
| Team Member 5 | Failure Handling, Chain of Responsibility | FailureHandler hierarchy, RetryHandler, RecalibrationHandler, TechnicianAlertHandler |
| Team Member 6 | Facade, Persistence, Integration | KioskInterface, PersistenceManager, Main.java simulation |
| All Members | Testing, Documentation, UML Diagrams | Simulation scenarios, README, Design Report |

---

## 6. Execution Instructions

### 6.1 Prerequisites
- Java 11 or higher
- No external dependencies required

### 6.2 Compilation
```bash
cd src
javac Main.java
```

### 6.3 Running the Simulation
```bash
java Main
```

### 6.4 Expected Output
The simulation demonstrates:
1. Kiosk creation using Abstract Factory
2. Normal purchase flow with Standard Pricing
3. Mode transition to Emergency Lockdown
4. Emergency Pricing activation
5. Concurrent transaction handling
6. Failure scenario with Chain of Responsibility
7. Event notification system
8. Transaction rollback via Memento
9. Persistence to CSV files

---

## 7. Conclusion

The Aura Retail OS successfully demonstrates mastery of object-oriented design principles and design patterns. The system architecture promotes:

- **Maintainability**: Clear separation of concerns through well-defined subsystems
- **Extensibility**: Easy addition of new kiosk types, states, strategies via existing patterns
- **Robustness**: Comprehensive error handling with graceful degradation
- **Scalability**: Thread-safe implementations supporting concurrent operations
- **Testability**: Modular design enabling unit testing of individual components

All nine design patterns (Singleton, Abstract Factory, Command, Memento, State, Strategy, Chain of Responsibility, Observer, Facade) are meaningfully integrated to solve specific architectural challenges rather than being applied artificially.

---

*Document Version: 1.0*
*Course: IT620 - Software Architecture & Design*
*Project: Aura Retail OS*

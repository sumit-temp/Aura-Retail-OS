# Aura Retail OS

**Course:** Object Oriented Programming (IT620)

Aura Retail OS is a modular Java-based smart-city retail infrastructure system supporting autonomous kiosks (Pharmacy, Food, Emergency Relief). This project fulfills the **Path A: Adaptive Autonomous System** constraints for dynamic decision-making, adaptive behavior, and fault tolerance.

## Architecture & Design Patterns

The platform leverages pure object-oriented principles and 9 professional design patterns to handle extreme hardware variations, unexpected transactional errors, and system emergencies natively without monolithic complexity.

1. **Abstract Factory**  
   Dynamically instantiates kiosk-specific components (`Dispenser`, `VerificationModule`, `InventoryPolicy`).
   
2. **Command Pattern**  
   Models transactions into executable classes (`PurchaseItemCommand`) enforcing isolated state interactions decoupled from hardware mechanisms.
   
3. **Memento Pattern**  
   Responsible for keeping deep-copied inventory snapshots, enabling true atomic transaction behavior. If dispensing hardware falls into a runtime error mid-cycle, the transaction natively rolls back to its exact previous state.
   
4. **Strategy Pattern**  
   Allows the system to swap live price formulas instantly (e.g., `StandardPricing` -> `EmergencyPricing`) depending on external context and global alerts.
   
5. **State Pattern**  
   Maintains kiosk behavioral states Context (`ActiveMode`, `MaintenanceMode`, `EmergencyLockdownMode`), physically preventing or permitting tasks structurally to avoid massive embedded `if/else` checks.
   
6. **Chain of Responsibility**  
   Passes runtime anomalies (such as Hardware errors) through a sequential, decoupled pipeline of handlers (`RetryHandler -> RecalibrationHandler -> TechnicianAlertHandler`).
   
7. **Observer Pattern / EventBus**  
   Serves as a publish/subscribe communication layer. Subsystems broadcast custom `SystemEvent` forms across threads while registered listeners scale and adapt behaviors instantly across modules.
   
8. **Singleton**  
   Coordinates core configuration maps and event-buses homogeneously via the `CentralRegistry` mapping the smart city's scope cleanly.
   
9. **Facade Pattern**  
   Wraps all sprawling modules behind `KioskInterface`, providing external clients a seamless unified interface for restocking and purchasing items cleanly.

## How to Run

Compile and execute the core `Main.java` orchestrator to see the entire simulation in action.

### Using Command Prompt (Windows)
```cmd
cd src
dir /s /b *.java > sources.txt
javac -d ../out @sources.txt
java -cp ../out Main
```

### Simulation Environment Output
Running `Main.java` natively triggers an automated logging demonstration across robust state tests:
* **Hardware Failures**: Forces simulated Dispenser Motor failures triggering atomic inventory rollbacks (Memento) and firing failure logs through City Monitoring chains logically.
* **City-wide Emergency Deployments**: Triggers `EmergencyModeActivated` events scaling operations into strict `EmergencyLockdownMode`, halting excessive purchase queries natively, and triggering 50% discount overrides safely without monolithic adjustments.
* **Concurrency Integrity**: Spawns overlapping execution contexts forcing atomic execution behavior cleanly processing requests efficiently.

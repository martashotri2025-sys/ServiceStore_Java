# Car Service Management System (ServiceStore)

## About the Project

**ServiceStore** is an object-oriented Java application designed to manage the full lifecycle of vehicle repair orders in an auto repair shop (STO). The system models real-world auto service operations including vehicle and client registration, mechanic assignment, diagnostic reporting, repair tracking, pricing calculation with business rules, and order completion.

The architecture follows clean separation of concerns and Domain-Driven Design (DDD) principles, separating domain entities, repository persistence abstractions, dynamic pricing strategies, and application orchestration services.

---

## Features

- **Client & Vehicle Management**:
  - Store and validate client records with unique identifiers (`UUID`).
  - Manage vehicle details (`VIN`, make, model, year of production) with strict boundary validation (production year limits, mandatory fields).
  - Transfer vehicle ownership dynamically.

- **Order Lifecycle & State Machine**:
  - Track order progression through strict, validated lifecycle states:
    `PENDING_DIAGNOSIS` $\to$ `DIAGNOSED` $\to$ `CLIENT_APPROVED` $\to$ `IN_PROGRESS` $\to$ `SUCCESSFULLY_COMPLETED` (or `CANCELED`).
  - Prevent invalid state transitions (e.g., cannot approve an undiagnosed or empty order; cannot finish an order with pending works).
  - Safely cancel/abort active orders while releasing assigned mechanics.

- **Service Work & Mechanics**:
  - Add individual repair work items with separate labor costs and parts costs (`BigDecimal`).
  - Track completion status for each service task.
  - Manage mechanic availability, ensuring a busy mechanic cannot be assigned to multiple concurrent jobs, and releasing mechanics automatically when repairs finish or cancel.

- **Flexible Commercial Pricing Engine (Strategy Pattern)**:
  - Calculate total base costs by aggregating parts and labor across all scheduled tasks.
  - Apply customizable, chainable pricing rules via `PricingRule`:
    - `DiscountRule`: Percentage-based promotional or client discounts.
    - `MinimumCostRule`: Minimum price threshold/floor guarantee.
  - Perform accurate monetary calculations using `BigDecimal` with half-up rounding.

- **Repository Layer**:
  - Decouple business logic from storage mechanisms using the `RepairOrderRepository` interface.
  - In-memory implementation (`InMemoryRepairOrderRepository`) for rapid storage, retrieval, and querying during execution.

- **Service Layer (Orchestration & Validation)**:
  - High-level `CarService` coordinator managing transactional-like operations across repository and domain models with dependency injection.

---

## Technologies & Libraries

- **Java 21**: Modern LTS version utilized for clean language features, strong typing, and enhanced standard library capabilities.
- **Apache Maven**: Build automation and dependency management tool.
- **JUnit 5 (Jupiter)**: Modern testing framework used for 183 automated unit and integration tests.
- **Mockito 5.18**: Mocking framework used for isolating dependencies and verifying service-layer behavior.
- **Project Lombok**: Annotation library (`@Getter`, `@EqualsAndHashCode`, `@ToString`) to eliminate boilerplate code and maintain clean domain models.

---

## Project Structure & Main Classes

```
src/main/java/carService/
├── CarServiceDemo.java             # Application entry point demonstrating end-to-end workflows
├── domain/                         # Domain Models & Business Logic
│   ├── Car.java                    # Vehicle entity with VIN and owner references
│   ├── Client.java                 # Customer model with unique UUID
│   ├── Mechanic.java               # Mechanic entity managing availability state
│   ├── ServiceWork.java            # Single repair task with labor and parts costs
│   ├── OrderStatus.java            # Enum representing strict lifecycle states
│   ├── RepairOrder.java            # Core aggregate root managing order workflow & works
│   ├── OrderNotFoundException.java # Thrown when querying missing orders
│   └── InvalidStateTransitionException.java # Thrown when illegal state transitions occur
├── price/                          # Flexible Pricing Engine (Strategy Pattern)
│   ├── PricingRule.java            # Interface for pricing rules
│   ├── DiscountRule.java           # Applies percentage discount to price
│   ├── MinimumCostRule.java        # Enforces a minimum billing floor
│   └── PriceCalculator.java        # Pipeline executor applying active pricing rules
├── repository/                     # Persistence Abstraction Layer
│   ├── RepairOrderRepository.java  # Repository interface (CRUD abstractions)
│   └── InMemoryRepairOrderRepository.java # Thread-safe in-memory storage implementation
└── service/                        # Application Service Layer
    ├── CarService.java             # Business coordinator and facade
    └── CarServiceException.java    # Runtime exception for service-level violations
```

### Core Classes Overview

- **`Car`**: Stores vehicle identification details (`VIN`, make, model, production year) and tracks the current owner.
- **`Client`**: Represents a client with unique identifier (`UUID`) and personal details.
- **`Mechanic`**: Models auto mechanics, managing their assignment and busy/free availability status.
- **`ServiceWork`**: Encapsulates an individual repair task, calculating parts and labor costs and tracking task completion.
- **`OrderStatus`**: Lifecycle state enum enforcing permissible transitions across order stages.
- **`RepairOrder`**: Core aggregate root orchestrating the repair lifecycle, attached work items, assigned mechanic, and state invariants.
- **`PricingRule` (`DiscountRule`, `MinimumCostRule`)**: Strategy interfaces and implementations for dynamic commercial pricing calculations.
- **`PriceCalculator`**: Applies a pipeline of active pricing rules to compute the final commercial invoice.
- **`RepairOrderRepository` (`InMemoryRepairOrderRepository`)**: Interface and thread-safe in-memory repository implementing CRUD storage operations.
- **`CarService`**: High-level application service that coordinates domain models, repository persistence, and billing.
- **`CarServiceDemo`**: Demonstration program illustrating composition root setup, end-to-end order execution, and error handling.

---

## System Requirements

Before running or building the project, ensure you have:
- **Java Development Kit (JDK) 21** or higher.
- **Apache Maven 3.8+** installed (or an IDE with bundled Maven).
- An IDE such as **IntelliJ IDEA**, **Eclipse**, or **VS Code** (recommended).

---

## How to Build and Run

### 1. Clone or Open the Project
Clone the repository or open the folder in your favorite IDE:
```bash
git clone <repository-url>
cd ServiceStore
```

### 2. Run All Tests
Execute the full test suite (180+ unit tests) via Maven:
```bash
mvn clean test
```
All tests should pass with `BUILD SUCCESS`.

### 3. Run the Demonstration Application
Run the `CarServiceDemo` class to see the complete workflow in action:
```bash
mvn compile exec:java -Dexec.mainClass="carService.CarServiceDemo"
```
Or directly run the `main` method in [`CarServiceDemo.java`](file:///c:/Users/marth/IdeaProjects/ServiceStore/src/main/java/carService/CarServiceDemo.java) inside IntelliJ IDEA.

The demo demonstrates:
1. **Dependency Injection Setup** (Composition Root).
2. **Positive Scenario**: Vehicle registration $\to$ Diagnosis $\to$ Work attachment $\to$ Client approval $\to$ Mechanic assignment $\to$ Repair execution $\to$ Commercial invoice generation.
3. **Negative Scenarios**: Proper rejection and exception handling when attempting to approve an empty order or query non-existent orders.

---

## Current Limitations

- **Persistence**: Orders are maintained in memory (`InMemoryRepairOrderRepository`) and do not persist across application restarts.
- **User Interface**: No web, graphical (GUI), or interactive CLI interface is attached yet; interactions are demonstrated via unit tests and `CarServiceDemo`.
- **Authentication**: No role-based access control (RBAC) for mechanics vs. managers vs. clients.

---

## Future Improvements

- Add persistent database storage using **Spring Data JPA / Hibernate** and **PostgreSQL/H2**.
- Build a RESTful API using **Spring Boot** to expose endpoints for frontend or mobile applications.
- Implement an interactive Web or Command-Line Interface (CLI).
- Add email/SMS notifications to customers when order status changes to `SUCCESSFULLY_COMPLETED`.

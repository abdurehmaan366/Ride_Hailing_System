# Ride-Hailing Management System

A console-based Ride-Hailing Management System built in **Java** to demonstrate core OOP principles in a practical, real-world simulation. The system supports ride requests, driver assignment, distance-based fare calculation, and ride record storage. Each component is intentionally designed around a specific OOP concept — abstract classes for users, inheritance across Rider/Driver/Vehicle, encapsulation via private fields, composition within the Ride class, and polymorphism in FareCalculator and PaymentService. Responsibilities are cleanly separated across dedicated service classes (RideRepository, NotificationService, RatingService), keeping the architecture organized and scalable.

---

## Features

- **Rider Management** — Register, login, request rides, cancel pending rides, view ride history, and rate drivers
- **Driver Management** — Register, login, toggle availability, accept ride requests, complete rides, and rate riders
- **Ride Lifecycle** — Full ride flow: Pending → Accepted → Completed / Cancelled tracked via `RideStatus` enum
- **Fare Calculation** — Distance-based fare calculation using `RouteService` and `FareCalculator`
- **Payment Processing** — Simulated wallet-based payment with validation and exception handling
- **Ride Repository** — Persistent ride storage with retrieval by rider, driver, or all rides
- **Notifications** — Console-based event notifications for ride booked, driver assigned, ride started, and completed
- **Rating System** — Mutual post-ride rating between riders and drivers with average tracking

---

## OOP Concepts Demonstrated

| Concept | Where Applied |
|---|---|
| **Abstraction** | `User` abstract class — cannot be instantiated directly |
| **Inheritance** | `Rider` and `Driver` extend `User` |
| **Encapsulation** | Private fields with getters/setters across all classes |
| **Composition** | `Ride` contains `Rider`, `Driver`, `Vehicle`, and `Location` objects |
| **Polymorphism** | `FareCalculator` and `PaymentService` |
| **Enums** | `RideStatus` — Pending, Accepted, Completed, Cancelled |
| **Single Responsibility** | Each service class has one job (PaymentService, RatingService, NotificationService) |
| **Separation of Concerns** | Business logic, file handling, and notifications are fully separated |

---

## Project Structure (14 Classes)

```
├── User.java                 # Abstract base class for all users
├── Rider.java                # Extends User — ride requests, history, ratings
├── Driver.java               # Extends User — availability, ride acceptance
├── Vehicle.java              # Composed inside Driver (Car, Bike, Van)
├── Location.java             # Coordinates + distance calculation
├── Ride.java                 # Core entity — composes Rider, Driver, Vehicle, Location
├── RideStatus.java           # Enum — Pending, Accepted, Completed, Cancelled
├── RouteService.java         # Calculates distance between locations
├── FareCalculator.java       # Calculates fare using RouteService
├── Payment.java              # Payment details — amount and method
├── PaymentService.java       # Processes payments with wallet validation
├── RideRepository.java       # Stores and retrieves ride records
├── NotificationService.java  # Console notifications for ride events
└── RatingService.java        # Handles mutual ratings between Rider and Driver
```

---

## How to Run

**Requirements:** Java 8 or above

```bash
# Compile all files
javac *.java

# Run the main class
java Main
```

---

## Course Info

**Course:** Object Oriented Programming (OOP in Java)  
**Instructor:** Mr. Mohsin Ahmed  
**University:** COMSATS University Islamabad  
**Department:** Software Engineering

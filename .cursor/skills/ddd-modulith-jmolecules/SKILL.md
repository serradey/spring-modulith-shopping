---
name: ddd-modulith-jmolecules
description: Applies DDD + Spring Modulith + jMolecules patterns when designing bounded contexts, aggregates, transactions, and events. Use when adding or refactoring modules, defining entities with transactional boundaries, coordinating single vs multiple transactions, or externalizing events (default Kafka).
---

# DDD + Spring Modulith + jMolecules

Apply this skill when designing or implementing domain structure in a Spring Boot application that uses Spring Modulith and jMolecules (e.g. modular monolith, event-driven modules).

## Decision flow

1. **New bounded context?**  
   → Introduce a **new module**: a direct sub-package of the application root (e.g. `example.order`, `example.inventory`). Add `package-info.java` and optional `internal` subpackage. Verify with `ApplicationModules.of(Application.class).verify()`.

2. **Entity with transactional boundary?**  
   → Model as **jMolecules aggregate root**: `implements AggregateRoot<Self, IdType>` (or `@AggregateRoot`) and a dedicated identifier type implementing `Identifier`. Child concepts that are not transactional boundaries use `Entity<AggregateRoot, EntityId>` (or `@Entity`).

3. **Coordinate 1+ aggregates in one transaction?**  
   → Use a **module service method** with `@Transactional`. The service loads/saves aggregates and publishes domain events via `ApplicationEventPublisher.publishEvent(...)` in the same transaction. Keep the use case in one module.

4. **Coordinate 2+ aggregates/modules across transactions?**  
   → Use **Modulith application events**: publisher module publishes a domain event (record implementing `DomainEvent`) with `events.publishEvent(...)`; consumer module has a method annotated with `@ApplicationModuleListener` that receives the event. Do not depend on the other module's types; use only the event type. Add EPR (Event Publication Registry) if at-least-once and tracking are required.

5. **Externalize events?**  
   → **Default: Kafka.** Add `spring-modulith-events-kafka` and `spring-kafka`; Modulith will externalize events to Kafka. If the user specifies in-process only or another broker (e.g. AMQP), use that instead.

## Code patterns (reference)

**Module layout (Modulith):**
- Root: `com.example` with `Application.java`
- Modules: `com.example.order`, `com.example.inventory`, etc.
- Internals: `com.example.order.internal` for types that must not be used by other modules

**Aggregate root (jMolecules):**
```java
public class Order implements AggregateRoot<Order, Order.OrderId> {
  private final OrderId id;
  public record OrderId(UUID value) implements Identifier {}
}
```

**Domain event:**
```java
public record OrderCompleted(OrderId orderId) implements DomainEvent {}
```

**Module service (single transaction):**
```java
@Service
@Transactional
public void complete(Order order) {
  // ... change aggregates ...
  events.publishEvent(new OrderCompleted(order.getId()));
}
```

**Cross-module listener:**
```java
@ApplicationModuleListener
void on(OrderCompleted event) {
  // React in this module; separate transaction
}
```

## Checklist

- [ ] Bounded contexts mapped to Modulith modules (one sub-package per module)
- [ ] Transactional boundaries modeled as jMolecules aggregate roots
- [ ] Single-transaction coordination in a module service with `@Transactional`
- [ ] Cross-transaction coordination via Modulith events (`publishEvent` + `@ApplicationModuleListener`)
- [ ] Event externalization: Kafka unless user says otherwise

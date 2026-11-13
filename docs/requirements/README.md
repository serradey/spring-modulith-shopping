# Shopping Domain — Requirements Specification

This directory contains a **technology-agnostic** requirements specification for a
shopping domain composed of three collaborating capabilities: **Shopping Cart**,
**Order**, and **Analytics**.

The specification was reverse-engineered from a reference implementation but is
deliberately written without reference to any framework, transport, persistence
mechanism, or programming language. It describes **what** the system must do and
**which rules** govern its behaviour, not **how** it is built. It can therefore be
used as the basis for an implementation on any stack (for this repository: a Spring
Modulith / jMolecules modular monolith, but the spec does not assume that).

## How to read this

Behaviour is expressed as [Gherkin](https://cucumber.io/docs/gherkin/) `.feature`
files. Each feature is independently meaningful and can be used directly as
acceptance criteria or, optionally, as executable specifications.

| File | Capability | Summary |
|------|-----------|---------|
| [`shopping-cart.feature`](./shopping-cart.feature) | Shopping Cart | Managing the contents of a cart and checking it out |
| [`item-popularity.feature`](./item-popularity.feature) | Item Popularity (read model) | A running tally of how often each item is added across all carts |
| [`order.feature`](./order.feature) | Order | Turning a checked-out cart into an order to be fulfilled |
| [`analytics.feature`](./analytics.feature) | Analytics | Observing cart activity for reporting and monitoring |

## Bounded contexts and their relationships (context map)

```
                         ┌───────────────────────────────┐
                         │        Shopping Cart          │
                         │  (owns the Cart aggregate +    │
                         │   the Item Popularity model)   │
                         └───────────────────────────────┘
                             │ publishes domain events
            ┌────────────────┴───────────────────┐
            │ CartCheckedOut                      │ ItemAdded / ItemQuantityAdjusted
            │ (with cart contents)                │ ItemRemoved / CartCheckedOut
            ▼                                     ▼
   ┌──────────────────┐                  ┌──────────────────┐
   │      Order       │                  │    Analytics     │
   │ (creates orders  │                  │ (observes events │
   │  from carts)     │                  │  for reporting)  │
   └──────────────────┘                  └──────────────────┘
```

- **Shopping Cart** is the upstream context and the single source of truth for cart
  state. It publishes **domain events** describing everything that happens to a cart.
- **Order** reacts to a cart being checked out and creates an order from the cart's
  final contents. It is a downstream consumer; the cart does not depend on it.
- **Analytics** is a downstream observer that consumes cart events for reporting and
  monitoring. It never changes cart, order, or popularity state.
- **Item Popularity** is a read model owned by (and kept consistent within) the
  Shopping Cart context.

All cross-context collaboration happens through **domain events** and is **eventually
consistent**. No downstream context blocks or vetoes a cart operation.

## Ubiquitous language (glossary)

| Term | Definition |
|------|------------|
| **Cart** | A collection of items selected by a single shopper, identified by a unique **Cart Id**. A cart is either *open* (still editable) or *checked out* (finalised). |
| **Cart Id** | Stable, unique identifier of a cart. Supplied by the caller; carts are addressed by it. |
| **Item** | A product that can be placed in a cart, identified by a unique **Item Id**. |
| **Item Id** | Stable, unique identifier of an item/product. |
| **Quantity** | The number of units of a given item in a cart. Always a positive integer while the item is in the cart. |
| **Line item** | The pairing of one Item Id with its Quantity within a cart. An item appears at most once per cart. |
| **Open cart** | A cart that has not been checked out and may still be modified. |
| **Checked-out cart** | A cart that has been finalised. Its contents are frozen and it can no longer be modified. |
| **Checkout** | The act of finalising a cart, capturing the moment in time it occurred. |
| **Cart summary** | A snapshot of a cart: its line items plus whether it is checked out. |
| **Item popularity** | A running, cross-cart count reflecting net demand for an item, derived from cart activity. |
| **Order** | A request to fulfil the contents of a checked-out cart. |
| **Domain event** | A record of something that has happened in the domain (e.g. an item was added, a cart was checked out). Events are the integration mechanism between contexts. |

## Catalogue of domain events (published by Shopping Cart)

These events form the published contract of the Shopping Cart context. Downstream
contexts subscribe to them.

| Event | Meaning | Key data |
|-------|---------|----------|
| **ItemAdded** | An item was added to an open cart | Cart Id, Item Id, Quantity |
| **ItemQuantityAdjusted** | The quantity of an existing line item changed | Cart Id, Item Id, old Quantity, new Quantity |
| **ItemRemoved** | An item was removed from an open cart | Cart Id, Item Id, removed Quantity |
| **CartCheckedOut** | A cart was finalised | Cart Id, checkout time |

> Note: The internally recorded events carry both old and new quantities so that
> downstream read models (such as Item Popularity) can compute deltas. The published
> form of an event may expose a subset of these fields, but the spec assumes the
> delta information is available wherever popularity-style aggregation is required.

## Cart lifecycle (state model)

```
        AddItem / AdjustQuantity / RemoveItem
        (with validation rules; see feature)
        ┌───────────────────────────────────┐
        │                                   │
        ▼                                   │
   ┌─────────┐   Checkout (non-empty)   ┌──────────────┐
   │  OPEN   │ ───────────────────────► │ CHECKED OUT  │
   └─────────┘                          └──────────────┘
        ▲                                   │
        └── Checkout on empty cart: rejected │
                                            ▼
                         Any modification or checkout: rejected
```

## Cross-cutting requirements

- **Idempotency / safety:** Removing an item that is not present is a no-op success
  (see cart feature). Implementations should favour safe, repeatable operations.
- **Eventual consistency:** Read models (Item Popularity) and downstream contexts
  (Order, Analytics) are updated asynchronously after the cart change is durable.
  They may briefly lag the cart.
- **Resilience:** A request that cannot be completed in time must fail clearly
  (e.g. "temporarily unavailable") rather than silently corrupting state.
- **Durability:** A cart's state and history must survive restarts; checked-out
  carts and their contents remain retrievable.

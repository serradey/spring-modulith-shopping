# Capability: Analytics
#
# The Analytics context is a downstream observer of the Shopping Cart context. It
# subscribes to the stream of cart domain events and derives operational insight
# from them (counts, throughput, and the basis for richer reporting).
#
# Analytics is strictly read-only with respect to the rest of the domain: it never
# changes cart, order, or popularity state, and the upstream context neither depends
# on nor is blocked by it. It is eventually consistent and may lag the source.
#
# Analytics consumes every published cart event type:
#   * ItemAdded
#   * ItemQuantityAdjusted
#   * ItemRemoved
#   * CartCheckedOut
#
# Delivery is reliable (at-least-once) and ordered per cart, so processing must be
# resilient to occasional duplicate delivery and resumable after restart without
# losing or double-counting position.
#
# This specification is technology-agnostic: it does not prescribe transport,
# storage, or any framework.

Feature: Observe cart activity for analytics

  As an analyst
  I want to observe everything that happens to carts
  So that I can monitor activity and report on shopper behaviour

  Rule: Every published cart event is observed and recorded

    Scenario Outline: Each cart event type is consumed
      Given the Analytics observer is running
      When a "<event>" event is published by the Shopping Cart context
      Then the observer records that a "<event>" event was consumed
      And the observer notes the affected cart "<cartId>"

      Examples:
        | event                | cartId |
        | ItemAdded            | cart-1 |
        | ItemQuantityAdjusted | cart-1 |
        | ItemRemoved          | cart-1 |
        | CartCheckedOut       | cart-1 |

  Rule: The observer maintains a cumulative count of all events consumed

    Scenario: The running total increases with each event
      Given the Analytics observer has consumed 0 events
      When 3 cart events are published and consumed
      Then the observer reports a cumulative total of 3 consumed events

  Rule: The observer reports processing throughput

    # Throughput is a periodic operational metric (events processed per unit of time),
    # used for monitoring rather than as part of the business domain.
    Scenario: Throughput is reported periodically
      Given the Analytics observer is consuming a steady stream of cart events
      When a reporting interval elapses
      Then the observer reports the number of events processed per unit of time over that interval

  Rule: Observation is read-only and never affects upstream state

    Scenario: Consuming events does not change carts, orders, or popularity
      Given a cart "cart-1" exists with item "socks" at quantity 3
      When the Analytics observer consumes the events for cart "cart-1"
      Then the contents of cart "cart-1" are unchanged
      And no order is created as a result of analytics processing
      And item popularity is not changed as a result of analytics processing

  Rule: Observation is resilient and resumable

    Scenario: Processing resumes from where it stopped after a restart
      Given the Analytics observer has consumed events up to a known position
      When the observer is restarted
      Then it resumes consuming from the next unprocessed event
      And previously consumed events are not reprocessed in a way that corrupts its metrics

    Scenario: Duplicate delivery of an event is tolerated
      Given the Analytics observer has consumed an "ItemAdded" event for cart "cart-1"
      When the same "ItemAdded" event is delivered again
      Then the observer handles it without failing

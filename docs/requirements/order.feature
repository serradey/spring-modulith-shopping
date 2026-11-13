# Capability: Order
#
# The Order context turns a checked-out cart into an order to be fulfilled. It is a
# downstream consumer of the Shopping Cart context: when a cart is checked out, the
# final contents of that cart are sent to Order as an order request. Order
# acknowledges receipt.
#
# Collaboration is event-driven and eventually consistent: the order is created some
# time after checkout, once the "CartCheckedOut" event has been observed and the
# cart's final contents have been read. The Shopping Cart context does not depend on
# Order and is not blocked by it.
#
# An order request carries the originating Cart Id and the list of line items
# (Item Id + Quantity) as they stood at checkout. In this reference scope the order
# is acknowledged and its total unit count is tallied; richer fulfilment (pricing,
# payment, shipping) is intentionally out of scope but is a natural extension point.
#
# This specification is technology-agnostic: it does not prescribe transport,
# storage, or any framework.

Feature: Create an order from a checked-out cart

  As the business
  I want a checked-out cart to become an order
  So that the selected items can be fulfilled

  Rule: Checking out a cart leads to an order being created from its final contents

    Scenario: A checked-out cart produces an order with the same items
      Given a cart "cart-1" containing:
        | item    | quantity |
        | socks   | 3        |
        | t-shirt | 2        |
      When the cart "cart-1" is checked out
      Then an order is created for cart "cart-1"
      And the order contains:
        | item    | quantity |
        | socks   | 3        |
        | t-shirt | 2        |
      And the order is acknowledged as accepted

    Scenario: The order reflects the cart contents at the moment of checkout
      Given a cart "cart-1" containing:
        | item  | quantity |
        | socks | 3        |
      And the quantity of item "socks" is adjusted to 5 before checkout
      And item "hat" with quantity 1 is added before checkout
      When the cart "cart-1" is checked out
      Then the order created for cart "cart-1" contains:
        | item  | quantity |
        | socks | 5        |
        | hat   | 1        |

  Rule: An order tallies the total number of units across all its line items

    Scenario: Total unit count of an order
      Given an order for cart "cart-1" containing:
        | item    | quantity |
        | socks   | 3        |
        | t-shirt | 2        |
      Then the order's total unit count is 5

  Rule: Order creation is eventually consistent and does not block checkout

    Scenario: Checkout completes before the order is created
      Given a cart "cart-1" containing item "socks" with quantity 3
      When the cart "cart-1" is checked out
      Then the checkout completes successfully for the shopper
      And the order for cart "cart-1" is created asynchronously afterwards

  Rule: Order creation is triggered exactly once per checkout

    # Because checkout happens at most once per cart, exactly one order results from
    # it. Delivery of the triggering event must be reliable (at-least-once), so the
    # consumer must tolerate seeing the same checkout more than once without creating
    # duplicate orders.
    Scenario: A repeated checkout notification does not create a second order
      Given a cart "cart-1" has been checked out and an order has been created
      When the "CartCheckedOut" notification for cart "cart-1" is received again
      Then no additional order is created for cart "cart-1"

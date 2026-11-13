# Capability: Item Popularity (read model)
#
# Item Popularity is a read model owned by the Shopping Cart context. It maintains a
# running, cross-cart count for each item that reflects net demand: how many units of
# an item have been added to carts, adjusted for later quantity changes and removals.
#
# The count is DERIVED from cart domain events and is eventually consistent: it is
# updated after the corresponding cart change is durable, and may briefly lag.
#
# Update rules (applied per event):
#   * ItemAdded(qty)                          -> count := count + qty
#   * ItemQuantityAdjusted(oldQty -> newQty)  -> count := count + (newQty - oldQty)
#   * ItemRemoved(removedQty)                 -> count := count - removedQty
#   * CartCheckedOut                          -> no change (checkout does not reduce popularity)
#
# An item that has never been seen has an implied count of 0.
#
# This specification is technology-agnostic: it does not prescribe storage or
# computation strategy, only the resulting counts.

Feature: Track item popularity across all carts

  As a merchandiser
  I want a running count of demand for each item
  So that I can understand which items are popular across all shoppers

  Rule: Adding an item increases its popularity by the added quantity

    Scenario: First time an item is added
      Given item "socks" has a popularity count of 0
      When an "ItemAdded" event occurs for item "socks" with quantity 3
      Then the popularity count of item "socks" becomes 3

    Scenario: Adding the same item from another cart accumulates the count
      Given item "socks" has a popularity count of 3
      When an "ItemAdded" event occurs for item "socks" with quantity 5
      Then the popularity count of item "socks" becomes 8

  Rule: Adjusting an item's quantity changes its popularity by the delta

    Scenario: Increasing a quantity raises popularity by the increase
      Given item "socks" has a popularity count of 3
      When an "ItemQuantityAdjusted" event occurs for item "socks" from 3 to 5
      Then the popularity count of item "socks" becomes 5

    Scenario: Decreasing a quantity lowers popularity by the decrease
      Given item "socks" has a popularity count of 8
      When an "ItemQuantityAdjusted" event occurs for item "socks" from 5 to 2
      Then the popularity count of item "socks" becomes 5

  Rule: Removing an item lowers its popularity by the removed quantity

    Scenario: Removing reduces the count
      Given item "socks" has a popularity count of 8
      When an "ItemRemoved" event occurs for item "socks" with removed quantity 3
      Then the popularity count of item "socks" becomes 5

  Rule: Checking out a cart does not change item popularity

    Scenario: Popularity is unaffected by checkout
      Given item "socks" has a popularity count of 5
      When a "CartCheckedOut" event occurs for a cart containing item "socks"
      Then the popularity count of item "socks" remains 5

  Rule: Popularity can be queried for any item

    Scenario: Query an item that has activity
      Given item "socks" has a popularity count of 5
      When the popularity of item "socks" is requested
      Then the reported popularity count is 5

    Scenario: Query an item that has never been seen
      Given no popularity has ever been recorded for item "unknown-item"
      When the popularity of item "unknown-item" is requested
      Then the reported popularity count is 0

  Rule: Popularity updates are eventually consistent with cart changes

    Scenario: A newly added item is reflected after the cart change is durable
      Given the cart change adding 4 of item "hat" has just been accepted
      When the corresponding "ItemAdded" event has been processed by the read model
      Then the popularity count of item "hat" reflects the additional 4 units

  Rule: A sequence of events produces a cumulative net count

    Scenario: Net count after a series of events
      Given item "socks" has a popularity count of 0
      When the following events occur in order for item "socks":
        | event                  | from | to | quantity |
        | ItemAdded              |      |    | 3        |
        | ItemAdded              |      |    | 5        |
        | ItemQuantityAdjusted   | 5    | 2  |          |
        | ItemRemoved            |      |    | 3        |
      Then the popularity count of item "socks" becomes 2

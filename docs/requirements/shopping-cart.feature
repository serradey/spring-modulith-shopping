# Capability: Shopping Cart
#
# The Shopping Cart context owns the lifecycle of a cart: adding, adjusting and
# removing items while the cart is open, finalising it via checkout, and reading
# its contents. It is the single source of truth for cart state and publishes the
# domain events that downstream contexts (Order, Analytics) and read models
# (Item Popularity) rely on.
#
# A cart is addressed by a caller-supplied Cart Id. Each Item Id may appear at
# most once in a cart (its presence is tracked together with a positive quantity).
# A cart is either OPEN (editable) or CHECKED OUT (frozen).
#
# This specification is technology-agnostic: it does not prescribe transport,
# storage, or any framework.

Feature: Manage a shopping cart

  As a shopper
  I want to add, change and remove items in my cart and then check it out
  So that I can decide what to buy before placing an order

  Background:
    Given a cart identified by "cart-1"

  # ----------------------------------------------------------------------------
  # Adding items
  # ----------------------------------------------------------------------------

  Rule: An item can be added to an open cart only if it is not already present and the quantity is positive

    Scenario: Add a new item to an empty cart
      Given the cart "cart-1" is open and empty
      When the shopper adds 3 of item "socks"
      Then the cart contains item "socks" with quantity 3
      And an "ItemAdded" event is published for cart "cart-1", item "socks", quantity 3
      And the operation returns the updated cart summary

    Scenario: Adding an item that is already in the cart is rejected
      Given the cart "cart-1" contains item "socks" with quantity 3
      When the shopper adds 2 of item "socks"
      Then the operation is rejected with a message that the item was already added
      And the cart still contains item "socks" with quantity 3
      And no event is published

    Scenario Outline: Adding with a non-positive quantity is rejected
      Given the cart "cart-1" is open and empty
      When the shopper adds <quantity> of item "socks"
      Then the operation is rejected with a message that the quantity must be greater than zero
      And the cart remains empty
      And no event is published

      Examples:
        | quantity |
        | 0        |
        | -1       |

  # ----------------------------------------------------------------------------
  # Adjusting quantities
  # ----------------------------------------------------------------------------

  Rule: The quantity of an existing line item can be adjusted to any positive value

    Scenario: Increase the quantity of an existing item
      Given the cart "cart-1" contains item "socks" with quantity 3
      When the shopper adjusts item "socks" to quantity 5
      Then the cart contains item "socks" with quantity 5
      And an "ItemQuantityAdjusted" event is published for cart "cart-1", item "socks", from 3 to 5

    Scenario: Decrease the quantity of an existing item
      Given the cart "cart-1" contains item "socks" with quantity 5
      When the shopper adjusts item "socks" to quantity 2
      Then the cart contains item "socks" with quantity 2
      And an "ItemQuantityAdjusted" event is published for cart "cart-1", item "socks", from 5 to 2

    Scenario: Adjusting an item that is not in the cart is rejected
      Given the cart "cart-1" is open and does not contain item "hat"
      When the shopper adjusts item "hat" to quantity 2
      Then the operation is rejected with a message that the item is not present in the cart
      And no event is published

    Scenario Outline: Adjusting to a non-positive quantity is rejected
      Given the cart "cart-1" contains item "socks" with quantity 3
      When the shopper adjusts item "socks" to quantity <quantity>
      Then the operation is rejected with a message that the quantity must be greater than zero
      And the cart still contains item "socks" with quantity 3
      And no event is published

      Examples:
        | quantity |
        | 0        |
        | -2       |

  # ----------------------------------------------------------------------------
  # Removing items
  # ----------------------------------------------------------------------------

  Rule: Removing an item takes it out of the cart; removing an absent item is a safe no-op

    Scenario: Remove an item that is present
      Given the cart "cart-1" contains item "socks" with quantity 3
      When the shopper removes item "socks"
      Then the cart does not contain item "socks"
      And an "ItemRemoved" event is published for cart "cart-1", item "socks", removed quantity 3

    Scenario: Removing an item that is not present succeeds without changing the cart
      Given the cart "cart-1" is open and does not contain item "hat"
      When the shopper removes item "hat"
      Then the operation succeeds and returns the unchanged cart summary
      And no event is published

  Rule: A combined "update" operation sets the quantity, treating zero as a removal

    # Convenience operation that some callers use: "set this item to quantity N".
    Scenario: Updating an existing item to a positive quantity adjusts it
      Given the cart "cart-1" contains item "socks" with quantity 3
      When the shopper updates item "socks" to quantity 5
      Then the cart contains item "socks" with quantity 5
      And an "ItemQuantityAdjusted" event is published for cart "cart-1", item "socks", from 3 to 5

    Scenario: Updating an existing item to quantity zero removes it
      Given the cart "cart-1" contains item "socks" with quantity 3
      When the shopper updates item "socks" to quantity 0
      Then the cart does not contain item "socks"
      And an "ItemRemoved" event is published for cart "cart-1", item "socks", removed quantity 3

  # ----------------------------------------------------------------------------
  # Checkout
  # ----------------------------------------------------------------------------

  Rule: A non-empty open cart can be checked out exactly once

    Scenario: Check out a cart that has items
      Given the cart "cart-1" contains item "socks" with quantity 3
      And the cart "cart-1" contains item "t-shirt" with quantity 2
      When the shopper checks out the cart
      Then the cart "cart-1" is marked as checked out
      And the checkout time is recorded
      And a "CartCheckedOut" event is published for cart "cart-1"

    Scenario: Checking out an empty cart is rejected
      Given the cart "cart-1" is open and empty
      When the shopper checks out the cart
      Then the operation is rejected with a message that an empty cart cannot be checked out
      And no event is published

  # ----------------------------------------------------------------------------
  # Behaviour after checkout (cart is frozen)
  # ----------------------------------------------------------------------------

  Rule: A checked-out cart is immutable

    Background:
      Given the cart "cart-1" contains item "socks" with quantity 3
      And the cart "cart-1" has been checked out

    Scenario: Adding an item to a checked-out cart is rejected
      When the shopper adds 1 of item "hat"
      Then the operation is rejected with a message that items cannot be added to a checked-out cart
      And no event is published

    Scenario: Removing an item from a checked-out cart is rejected
      When the shopper removes item "socks"
      Then the operation is rejected with a message that items cannot be removed from a checked-out cart
      And no event is published

    Scenario: Adjusting an item in a checked-out cart is rejected
      When the shopper adjusts item "socks" to quantity 1
      Then the operation is rejected with a message that items cannot be adjusted on a checked-out cart
      And no event is published

    Scenario: Checking out an already checked-out cart is rejected
      When the shopper checks out the cart
      Then the operation is rejected with a message that the cart is already checked out
      And no event is published

  # ----------------------------------------------------------------------------
  # Reading a cart
  # ----------------------------------------------------------------------------

  Rule: A cart's contents can be read at any time; an unknown or empty cart is reported as not found

    Scenario: Read a cart that has contents
      Given the cart "cart-1" contains item "socks" with quantity 3
      When the shopper requests the contents of cart "cart-1"
      Then the cart summary lists item "socks" with quantity 3
      And the summary indicates the cart is not checked out

    Scenario: Read a checked-out cart
      Given the cart "cart-1" contains item "socks" with quantity 3
      And the cart "cart-1" has been checked out
      When the shopper requests the contents of cart "cart-1"
      Then the cart summary lists item "socks" with quantity 3
      And the summary indicates the cart is checked out

    Scenario: Reading a cart that has never had items reports not found
      Given no cart exists with id "ghost-cart"
      When the shopper requests the contents of cart "ghost-cart"
      Then the request reports that the cart was not found

  # ----------------------------------------------------------------------------
  # Durability and resilience (non-functional, expressed as behaviour)
  # ----------------------------------------------------------------------------

  Rule: Cart state is durable and survives restarts

    Scenario: A cart's contents are retained after a system restart
      Given the cart "cart-1" contains item "socks" with quantity 3
      When the system restarts
      And the shopper requests the contents of cart "cart-1"
      Then the cart summary lists item "socks" with quantity 3

  Rule: Operations that cannot complete in time fail clearly rather than corrupting state

    Scenario: An operation that times out is reported as temporarily unavailable
      Given the cart "cart-1" is open and empty
      When the shopper adds 1 of item "socks" but the operation cannot complete in time
      Then the operation fails with a "temporarily unavailable" indication
      And the cart state is left unchanged

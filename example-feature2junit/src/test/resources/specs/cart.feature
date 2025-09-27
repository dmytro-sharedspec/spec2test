Feature: online shopping cart

  Scenario: update quantity updates subtotal
    Given my cart contains "Wireless Headphones" with quantity "1" and unit price "60.00"
    When I change the quantity to "2"
    Then my cart subtotal is "120.00"
    Then my cart subtotal would be "120.00"

  Rule: free shipping applies to orders over €50

    Scenario: show free-shipping banner when threshold is met
      Given my cart subtotal is "55.00"
      When I view the cart
      Then I see the "Free shipping" banner
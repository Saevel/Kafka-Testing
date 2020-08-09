Feature: Transactions Alert

  @transactions-alert
  Scenario Outline: Should alert when there are two transactions from different countries in short succession
    Given a subscription to the alerts topic
    When there is a user with id: <user_id>, email: <email> and phone number: <phone_number>
    And in comes a transaction with id: <transaction_id1>, amount: <amount1>, type: <type1>, country: <country1> and user id: <user_id>
    And in comes a transaction with id: <transaction_id2>, amount: <amount2>, type: <type2>, country: <country2> and user id: <user_id>
    Then there should be an alert published with user id: <user_id> and transaction id: <transaction_id1>
    And there should be an alert published with user id: <user_id> and transaction id: <transaction_id2>

    Examples:
      | user_id | email            | phone_number | transaction_id1 | transaction_id2 | amount1 | amount2 | type1       | type2     | country1 | country2  |
      | 4321    | kamil@random.pl  | +48321533211 | 5555            | 6666            | 321.45  | 456.32  | widthdrawal | insertion | Poland   | Brazil    |
      | 0000    | random@random.pl | +48654234654 | 7777            | 5354363         | 842.73  | 763.53  | insertion   | insertion | Germany  | Lithuania |
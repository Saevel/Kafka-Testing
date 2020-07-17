Feature: Splitter

  Scenario Outline: Should split a String from the input topic and send the result to the output topic
    When a message with key: <key> and value: <inputValue> is sent to the Splitter input topic
    Then there should be a message with key: <key> and value: <outputValue1> on the Splitter output topic
    And there should be a message with key: <key> and value: <outputValue2> on the Splitter output topic
    And there should be a message with key: <key> and value: <outputValue3> on the Splitter output topic

    Examples:
      | key | inputValue              | outputValue1 | outputValue2 | outputValue3 |
      | k1  | abc,def,ghi             | abc          | def          | ghi          |
      | k2  | 1,2,3                   | 1            | 2            | 3            |
      | k3  | Kamil,Krystian,Owczarek | Kamil        | Krystian     | Owczarek     |
# Kafka Testing


## Intro

This project aims at showing how we can effectively do acceptance testing of Kafka-driven applications, including (but 
not limited to) stream processing ones.


## Requirements

To run this project, you will require:

    * JVM 8 or higher
    * Running Kafka
    * Running Schema Registry
    
For Kafka and schema registry, I suggest using: 
https://github.com/confluentinc/cp-docker-images/tree/5.3.0-post/examples/cp-all-in-one to run Confluent Streaming
Platform locally, by running: "docker-compose up"

Then, to access Confluent Kafka / Avro binaries, you need to log in to the "schema-registry" container with:
"docker exec -it schema-registry bash". 

To access Kafka console utilities, you need to log in to the "broker" container with:
"docker exec -it broker bash".

You don't need to install Gradle, as the embedded Gradle ships with this project.


## Configuration

Make sure you configure the "url" property in the "schemaRegistry" block to point to the schema registry base url in the 
"transactions-alert/build.gradle" file as well as the same block in the "automated-testing/build.gradle" file and the
"prv.saevel.kafka.academy.Envrionment" class inside the "transaction-alert" project. 

Make sure you configure the "kafka.bootstrap.servers" property both in the "prv.saevel.kafka.academy.Envrionment" class
inside the "transaction-alert" project and the "src/cucumberTest/resources/application.yml" file inside the 
"automated-testing" project to point towards your Kafka bootstrap servers.


## Running the tested apps

To run the "Splitter" application, run: "gradlew :transaction-alert:runSplitter"

To run the "Transactions Alert" application, run "gradlew :transaction-alert:runTransactionsAlert"

Both will automatically register relevant schemas in your schema registry and generate Avro classes from those schemas.


## Semi-manual testing

To test Splitter: 

    1. Make sure the Splitter is running
    
    2. Log into a machine / container with Kafka console binaries 
 
    3. Use the "kafka-console-consumer" utility to connect to the "splitter-output" topic
 
    4. Use the "kafka-console-producer" utility to connect to the "splitter-input" topic
 
    5. Type the lines from the "semi-manual-testing/splitter/input.csv" file line-by-line into the "kafka-console-producer"
    to provide the input for the tested application
 
    6. Observe the output in the "kafka-console-consumer" tool and compare to the "emi-manual-testing/splitter/output.csv"
    to verify if the Splitter works correctly
 
 
To test Transactions Alert:

    1. Make sure Transactions Alert is running
    
    2. Log into a machine / container with Confluent Avro - Kafka console binaries (i.e Schema Registry)
    
    3. Use the script formula from "semi-manual-testing/alerts.sh" to connect to the alerts topic with 
       kafka-avro-console-consumer. You may need to change the "--bootstrap-server" parameter inside the script if you are 
       not using the default Confluent docker-compose.
    
    4. Use the script formula from "semi-manual-testing/transactions.sh" to connect to the transactions topic with
       kafka-avro-console-producer. You may need to change the "--broker-list" parameter inside the script if you are 
       not using the default Confluent docker-compose.
    
    5. Use the script formula from "semi-manual-testing/users.sh" to connect to the users topic with
       kafka-avro-console-producer. You may need to change the "--broker-list" parameter inside the script if you are 
       not using the default Confluent docker-compose.
       
    6. Insert the data from "semi-manual-testing/users.data" file line-by-line to submit data to the users topic.
    
    7. Insert the data from "semi-manual-testing/transactions.data" file line-by-line to sumibt data to the transactions
       topic.
       
    8. Monitor the output in the "kafka-avro-console-consumer" tool and compare it to the contents of the 
       "semi-manual-testing/alerts.data" file, to verify correctness.
       
       
## Automated Testing

To run automated tests using Cucumber + Java Spring bindings for Kafka:

    1. Make sure that both Splitter and Transactions Alert applications are running
    
    2. Run "gradlew :automated-testing:cleanCucumberTest :automated-testing:cucumberTest" taks to run Cucumber tests
    
    3. You can find the test results in the "automated-testing/build/reports/cucumberTests/cucumber-html-reports" folder
    in the HTML form
    
Please not that the main purpose of this example is to showcase how to leverage Spring, Reactor, Java and Cucumber to 
easily test even complex Kafka apps, not provide you with some difficult task.




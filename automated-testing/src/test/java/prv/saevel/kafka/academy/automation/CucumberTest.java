package prv.saevel.kafka.academy.automation;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources",
        glue = "prv.saevel.kafka.academy.automation.glue"
)
public class CucumberTest {
}
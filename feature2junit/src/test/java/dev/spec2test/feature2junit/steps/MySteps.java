package dev.spec2test.feature2junit.steps;

import dev.spec2test.feature2junit.Feature2JUnit;
import dev.spec2test.feature2junit.Feature2JUnitGenerator;
import dev.spec2test.feature2junit.Feature2JUnitOptions;
import dev.spec2test.feature2junit.mocks.Mocks;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Set;

public class MySteps {

    protected RoundEnvironment roundEnv;

    protected ProcessingEnvironment processingEnvironment;

    protected Feature2JUnitGenerator generator;

    protected Set<TypeElement> annotationSetToProcess;

    protected Feature2JUnit feature2JUnitAnnotation;

    protected Feature2JUnitOptions feature2JUnitOptions;

    protected TypeElement annotatedBaseClass;

    protected Filer filer;

    protected StringWriter generatedClassWriter;


    public MySteps() {

        processingEnvironment = Mocks.processingEnvironment();

        filer = Mocks.filer(processingEnvironment);

        generatedClassWriter = Mocks.generatedClassWriter(filer);

        generator = Mocks.generator(processingEnvironment);

        feature2JUnitAnnotation = Mocks.feature2junit();
        feature2JUnitOptions = Mocks.feature2junitOptions();

        //        annotatedBaseClass = Mocks.annotatedBaseClass(feature2JUnitAnnotation, feature2JUnitOptions);
        annotatedBaseClass = Mocks.annotatedBaseClass(feature2JUnitAnnotation, null);

        TypeElement feature2junitAnnotationType = Mocks.feature2junitAnnotationTypeMirror();

        roundEnv = Mocks.roundEnvironment(annotatedBaseClass, feature2junitAnnotationType);

        annotationSetToProcess = Set.of(feature2junitAnnotationType);
    }

    @Given("the following base class:")
    public void the_following_base_class(String docString) {
        // Write code here that turns the phrase above into concrete actions
        //        throw new io.cucumber.java.PendingException();
    }

    @When("the generator is run")
    public void the_generator_is_run() {
        // Write code here that turns the phrase above into concrete actions
        //        throw new io.cucumber.java.PendingException();

        boolean finished = generator.process(annotationSetToProcess, roundEnv);
    }

    @Then("the content of the {string} class should be:")
    public void the_content_of_the_generated_class_should_be(String stringParam, String docString) {
        // Write code here that turns the phrase above into concrete actions

        String generatedClas = generatedClassWriter.toString().trim();
        String expectedClass = docString.trim();
        Assertions.assertEquals(expectedClass, generatedClas);
    }

    @Then("the content of generated class should be:")
    public void the_content_of_generated_class_should_be(String docString) {
        // Write code here that turns the phrase above into concrete actions

        String generatedClas = generatedClassWriter.toString().trim();
        String expectedClass = docString.trim();
        Assertions.assertEquals(expectedClass, generatedClas);
    }

    @Given("the following feature file:")
    public void the_following_feature_file(String docString) throws IOException {

        Filer filer = processingEnvironment.getFiler();

        FileObject specFile = Mockito.mock(FileObject.class);
        Mockito.when(filer.getResource(Mockito.any(), Mockito.any(), Mockito.any()))
                .thenReturn(specFile);

        Mockito.when(specFile.getCharContent(Mockito.anyBoolean()))
                .thenReturn(docString);
    }

    @Then("the generated class should be:")
    public void the_generated_class_should_be(String docString) {
        // Write code here that turns the phrase above into concrete actions
        throw new io.cucumber.java.PendingException();
    }

    @Given("a precondition")
    public void a_precondition() {
        // Write code here that turns the phrase above into concrete actions
        //        throw new io.cucumber.java.PendingException();
    }

    @When("an action is performed")
    public void an_action_is_performed() {
        // Write code here that turns the phrase above into concrete actions
        //        throw new io.cucumber.java.PendingException();
    }

    @Then("expect a result")
    public void expect_a_result() {
        // Write code here that turns the phrase above into concrete actions
        //        throw new io.cucumber.java.PendingException();
    }

}

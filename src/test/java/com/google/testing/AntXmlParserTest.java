package com.google.testing;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.google.testing.TestSuiteProto.Property;
import com.google.testing.TestSuiteProto.TestCase;
import com.google.testing.TestSuiteProto.TestSuite;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * @author alexeagle@google.com (Alex Eagle)
 */
@RunWith(JUnit4.class)
public class AntXmlParserTest {
  @Test
  public void testShouldParseSimpleXml() throws Exception {
    AntXmlParser parser = new AntXmlParser();
    TestSuite actual = parser.parse(getClass().getResourceAsStream("/simple.xml"), UTF_8);
    TestSuite expected = TestSuite.newBuilder()
        .setName("com.google.errorprone.matchers.ConstructorOfClassTest")
        .setTotalCount(8)
        .setFailureCount(1)
        .setErrorCount(2)
        .setSkippedCount(4)
        .setElapsedTimeMillis(68L)
        .addProperty(Property.newBuilder()
            .setName("java.runtime.name").setValue("Java(TM) SE Runtime Environment"))
        .addProperty(Property.newBuilder()
            .setName("sun.cpu.isalist").setValue(""))
        .addTestCase(TestCase.newBuilder()
            .setElapsedTimeMillis(17L)
            .setClassName("com.google.errorprone.matchers.ConstructorOfClassTest")
            .setName("shouldMatchSingleConstructor"))
        .build();
    assertThat(actual, is(expected));
  }
  @Test
  public void testShouldParseSimpleXmlWithComments() throws Exception {
    AntXmlParser parser = new AntXmlParser();
    TestSuite actual = parser.parse(getClass().getResourceAsStream("/simpleWithComments.xml"), UTF_8);
    TestSuite expected = TestSuite.newBuilder()
        .setName("com.google.errorprone.matchers.ConstructorOfClassTest")
        .setTotalCount(8)
        .setFailureCount(1)
        .setErrorCount(2)
        .setSkippedCount(4)
        .setElapsedTimeMillis(68L)
        .addProperty(Property.newBuilder()
            .setName("java.runtime.name").setValue("Java(TM) SE Runtime Environment"))
        .addProperty(Property.newBuilder()
            .setName("sun.cpu.isalist").setValue(""))
        .addTestCase(TestCase.newBuilder()
            .setElapsedTimeMillis(17L)
            .setClassName("com.google.errorprone.matchers.ConstructorOfClassTest")
            .setName("shouldMatchSingleConstructor"))
        .build();
    assertThat(actual, is(expected));
  }

}

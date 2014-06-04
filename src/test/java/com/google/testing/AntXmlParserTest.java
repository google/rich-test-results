package com.google.testing;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.google.testing.TestSuiteProto.ParsedStackFrame;
import com.google.testing.TestSuiteProto.Property;
import com.google.testing.TestSuiteProto.StackFrame;
import com.google.testing.TestSuiteProto.StackFrame.Builder;
import com.google.testing.TestSuiteProto.StackTrace;
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
  @Test public void shouldParseSimpleXml() throws Exception {
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

  @Test public void shouldParseTestFailure() throws Exception {
    AntXmlParser parser = new AntXmlParser();
    TestSuite actual = parser.parse(getClass().getResourceAsStream("/fail.xml"), UTF_8);
    TestSuite expected = TestSuite.newBuilder()
        .setName("com.google.SimpleTest")
        .setTotalCount(2)
        .setFailureCount(1)
        .setErrorCount(0)
        .setSkippedCount(0)
        .setElapsedTimeMillis(6L)
        .addTestCase(TestCase.newBuilder()
            .setElapsedTimeMillis(6L)
            .setClassName("com.google.SimpleTest")
            .setName("testThatFails")
            .addFailure(StackTrace.newBuilder()
                .setExceptionMessage("expected:<1> but was:<2>")
                .setExceptionType("java.lang.AssertionError")
                .addStackFrame(createUnparsedStackFrame(
                    "java.lang.AssertionError: expected:<1> but was:<2>"))
                .addStackFrame(createStackFrame("org.junit.Assert", "fail", "Assert.java", 88))
                .addStackFrame(
                    createStackFrame("org.junit.Assert", "failNotEquals", "Assert.java", 743))
                .addStackFrame(
                    createStackFrame("org.junit.Assert", "assertEquals", "Assert.java", 118))
                .addStackFrame(
                    createStackFrame("org.junit.Assert", "assertEquals", "Assert.java", 555))
                .addStackFrame(
                    createStackFrame("org.junit.Assert", "assertEquals", "Assert.java", 542))
                .addStackFrame(
                    createStackFrame("com.google.SimpleTest", "testThatFails", "SimpleTest.java",
                        11))
                .addStackFrame(createUnparsedStackFrame(
                        "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)"))))
        .addTestCase(TestCase.newBuilder()
            .setElapsedTimeMillis(0L)
            .setClassName("com.google.SimpleTest")
            .setName("testThatPasses"))
        .build();
    assertThat(actual, is(expected));
  }

  private StackFrame createUnparsedStackFrame(String unparsedLine) {
    return StackFrame.newBuilder()
        .setUnparsedLine(unparsedLine)
        .build();
  }

  @Test public void shouldParseTestError() throws Exception {
    AntXmlParser parser = new AntXmlParser();
    TestSuite actual = parser.parse(getClass().getResourceAsStream("/error.xml"), UTF_8);
    TestSuite expected = TestSuite.newBuilder()
        .setName("com.google.ExceptionThrownTest")
        .setTotalCount(1)
        .setFailureCount(0)
        .setErrorCount(1)
        .setSkippedCount(0)
        .setElapsedTimeMillis(7L)
        .addTestCase(TestCase.newBuilder()
            .setElapsedTimeMillis(7L)
            .setClassName("com.google.ExceptionThrownTest")
            .setName("testDivision")
            .setError(StackTrace.newBuilder()
                .setExceptionMessage("/ by zero")
                .setExceptionType("java.lang.ArithmeticException")
                .addStackFrame(createUnparsedStackFrame("java.lang.ArithmeticException: / by zero"))
                .addStackFrame(createStackFrame("com.google.ExceptionThrownTest", "testDivision",
                    "ExceptionThrownTest.java", 11))
                .addStackFrame(createUnparsedStackFrame(
                    "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)"))
                .addStackFrame(createStackFrame("sun.reflect.NativeMethodAccessorImpl", "invoke",
                    "NativeMethodAccessorImpl.java", 57))
                .addStackFrame(createStackFrame("sun.reflect.DelegatingMethodAccessorImpl",
                    "invoke",
                    "DelegatingMethodAccessorImpl.java", 43))
                .addStackFrame(createStackFrame("java.lang.reflect.Method", "invoke", "Method.java",
                    606))
                .addStackFrame(createStackFrame("org.junit.runners.model.FrameworkMethod$1", "runReflectiveCall",
                    "FrameworkMethod.java", 47))))
        .build();
    assertThat(actual, is(expected));
  }

  @Test public void shouldParseInvalidXml() throws Exception {
    AntXmlParser parser = new AntXmlParser();
    TestSuite actual = parser.parse(getClass().getResourceAsStream("/invalid.xml"), UTF_8);
    TestSuite expected = TestSuite.newBuilder()
        .setName("com.google.ExceptionThrownTest")
        .setTotalCount(1)
        .setFailureCount(0)
        .setErrorCount(1)
        .setSkippedCount(0)
        .setElapsedTimeMillis(7L)
        .addTestCase(TestCase.newBuilder()
            .setElapsedTimeMillis(7L)
            .setClassName("com.google.ExceptionThrownTest")
            .setName("testDivision")
            .setError(StackTrace.newBuilder()
                .setExceptionMessage("/ by zero")
                .setExceptionType("java.lang.ArithmeticException")
                .addStackFrame(createUnparsedStackFrame("java.lang.ArithmeticException: / by zero"))
                .addStackFrame(createUnparsedStackFrame("\tat java.lang.reflect.Method.invoke"))
                .addStackFrame(createStackFrame("org.junit.runners.model.FrameworkMethod$1",
                    "runReflectiveCall", "FrameworkMethod.java", 47))))
        .build();
    assertThat(actual, is(expected));
  }

  private Builder createStackFrame(String fullyQualifiedClassname, String methodName,
      String filename, int lineNumber) {
    return StackFrame.newBuilder()
        .setParsedLine(ParsedStackFrame.newBuilder()
            .setFullyQualifiedClassname(fullyQualifiedClassname)
            .setMethodName(methodName)
            .setFilename(filename)
            .setLineNumber(lineNumber));
  }

  @Test public void shouldParseSimpleXmlWithComments() throws Exception {
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

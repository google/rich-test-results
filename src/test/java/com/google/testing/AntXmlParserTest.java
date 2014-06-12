package com.google.testing;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.google.testing.TestSuiteProto.CodeReference;
import com.google.testing.TestSuiteProto.Property;
import com.google.testing.TestSuiteProto.StackContent;
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
                .addStackContent(text(
                    "java.lang.AssertionError: expected:<1> but was:<2>\n"
                        + "\tat org.junit.Assert.fail("))
                .addStackContent(codeRef("Assert.java:88", "org/junit/Assert.java", 88))
                .addStackContent(text(")\n"
                    + "\tat org.junit.Assert.failNotEquals("))
                .addStackContent(codeRef("Assert.java:743", "org/junit/Assert.java", 743))
                .addStackContent(text(")\n"
                    + "\tat org.junit.Assert.assertEquals("))
                .addStackContent(codeRef("Assert.java:118", "org/junit/Assert.java", 118))
                .addStackContent(text(")\n"
                    + "\tat org.junit.Assert.assertEquals("))
                .addStackContent(codeRef("Assert.java:555", "org/junit/Assert.java", 555))
                .addStackContent(text(")\n"
                    + "\tat org.junit.Assert.assertEquals("))
                .addStackContent(codeRef("Assert.java:542", "org/junit/Assert.java", 542))
                .addStackContent(text(")\n"
                    + "\tat com.google.SimpleTest.testThatFails("))
                .addStackContent(codeRef("SimpleTest.java:11", "com/google/SimpleTest.java", 11))
                .addStackContent(text(")\n"
                    + "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n"))))
        .addTestCase(TestCase.newBuilder()
            .setElapsedTimeMillis(0L)
            .setClassName("com.google.SimpleTest")
            .setName("testThatPasses"))
        .build();
    assertThat(actual, is(expected));
  }

  private StackContent text(String unparsedText) {
    return StackContent.newBuilder()
        .setText(unparsedText)
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
                .addStackContent(text("java.lang.ArithmeticException: / by zero\n"
                    + "\tat com.google.ExceptionThrownTest.testDivision("))
                .addStackContent(codeRef(
                    "ExceptionThrownTest.java:11", "com/google/ExceptionThrownTest.java", 11))
                .addStackContent(text(")\n"
                    + "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n"
                    + "\tat sun.reflect.NativeMethodAccessorImpl.invoke("))
                .addStackContent(codeRef(
                    "NativeMethodAccessorImpl.java:57","sun/reflect/NativeMethodAccessorImpl.java", 57))
                .addStackContent(text(")\n"
                    + "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke("))
                .addStackContent(codeRef(
                    "DelegatingMethodAccessorImpl.java:43", "sun/reflect/DelegatingMethodAccessorImpl.java", 43))
                .addStackContent(text(")\n"
                    + "\tat java.lang.reflect.Method.invoke("))
                .addStackContent(codeRef("Method.java:606", "java/lang/reflect/Method.java", 606))
                .addStackContent(text(")\n"
                    + "\tat org.junit.runners.model.FrameworkMethod$1.runReflectiveCall("))
                .addStackContent(codeRef(
                    "FrameworkMethod.java:47", "org/junit/runners/model/FrameworkMethod.java", 47))
                .addStackContent(text(")\n"))))
        .build();
    assertThat(actual, is(expected));
  }

  @Test public void shouldParseTestErrorCauseChain() throws Exception {
    AntXmlParser parser = new AntXmlParser();
    TestSuite actual = parser.parse(getClass().getResourceAsStream("/error-cause-chain.xml"), UTF_8);
    StackTrace.Builder expectedError = StackTrace.newBuilder()
        .setExceptionMessage("Division operation failed")
        .setExceptionType("java.lang.RuntimeException")
        .addStackContent(text("java.lang.RuntimeException: Division operation failed\n"
            + "\tat com.google.NestedExceptionThrownTest.testDivision("))
        .addStackContent(codeRef(
            "NestedExceptionThrownTest.java:14", "com/google/NestedExceptionThrownTest.java", 14))
        .addStackContent(text(")\n"
            + "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n"
            + "\tat sun.reflect.NativeMethodAccessorImpl.invoke("))
        .addStackContent(codeRef(
            "NativeMethodAccessorImpl.java:57", "sun/reflect/NativeMethodAccessorImpl.java", 57))
        .addStackContent(text(")\n"
            + "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke("))
        .addStackContent(codeRef(
            "DelegatingMethodAccessorImpl.java:43", "sun/reflect/DelegatingMethodAccessorImpl.java", 43))
        .addStackContent(text(")\n"
            + "\tat java.lang.reflect.Method.invoke("))
        .addStackContent(codeRef("Method.java:606", "java/lang/reflect/Method.java", 606))
        .addStackContent(text(")\n"
            + "Caused by: java.lang.ArithmeticException: / by zero\n"
            + "\tat com.google.NestedExceptionThrownTest.testDivision("))
        .addStackContent(codeRef(
            "NestedExceptionThrownTest.java:12", "com/google/NestedExceptionThrownTest.java", 12))
        .addStackContent(text(")\n"
            + "\t... 4 more\n"));
    TestSuite expected = TestSuite.newBuilder()
        .setName("com.google.NestedExceptionThrownTest")
        .setTotalCount(1)
        .setFailureCount(0)
        .setErrorCount(1)
        .setSkippedCount(0)
        .setElapsedTimeMillis(7L)
        .addTestCase(TestCase.newBuilder()
            .setElapsedTimeMillis(7L)
            .setClassName("com.google.NestedExceptionThrownTest")
            .setName("testDivision")
            .setError(expectedError))
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
                .addStackContent(text("java.lang.ArithmeticException: / by zero\n"
                    + "\tat java.lang.reflect.Method.invoke\n"
                    + "\tat org.junit.runners.model.FrameworkMethod$1.runReflectiveCall("))
                .addStackContent(codeRef(
                    "FrameworkMethod.java:47", "org/junit/runners/model/FrameworkMethod.java", 47))
                .addStackContent(text(")\n"))))
        .build();
    assertThat(actual, is(expected));
  }

  private StackContent codeRef(String text, String path, int lineNumber) {
    return StackContent.newBuilder()
        .setCodeReference(CodeReference.newBuilder()
            .setText(text)
            .setPath(path)
            .setLineNumber(lineNumber))
        .build();
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

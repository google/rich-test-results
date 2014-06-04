package com.google.testing;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.google.testing.TestSuiteProto.Property;
import com.google.testing.TestSuiteProto.StackFrame;
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
                .addStackFrame(StackFrame.newBuilder()
                    .setFullyQualifiedClassname("org.junit.Assert")
                    .setMethodName("fail")
                    .setFilename("Assert.java")
                    .setLineNumber(88))
                .addStackFrame(StackFrame.newBuilder()
                    .setFullyQualifiedClassname("org.junit.Assert")
                    .setMethodName("failNotEquals")
                    .setFilename("Assert.java")
                    .setLineNumber(743))
                .addStackFrame(StackFrame.newBuilder()
                    .setFullyQualifiedClassname("org.junit.Assert")
                    .setMethodName("assertEquals")
                    .setFilename("Assert.java")
                    .setLineNumber(118))
                .addStackFrame(StackFrame.newBuilder()
                    .setFullyQualifiedClassname("org.junit.Assert")
                    .setMethodName("assertEquals")
                    .setFilename("Assert.java")
                    .setLineNumber(555))
                .addStackFrame(StackFrame.newBuilder()
                    .setFullyQualifiedClassname("org.junit.Assert")
                    .setMethodName("assertEquals")
                    .setFilename("Assert.java")
                    .setLineNumber(542))
                .addStackFrame(StackFrame.newBuilder()
                    .setFullyQualifiedClassname("com.google.SimpleTest")
                    .setMethodName("testThatFails")
                    .setFilename("SimpleTest.java")
                    .setLineNumber(11))
                .addStackFrame(StackFrame.newBuilder()
                    .setFullyQualifiedClassname("sun.reflect.NativeMethodAccessorImpl")
                    .setMethodName("invoke0"))))
        .addTestCase(TestCase.newBuilder()
            .setElapsedTimeMillis(0L)
            .setClassName("com.google.SimpleTest")
            .setName("testThatPasses"))
        .build();
    assertThat(actual, is(expected));
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
                .addStackFrame(StackFrame.newBuilder()
                    .setFullyQualifiedClassname("com.google.ExceptionThrownTest")
                    .setMethodName("testDivision")
                    .setFilename("ExceptionThrownTest.java")
                    .setLineNumber(11))
                .addStackFrame(StackFrame.newBuilder()
                    .setFullyQualifiedClassname("sun.reflect.NativeMethodAccessorImpl")
                    .setMethodName("invoke0"))
                .addStackFrame(StackFrame.newBuilder()
                    .setFullyQualifiedClassname("sun.reflect.NativeMethodAccessorImpl")
                    .setMethodName("invoke")
                    .setFilename("NativeMethodAccessorImpl.java")
                    .setLineNumber(57))
                .addStackFrame(StackFrame.newBuilder()
                    .setFullyQualifiedClassname("sun.reflect.DelegatingMethodAccessorImpl")
                    .setMethodName("invoke")
                    .setFilename("DelegatingMethodAccessorImpl.java")
                    .setLineNumber(43))
                .addStackFrame(StackFrame.newBuilder()
                    .setFullyQualifiedClassname("java.lang.reflect.Method")
                    .setMethodName("invoke")
                    .setFilename("Method.java")
                    .setLineNumber(606))
                .addStackFrame(StackFrame.newBuilder()
                    .setFullyQualifiedClassname("org.junit.runners.model.FrameworkMethod$1")
                    .setMethodName("runReflectiveCall")
                    .setFilename("FrameworkMethod.java")
                    .setLineNumber(47))))
        .build();
    assertThat(actual, is(expected));
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

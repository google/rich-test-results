/*
/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.testing.results;

import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.testing.results.TestSuiteProto.CodeReference;
import com.google.testing.results.TestSuiteProto.Property;
import com.google.testing.results.TestSuiteProto.StackContent;
import com.google.testing.results.TestSuiteProto.StackTrace;
import com.google.testing.results.TestSuiteProto.TestCase;
import com.google.testing.results.TestSuiteProto.TestStatus;
import com.google.testing.results.TestSuiteProto.TestSuite;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

/**
 * @author alexeagle@google.com (Alex Eagle)
 */
@RunWith(JUnit4.class)
public class AntXmlParserTest {
  @Rule public ExpectedException thrown = ExpectedException.none();
  AntXmlParser parser;

  @Before
  public void setUp() throws Exception {
    parser = new AntXmlParser();
  }

  @Test
  public void shouldParseSimpleXml() throws Exception {
    List<TestSuite> actual = parser.parse(
        getClass().getResourceAsStream("/simple.xml"), UTF_8);
    TestSuite testSuite = TestSuite.newBuilder()
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
            .setStatus(TestStatus.PASSED)
            .setClassName("com.google.errorprone.matchers.ConstructorOfClassTest")
            .setName("shouldMatchSingleConstructor"))
        .build();
    assertThat(actual).containsExactly(testSuite);
  }

  @Test
  public void shouldParseTestCaseTime() throws Exception {
    final double startTimestamp = 1498079648.291;
    final double endTimestamp = 1498079648.917;
    List<TestSuite> actual = parser.parse(
        getClass().getResourceAsStream("/simple_with_test_case_time.xml"), UTF_8);
    TestSuite testSuite = TestSuite.newBuilder()
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
            .setStartTimestamp(startTimestamp)
            .setEndTimestamp(endTimestamp)
            .setStatus(TestStatus.PASSED)
            .setClassName("com.google.errorprone.matchers.ConstructorOfClassTest")
            .setName("shouldMatchSingleConstructor"))
        .build();
    assertThat(testSuite.getTestCase(0).getStartTimestamp())
        .isWithin(0.0)
        .of(startTimestamp);
    assertThat(testSuite.getTestCase(0).getEndTimestamp())
        .isWithin(0.0)
        .of(endTimestamp);
    assertThat(actual).containsExactly(testSuite);
  }

  @Test
  public void shouldParseSkippedTest() throws Exception {
    List<TestSuite> actual = parser.parse(
        getClass().getResourceAsStream("/skipped.xml"), UTF_8);
    TestSuite testSuite = TestSuite.newBuilder()
        .setName("com.google.errorprone.matchers.ConstructorOfClassTest")
        .setTotalCount(8)
        .setFailureCount(1)
        .setErrorCount(2)
        .setSkippedCount(4)
        .setElapsedTimeMillis(1068L)
        .addProperty(Property.newBuilder()
            .setName("java.runtime.name").setValue("Java(TM) SE Runtime Environment"))
        .addProperty(Property.newBuilder()
            .setName("sun.cpu.isalist").setValue(""))
        .addTestCase(TestCase.newBuilder()
            .setElapsedTimeMillis(0L)
            .setStatus(TestStatus.SKIPPED)
            .setSkippedMessage("the test is skipped.")
            .setClassName("com.google.errorprone.matchers.ConstructorOfClassTest")
            .setName("shouldMatchSingleConstructor"))
        .build();
    assertThat(actual).containsExactly(testSuite);
  }

  @Test
  public void shouldParseMultipleTestSuites() throws Exception {
    List<TestSuite> actual = parser
        .parse(getClass().getResourceAsStream("/multiple-testsuites.xml"), UTF_8);
    TestSuite testSuite = TestSuite.newBuilder()
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
            .setStatus(TestStatus.PASSED)
            .setClassName("com.google.errorprone.matchers.ConstructorOfClassTest")
            .setName("shouldMatchSingleConstructor"))
        .build();
    assertThat(actual).containsExactly(testSuite, getExpectedFailTestSuite());
  }

  @Test
  public void shouldParseTestFailure() throws Exception {
    List<TestSuite> actual =
        parser.parse(getClass().getResourceAsStream("/fail.xml"), UTF_8);
    assertThat(actual).containsExactly(getExpectedFailTestSuite());
  }

  private TestSuite getExpectedFailTestSuite() {
    return TestSuite.newBuilder()
        .setName("com.google.SimpleTest")
        .setTotalCount(2)
        .setFailureCount(1)
        .setErrorCount(0)
        .setSkippedCount(0)
        .setElapsedTimeMillis(6L)
        .addTestCase(TestCase.newBuilder()
            .setElapsedTimeMillis(6L)
            .setStatus(TestStatus.FAILED)
            .setClassName("com.google.SimpleTest")
            .setName("testThatFails")
            .addFailure(StackTrace.newBuilder()
                .setExceptionMessage("expected:<1> but was:<2>")
                .setExceptionType("java.lang.AssertionError")
                .setContent("java.lang.AssertionError: expected:<1> but was:<2>\n"
                    + "\tat org.junit.Assert.fail(Assert.java:88)\n"
                    + "\tat org.junit.Assert.failNotEquals(Assert.java:743)\n"
                    + "\tat org.junit.Assert.assertEquals(Assert.java:118)\n"
                    + "\tat org.junit.Assert.assertEquals(Assert.java:555)\n"
                    + "\tat org.junit.Assert.assertEquals(Assert.java:542)\n"
                    + "\tat com.google.SimpleTest.testThatFails(SimpleTest.java:11)\n"
                    + "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n")
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
            .setStatus(TestStatus.PASSED)
            .setClassName("com.google.SimpleTest")
            .setName("testThatPasses"))
        .build();
  }

  private StackContent text(String unparsedText) {
    return StackContent.newBuilder()
        .setText(unparsedText)
        .build();
  }

  @Test
  public void shouldParseTestError() throws Exception {
    List<TestSuite> actual = parser.parse(getClass().getResourceAsStream("/error.xml"),
        UTF_8);
    TestSuite testSuite = TestSuite.newBuilder()
        .setName("com.google.ExceptionThrownTest")
        .setTotalCount(1)
        .setFailureCount(0)
        .setErrorCount(1)
        .setSkippedCount(0)
        .setElapsedTimeMillis(7L)
        .addTestCase(TestCase.newBuilder()
            .setElapsedTimeMillis(7L)
            .setStatus(TestStatus.ERROR)
            .setClassName("com.google.ExceptionThrownTest")
            .setName("testDivision")
            .setError(StackTrace.newBuilder()
                .setExceptionMessage("/ by zero")
                .setExceptionType("java.lang.ArithmeticException")
                .setContent("java.lang.ArithmeticException: / by zero\n"
                    + "\tat com.google.ExceptionThrownTest.testDivision(ExceptionThrownTest.java:11)\n"
                    + "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n"
                    + "\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)\n"
                    + "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\n"
                    + "\tat java.lang.reflect.Method.invoke(Method.java:606)\n"
                    + "\tat org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:47)\n")
                .addStackContent(text("java.lang.ArithmeticException: / by zero\n"
                    + "\tat com.google.ExceptionThrownTest.testDivision("))
                .addStackContent(codeRef(
                    "ExceptionThrownTest.java:11", "com/google/ExceptionThrownTest.java", 11))
                .addStackContent(text(")\n"
                    + "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n"
                    + "\tat sun.reflect.NativeMethodAccessorImpl.invoke("))
                .addStackContent(codeRef(
                    "NativeMethodAccessorImpl.java:57",
                    "sun/reflect/NativeMethodAccessorImpl.java",
                    57))
                .addStackContent(text(")\n"
                    + "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke("))
                .addStackContent(codeRef(
                    "DelegatingMethodAccessorImpl.java:43",
                    "sun/reflect/DelegatingMethodAccessorImpl.java", 43))
                .addStackContent(text(")\n"
                    + "\tat java.lang.reflect.Method.invoke("))
                .addStackContent(
                    codeRef("Method.java:606", "java/lang/reflect/Method.java", 606))
                .addStackContent(text(")\n"
                    + "\tat org.junit.runners.model.FrameworkMethod$1.runReflectiveCall("))
                .addStackContent(codeRef(
                    "FrameworkMethod.java:47", "org/junit/runners/model/FrameworkMethod.java",
                    47))
                .addStackContent(text(")\n"))))
        .build();
    assertThat(actual).containsExactly(testSuite);
  }

  @Test
  public void shouldParseTestErrorCauseChain() throws Exception {
    List<TestSuite> actual = parser.parse(
        getClass().getResourceAsStream("/error-cause-chain.xml"), UTF_8);
    StackTrace.Builder expectedError = StackTrace.newBuilder()
        .setExceptionMessage("Division operation failed")
        .setExceptionType("java.lang.RuntimeException")
        .setContent("java.lang.RuntimeException: Division operation failed\n"
            + "\tat com.google.NestedExceptionThrownTest.testDivision(NestedExceptionThrownTest.java:14)\n"
            + "\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)\n"
            + "\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)\n"
            + "\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)\n"
            + "\tat java.lang.reflect.Method.invoke(Method.java:606)\n"
            + "Caused by: java.lang.ArithmeticException: / by zero\n"
            + "\tat com.google.NestedExceptionThrownTest.testDivision(NestedExceptionThrownTest.java:12)\n"
            + "\t... 4 more\n")
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
            "DelegatingMethodAccessorImpl.java:43", "sun/reflect/DelegatingMethodAccessorImpl.java",
            43))
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
            .setStatus(TestStatus.ERROR)
            .setClassName("com.google.NestedExceptionThrownTest")
            .setName("testDivision")
            .setError(expectedError))
        .build();
    assertThat(actual).containsExactly(expected);
  }

  @Ignore("Guice stack lines start with 2 spaces rather than tab, which we don't handle yet")
  @Test
  public void shouldParseTestGuiceError() throws Exception {
    List<TestSuite> actual = parser.parse(
        getClass().getResourceAsStream("/guice-error.xml"), UTF_8);
    StackTrace.Builder expectedError = StackTrace.newBuilder()
        .setExceptionMessage(("Guice creation errors:\n"
            + "\n"
            + "1) No implementation for java.lang.Integer annotated with @com.google.GuiceExceptionTest$A() was bound.\n"
            + "  at com.google.GuiceExceptionTest$MyModule.provideA(GuiceExceptionTest.java:44)\n"
            + "\n"
            + "2) No implementation for java.lang.Integer annotated with @com.google.GuiceExceptionTest$B() was bound.\n"
            + "  at com.google.GuiceExceptionTest$MyModule.provideB(GuiceExceptionTest.java:49)\n"
            + "\n"
            + "2 errors").replaceAll("\n", " "))
        .setExceptionType("com.google.inject.CreationException")
        .setContent("com.google.inject.CreationException: Guice creation errors:\n"
            + "\n"
            + "1) No implementation for java.lang.Integer annotated with @com.google.GuiceExceptionTest$A() was bound.\n"
            + "  at com.google.GuiceExceptionTest$MyModule.provideA(GuiceExceptionTest.java:44)\n"
            + "\n"
            + "2) No implementation for java.lang.Integer annotated with @com.google.GuiceExceptionTest$B() was bound.\n"
            + "  at com.google.GuiceExceptionTest$MyModule.provideB(GuiceExceptionTest.java:49)\n"
            + "\n"
            + "2 errors\n"
            + "\tat com.google.inject.internal.Errors.throwCreationExceptionIfErrorsExist(Errors.java:435)\n"
            + "\tat com.google.inject.internal.InternalInjectorCreator.initializeStatically(InternalInjectorCreator.java:154)\n"
            + "\tat com.google.inject.internal.InternalInjectorCreator.build(InternalInjectorCreator.java:106)\n"
            + "\tat com.google.inject.Guice.createInjector(Guice.java:95)\n"
            + "\tat com.google.inject.Guice.createInjector(Guice.java:72)\n"
            + "\tat com.google.inject.Guice.createInjector(Guice.java:62)\n"
            + "\tat com.google.GuiceExceptionTest.testGuiceException(GuiceExceptionTest.java:21)")
        .addStackContent(text("com.google.inject.CreationException: Guice creation errors:\n"
            + "\n"
            + "1) No implementation for java.lang.Integer annotated with @com.google.GuiceExceptionTest$A() was bound.\n"
            + "  at com.google.GuiceExceptionTest$MyModule.provideA("))
        .addStackContent(codeRef(
            "GuiceExceptionTest.java:44", "com/google/GuiceExceptionTest.java", 44))
        .addStackContent(text(")\n"
            + "\n"
            + "2) No implementation for java.lang.Integer annotated with @com.google.GuiceExceptionTest$B() was bound.\n"
            + "  at com.google.GuiceExceptionTest$MyModule.provideB("))
        .addStackContent(codeRef(
            "GuiceExceptionTest.java:49", "com/google/GuiceExceptionTest.java", 49))
        .addStackContent(text(")\n"
            + "\n"
            + "2 errors\n"
            + "\tat com.google.inject.internal.Errors.throwCreationExceptionIfErrorsExist("))
        .addStackContent(codeRef("Errors.java:435", "com/google/inject/internal/Errors.java", 435))
        .addStackContent(text(")\n"
            + "\tat com.google.inject.internal.InternalInjectorCreator.initializeStatically("))
        .addStackContent(codeRef("InternalInjectorCreator.java:154",
            "com/google/inject/internal/InternalInjectorCreator.java", 154))
        .addStackContent(text(")\n"
            + "\tat com.google.inject.internal.InternalInjectorCreator.build("))
        .addStackContent(codeRef("InternalInjectorCreator.java:106",
            "com/google/inject/internal/InternalInjectorCreator.java", 106))
        .addStackContent(text(")\n"
            + "\tat com.google.inject.Guice.createInjector("))
        .addStackContent(codeRef("Guice.java:95", "com/google/inject/Guice.java", 95))
        .addStackContent(text(")\n"
            + "\tat com.google.inject.Guice.createInjector("))
        .addStackContent(codeRef("Guice.java:72", "com/google/inject/Guice.java", 72))
        .addStackContent(text(")\n"
            + "\tat com.google.inject.Guice.createInjector("))
        .addStackContent(codeRef("Guice.java:62", "com/google/inject/Guice.java", 62))
        .addStackContent(text(")\n"
            + "\tat com.google.GuiceExceptionTest.testGuiceException("))
        .addStackContent(codeRef(
            "GuiceExceptionTest.java:21", "com/google/GuiceExceptionTest.java", 21))
        .addStackContent(text(")\n"));
    TestSuite testSuite = TestSuite.newBuilder()
        .setName("com.google.GuiceExceptionTest")
        .setTotalCount(1)
        .setFailureCount(0)
        .setErrorCount(1)
        .setSkippedCount(0)
        .setElapsedTimeMillis(152L)
        .addTestCase(TestCase.newBuilder()
            .setElapsedTimeMillis(152L)
            .setStatus(TestStatus.ERROR)
            .setClassName("com.google.GuiceExceptionTest")
            .setName("testGuiceException")
            .setError(expectedError))
        .build();
    assertThat(actual).containsExactly(testSuite);
  }

  @Test
  public void shouldParseInvalidXml() throws Exception {
    List<TestSuite> actual = parser.parse(getClass().getResourceAsStream("/invalid.xml"),
        UTF_8);
    TestSuite testSuite = TestSuite.newBuilder()
        .setName("com.google.ExceptionThrownTest")
        .setTotalCount(1)
        .setFailureCount(0)
        .setErrorCount(1)
        .setSkippedCount(0)
        .setElapsedTimeMillis(7L)
        .addTestCase(TestCase.newBuilder()
            .setElapsedTimeMillis(7L)
            .setStatus(TestStatus.ERROR)
            .setClassName("com.google.ExceptionThrownTest")
            .setName("testDivision")
            .setError(StackTrace.newBuilder()
                .setExceptionMessage("/ by zero")
                .setExceptionType("java.lang.ArithmeticException")
                .setContent("java.lang.ArithmeticException: / by zero\n"
                    + "\tat java.lang.reflect.Method.invoke\n"
                    + "\tat org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:47)\n")
                .addStackContent(text("java.lang.ArithmeticException: / by zero\n"
                    + "\tat java.lang.reflect.Method.invoke\n"
                    + "\tat org.junit.runners.model.FrameworkMethod$1.runReflectiveCall("))
                .addStackContent(codeRef(
                    "FrameworkMethod.java:47", "org/junit/runners/model/FrameworkMethod.java",
                    47))
                .addStackContent(text(")\n"))))
        .build();
    assertThat(actual).containsExactly(testSuite);
  }

  private StackContent codeRef(String text, String path, int lineNumber) {
    return StackContent.newBuilder()
        .setCodeReference(CodeReference.newBuilder()
            .setText(text)
            .setPath(path)
            .setLineNumber(lineNumber))
        .build();
  }

  @Test
  public void shouldParseSimpleXmlWithComments() throws Exception {
    List<TestSuite> actual = parser
        .parse(getClass().getResourceAsStream("/simpleWithComments.xml"), UTF_8);
    TestSuite testSuite = TestSuite.newBuilder()
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
            .setStatus(TestStatus.PASSED)
            .setClassName("com.google.errorprone.matchers.ConstructorOfClassTest")
            .setName("shouldMatchSingleConstructor"))
        .build();
    assertThat(actual).containsExactly(testSuite);
  }

  @Test
  public void shouldRaiseXmlParseErrorWhenNoTestSuiteFound() throws Exception {
    thrown.expect(XmlParseException.class);
    parser.parse(
        getClass().getResourceAsStream("/no-testsuite.xml"), UTF_8);
  }

  @Test
  public void shouldRaiseXmlParseErrorWhenTestSuiteContainsTestSuite() throws Exception {
    thrown.expect(XmlParseException.class);
    thrown.expectMessage("Element <testsuite> should not contain element <testsuite>.");
    parser.parse(
        getClass().getResourceAsStream("/unsupported_testsuite_element.xml"), UTF_8);
  }

  @Test
  public void shouldRaiseXmlParseErrorWithMalformedXMLInput() throws Exception {
    thrown.expect(XmlParseException.class);
    thrown.expectMessage("Element <root> should not contain element <one>.");
    parser.parse(
        getClass().getResourceAsStream("/malformed-xml.xml"), UTF_8);
  }

  @Test
  public void shouldPreventXXEAttacks() throws Exception {
    // If the attack is successful, the parser will fail with
    // ParseError at [row,col]:[22,15]
    // Message: /does-not-exist (No such file or directory)
    parser.parse(
        getClass().getResourceAsStream("/xxe_attack.xml"), UTF_8);

  }

  @Test
  public void shouldParseStackTraceWithoutPackageName() throws Exception {
    List<TestSuite> actual = parser.parse(
        getClass().getResourceAsStream("/no-package-name-stacktrace.xml"), UTF_8);
    TestSuite testSuite = TestSuite.newBuilder()
        .setName("")
        .setTotalCount(1)
        .setFailureCount(1)
        .setErrorCount(0)
        .setSkippedCount(0)
        .setElapsedTimeMillis(27L)
        .addTestCase(TestCase.newBuilder()
            .setElapsedTimeMillis(0L)
            .setStatus(TestStatus.FAILED)
            .setClassName("ApplicationTest")
            .setName("testSomeCoolTesting")
            .addFailure(StackTrace.newBuilder()
                .setContent("java.lang.NullPointerException: Attempt to invoke virtual method "
                    + "'void android.app.Instrumentation.setInTouchMode(boolean)' "
                    + "on a null object reference\n"
                    + "\tat android.test.ActivityInstrumentationTestCase2.getActivity"
                    + "(ActivityInstrumentationTestCase2.java:100)\n"
                    + "\tat ApplicationTest.testSomeCoolTesting(ApplicationTest.java:25)\n")
                .addStackContent(text("java.lang.NullPointerException: Attempt to invoke virtual "
                    + "method 'void android.app.Instrumentation.setInTouchMode(boolean)' on "
                    + "a null object reference\n\tat "
                    + "android.test.ActivityInstrumentationTestCase2.getActivity("))
                .addStackContent(codeRef(
                    "ActivityInstrumentationTestCase2.java:100",
                    "android/test/ActivityInstrumentationTestCase2.java",
                    100))
                .addStackContent(text(")\n\tat ApplicationTest.testSomeCoolTesting("))
                .addStackContent(codeRef(
                    "ApplicationTest.java:25",
                    "ApplicationTest.java",
                    25))
                .addStackContent(text(")\n"))))
        .build();
    assertThat(actual).containsExactly(testSuite);
  }
}

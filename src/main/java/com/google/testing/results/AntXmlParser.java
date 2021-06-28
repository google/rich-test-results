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

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.TextFormat;
import com.google.testing.results.TestSuiteProto.Property.Builder;
import com.google.testing.results.TestSuiteProto.StackTrace;
import com.google.testing.results.TestSuiteProto.TestCase;
import com.google.testing.results.TestSuiteProto.TestStatus;
import com.google.testing.results.TestSuiteProto.TestSuite;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * STaX parser for the Ant (Junit task) XML test results format.
 * @author alexeagle@google.com (Alex Eagle)
 * @author pepstein@google.com (Peter Epstein)
 */
public class AntXmlParser {
  private static final String JAVA_STACK_FRAME_PREFIX = "\tat ";

  XMLInputFactory xmlInputFactory = createFactory();

  private XMLInputFactory createFactory() {
    XMLInputFactory factory = XMLInputFactory.newInstance();
    // Prevent XXE (Xml eXternal Entity) attacks
    factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
    factory.setProperty("http://java.sun.com/xml/stream/properties/ignore-external-dtd", true);
    return factory;
  }

  public static void main(String[] args) throws IOException, XmlParseException {
    if (args.length != 1) {
      System.err.println("Usage: java AntXmlParser path/to/results.xml");
      System.exit(1);
    }
    String path = args[0];
    ImmutableList<TestSuite> testSuites = new AntXmlParser()
        .parse(new FileInputStream(path), UTF_8);
    for (TestSuite testSuite : testSuites) {
      TextFormat.print(testSuite, System.out);
    }
  }

  /**
   * Returns the list of {@link TestSuite} objects parsed from the Ant XML format input stream.
   */
  public ImmutableList<TestSuite> parse(InputStream in, Charset encoding)
      throws XmlParseException {
    try {
      XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(in, encoding.name());
      try {
        while (xmlStreamReader.hasNext()) {
          int next = xmlStreamReader.next();
          if (next == XMLStreamConstants.END_DOCUMENT) {
            break;
          }
          if (!xmlStreamReader.hasName()) {
            continue;
          }
          String tagName = xmlStreamReader.getName().toString();
          if (xmlStreamReader.isStartElement()) {
            switch (tagName) {
              case "testsuites":
                return parseSuites(xmlStreamReader);
              case "testsuite":
                return ImmutableList.of(parseSuite(xmlStreamReader));
              default:
                handleUnsupportedElement("root", tagName);
            }
          }
        }
      } finally {
        xmlStreamReader.close();
      }
    } catch (XMLStreamException e) {
      if (e.getLocation() != null) {
        throw new XmlParseException(e.getMessage(), e);
      } else {
        throw new RuntimeException(e);
      }
    }
    throw new XmlParseException("No testsuites or testsuite element found.");
  }

  private ImmutableList<TestSuite> parseSuites(XMLStreamReader xmlStreamReader)
      throws XMLStreamException, XmlParseException {
    String tagName = null;
    ImmutableList.Builder<TestSuite> testSuites = ImmutableList.builder();
    do {
      xmlStreamReader.next();
      if (!xmlStreamReader.hasName()) {
        continue;
      }
      tagName = xmlStreamReader.getName().toString();
      if (xmlStreamReader.isStartElement()) {
        switch (tagName) {
          case "testsuite":
            testSuites.add(parseSuite(xmlStreamReader));
            break;
          default:
            handleUnsupportedElement("testsuites", tagName);
        }
      }
    } while (!xmlStreamReader.isEndElement() || !"testsuites".equals(tagName));
    return testSuites.build();
  }

  private TestSuite parseSuite(XMLStreamReader xmlStreamReader)
      throws XMLStreamException, XmlParseException {
    TestSuite.Builder builder = TestSuite.newBuilder();
    for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
      String attributeValue = xmlStreamReader.getAttributeValue(i);
      switch (xmlStreamReader.getAttributeName(i).toString()) {
        case "name":
          builder.setName(attributeValue);
          break;
        case "tests":
          builder.setTotalCount(Integer.parseInt(attributeValue));
          break;
        case "time":
          builder.setElapsedTimeMillis((long) (Float.parseFloat(attributeValue) * 1000));
          break;
        case "errors":
          builder.setErrorCount(Integer.parseInt(attributeValue));
          break;
        case "failures":
          builder.setFailureCount(Integer.parseInt(attributeValue));
          break;
        case "skipped":
          builder.setSkippedCount(Integer.parseInt(attributeValue));
          break;
      }
    }

    String tagName = null;
    do {
      xmlStreamReader.next();
      if (!xmlStreamReader.hasName()) {
        continue;
      }
      tagName = xmlStreamReader.getName().toString();
      if (xmlStreamReader.isStartElement()) {
        switch (tagName) {
          case "properties":
            parseProperties(xmlStreamReader, builder);
            break;
          case "testcase":
            parseTestCase(xmlStreamReader, builder);
            break;
          case "system-out":
            skipElement(xmlStreamReader, "system-out");
            break;
          case "system-err":
            skipElement(xmlStreamReader, "system-err");
            break;
          default:
            handleUnsupportedElement("testsuite", tagName);
        }
      }
    } while (!xmlStreamReader.isEndElement() || !"testsuite".equals(tagName));
    return builder.build();
  }

  private void parseProperties(XMLStreamReader xmlStreamReader, TestSuite.Builder suiteBuilder)
      throws XMLStreamException {
    String tagName = null;
    do {
      xmlStreamReader.next();
      if (!xmlStreamReader.hasName()) {
        continue;
      }
      tagName = xmlStreamReader.getName().toString();
      if (xmlStreamReader.isStartElement()) {
        switch (tagName) {
          case "property":
            Builder builder = suiteBuilder.addPropertyBuilder();
            for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
              String attributeValue = xmlStreamReader.getAttributeValue(i);
              switch (xmlStreamReader.getAttributeName(i).toString()) {
                case "name":
                  builder.setName(attributeValue);
                  break;
                case "value":
                  builder.setValue(attributeValue);
                  break;
              }
            }
            break;
        }
      } else if (xmlStreamReader.isEndElement() && "properties".equals(tagName)) {
        break;
      }
    } while (!xmlStreamReader.isEndElement() || !"properties".equals(tagName));
  }

  private void parseTestCase(XMLStreamReader xmlStreamReader, TestSuite.Builder suiteBuilder)
      throws XMLStreamException, XmlParseException {
    TestCase.Builder builder = suiteBuilder.addTestCaseBuilder();
    builder.setStatus(TestStatus.PASSED);
    for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
      String attributeValue = xmlStreamReader.getAttributeValue(i);
      switch (xmlStreamReader.getAttributeName(i).toString()) {
        case "name":
          builder.setName(attributeValue);
          break;
        case "classname":
          builder.setClassName(attributeValue);
          break;
        case "time":
          builder.setElapsedTimeMillis((long) (Float.parseFloat(attributeValue) * 1000));
          break;
      }
    }

    String tagName = null;
    do {
      xmlStreamReader.next();
      if (!xmlStreamReader.hasName()) {
        continue;
      }
      tagName = xmlStreamReader.getName().toString();
      if (xmlStreamReader.isStartElement()) {
        switch (tagName) {
          case "failure":
            builder.setStatus(TestStatus.FAILED);
            parseStackTrace(xmlStreamReader, builder.addFailureBuilder(), "failure");
            break;
          case "error":
            builder.setStatus(TestStatus.ERROR);
            parseStackTrace(xmlStreamReader, builder.getErrorBuilder(), "error");
            break;
          case "skipped":
            builder.setStatus(TestStatus.SKIPPED);
            builder.setSkippedMessage(getElementContent(xmlStreamReader, "skipped"));
            break;
          case "system-out":
            skipElement(xmlStreamReader, "system-out");
            break;
          case "system-err":
            skipElement(xmlStreamReader, "system-err");
            break;
          default:
            handleUnsupportedElement("testcase", tagName);
        }
      }
    } while (!xmlStreamReader.isEndElement() || !"testcase".equals(tagName));
  }

  private void skipElement(XMLStreamReader xmlStreamReader, String elementName)
      throws XMLStreamException, XmlParseException {
    String tagName = null;
    do {
      xmlStreamReader.next();
      if (!xmlStreamReader.hasName()) {
        continue;
      }
      tagName = xmlStreamReader.getName().toString();
    } while (!xmlStreamReader.isEndElement() || !elementName.equals(tagName));
  }

  private TestSuite handleUnsupportedElement(String elementName, String childElement)
      throws XmlParseException {
    throw new XmlParseException(
        "Element <" + elementName + "> should not contain element <" + childElement + ">.");
  }

  private String getElementContent(XMLStreamReader xmlStreamReader, String elementName)
      throws XMLStreamException {
    String tagName = null;
    StringBuilder stringBuilder = new StringBuilder();
    do {
      xmlStreamReader.next();
      if (xmlStreamReader.hasName()) {
        tagName = xmlStreamReader.getName().toString();
      } else if (xmlStreamReader.isCharacters()) {
        String text = xmlStreamReader.getText();
        stringBuilder.append(text);
      }
    } while (!xmlStreamReader.isEndElement() || !elementName.equals(tagName));
    return stringBuilder.toString();
  }

  private void parseStackTrace(XMLStreamReader xmlStreamReader,
      StackTrace.Builder stackTraceBuilder, String elementType) throws XMLStreamException {
    for (int i = 0; i < xmlStreamReader.getAttributeCount(); i++) {
      String attributeValue = xmlStreamReader.getAttributeValue(i);
      switch (xmlStreamReader.getAttributeName(i).toString()) {
        case "message":
          stackTraceBuilder.setExceptionMessage(attributeValue);
          break;
        case "type":
          stackTraceBuilder.setExceptionType(attributeValue);
          break;
      }
    }

    //TODO(pepstein): Avoid holding entire stack trace in memory.
    String stackTrace = getElementContent(xmlStreamReader, elementType);
    stackTraceBuilder.setContent(stackTrace);

    BufferedReader reader = new BufferedReader(new StringReader(stackTrace));
    try {
      StringBuilder textBuilder = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        parseLine(stackTraceBuilder, textBuilder, line);
      }

      if (textBuilder.length() > 0) {
        stackTraceBuilder.addStackContentBuilder().setText(textBuilder.toString());
      }
    } catch (IOException e) {
      throw new XMLStreamException("Error parsing stack trace", e);
    }
  }

  private void parseLine(
      StackTrace.Builder stackTraceBuilder, StringBuilder textBuilder, String line)
      throws XMLStreamException {
    try {
      int openParen = line.lastIndexOf('(');
      int closeParen = line.lastIndexOf(')');
      if (!line.startsWith(JAVA_STACK_FRAME_PREFIX) || openParen < 0 || closeParen < 0) {
        textBuilder.append(line).append("\n");
        return;
      }

      String fileAndLine = line.substring(openParen + 1, closeParen);
      int colon = fileAndLine.indexOf(':');
      if (colon <= 0 || colon != fileAndLine.lastIndexOf(':')) {
        textBuilder.append(line).append("\n");
        return;
      }

      String path;
      String classAndMethod = line.substring(JAVA_STACK_FRAME_PREFIX.length(), openParen);
      String fullyQualifiedClassname = classAndMethod
          .substring(0, classAndMethod.lastIndexOf('.'));
      String filename = fileAndLine.substring(0, colon);
      if (fullyQualifiedClassname.contains(".")) {
        String packageName =
            fullyQualifiedClassname.substring(0, fullyQualifiedClassname.lastIndexOf("."));
        String directory = packageName.replaceAll("\\.", File.separator);
        path = directory + File.separator + filename;
      } else {
        path = filename;
      }

      int lineNumber;
      try {
        lineNumber = Integer.parseInt(fileAndLine.substring(colon + 1));
      } catch (NumberFormatException e) {
        textBuilder.append(line).append("\n");
        return;
      }

      textBuilder.append(line.substring(0, openParen + 1));
      if (textBuilder.length() > 0) {
        stackTraceBuilder.addStackContentBuilder().setText(textBuilder.toString());
        textBuilder.setLength(0);
      }
      stackTraceBuilder.addStackContentBuilder().getCodeReferenceBuilder()
          .setText(fileAndLine)
          .setPath(path)
          .setLineNumber(lineNumber);
      textBuilder.append(line.substring(closeParen)).append("\n");
    } catch (Exception e) {
      throw new XMLStreamException("Error parsing stack trace on line:\n" + line + "\n", e);
    }
  }
}

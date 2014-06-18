package com.google.testing.results;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.protobuf.TextFormat;
import com.google.testing.TestSuiteProto.Property.Builder;
import com.google.testing.TestSuiteProto.StackTrace;
import com.google.testing.TestSuiteProto.TestCase;
import com.google.testing.TestSuiteProto.TestSuite;

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

  XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

  public static void main(String[] args) throws IOException {
    String path = args[0];
    TestSuite testSuite = new AntXmlParser().parse(new FileInputStream(path), UTF_8);
    TextFormat.print(testSuite, System.out);
  }

  public TestSuite parse(InputStream in, Charset encoding) {
    TestSuite.Builder builder = TestSuite.newBuilder();
    try {
      XMLStreamReader xmlStreamReader = xmlInputFactory.createXMLStreamReader(in, encoding.name());
      while (xmlStreamReader.hasNext()) {
        int next = xmlStreamReader.next();
        if (next == XMLStreamConstants.END_DOCUMENT) {
          break;
        }
        if (!xmlStreamReader.hasName()) {
          continue;
        }
        switch (xmlStreamReader.getName().toString()) {
          case "testsuite":
            parseSuite(xmlStreamReader, builder);
            break;
          default:
        }
      }

    } catch (XMLStreamException e) {
      throw new RuntimeException(e);
    }
    return builder.build();
  }

  private void parseSuite(XMLStreamReader xmlStreamReader, TestSuite.Builder builder) throws XMLStreamException {
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
        }
      }
    } while (!xmlStreamReader.isEndElement() || !"testsuite".equals(tagName));
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
      throws XMLStreamException {
    TestCase.Builder builder = suiteBuilder.addTestCaseBuilder();
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
            parseStackTrace(xmlStreamReader, builder.addFailureBuilder(), "failure");
            break;
          case "error":
            parseStackTrace(xmlStreamReader, builder.getErrorBuilder(), "error");
            break;
        }
      }
    } while (!xmlStreamReader.isEndElement() || !"testcase".equals(tagName));
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

    String tagName = null;
    //TODO(pepstein): Avoid holding entire stack trace in memory.
    StringBuilder stringBuilder = new StringBuilder();
    do {
      xmlStreamReader.next();
      if (xmlStreamReader.hasName()) {
        tagName = xmlStreamReader.getName().toString();
      } else if (xmlStreamReader.isCharacters()) {
        String text = xmlStreamReader.getText();
        stringBuilder.append(text);
      }
    } while (!xmlStreamReader.isEndElement() || !elementType.equals(tagName));
    String stackTrace = stringBuilder.toString();

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
      int openParen = line.indexOf('(');
      int closeParen = line.indexOf(')');
      if (!line.startsWith(JAVA_STACK_FRAME_PREFIX) || openParen < 0 || closeParen < 0) {
        textBuilder.append(line).append("\n");
        return;
      }

      String fileAndLine = line.substring(openParen + 1, closeParen);
      int colon = fileAndLine.indexOf(':');
      if (colon <= 0) {
        textBuilder.append(line).append("\n");
        return;
      }

      String classAndMethod = line.substring(JAVA_STACK_FRAME_PREFIX.length(), openParen);
      String fullyQualifiedClassname = classAndMethod
          .substring(0, classAndMethod.lastIndexOf('.'));
      String packageName = fullyQualifiedClassname
          .substring(0, fullyQualifiedClassname.lastIndexOf("."));
      String directory = packageName.replaceAll("\\.", File.separator);
      String filename = fileAndLine.substring(0, colon);
      String path = directory + File.separator + filename;
      int lineNumber = Integer.parseInt(fileAndLine.substring(colon + 1));

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

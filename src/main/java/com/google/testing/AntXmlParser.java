package com.google.testing;

import com.google.testing.TestSuiteProto.Property.Builder;
import com.google.testing.TestSuiteProto.TestCase;
import com.google.testing.TestSuiteProto.TestSuite;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.nio.charset.Charset;

/**
 * STaX parser for the Ant (Junit task) XML test results format.
 * @author alexeagle@google.com (Alex Eagle)
 */
public class AntXmlParser {
  XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();

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

  private void parseTestCase(XMLStreamReader xmlStreamReader, TestSuite.Builder suiteBuilder) {
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
  }

}

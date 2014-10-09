package com.google.testing.results;

/**
 * The input is not a well formed XML file or expected Ant (Junit task) XML
 * element not found in the XML file.
 */
public class XmlParseException extends Exception {
  public XmlParseException(String message) {
    super(message);
  }

  public XmlParseException(Throwable cause) {
    super(cause);
  }

  public XmlParseException(String message, Throwable cause) {
    super(message, cause);
  }
}
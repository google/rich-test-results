---
title: XUnit XML
layout: master
---

The XUnit XML format was developed by the Ant project as part of their JUnit
runner. The format includes a tree of test cases organized by test suite,
and includes counts of test cases with each status, and the stack trace produced
by test cases which failed or errored.

There is no official schema. There is an unofficial [xsd schema][] available.

__Support by producers__

Many testing tools produce this format. It has been the de-facto industry
standard for many years.

__Support for consumers__

rich-test-results includes a robust STaX parser for the XUnit XML format. The
result is currently only represented as a [protocol buffer][].
You may use it as a library, or as a standalone Java program.

Example:
```bash
$ java -cp path/to/rich-test-results.jar \
  com.google.testing.results.AntXmlParser path/to/results.xml
```
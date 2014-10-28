---
title: XUnit XML
layout: master
---

The XUnit XML format was developed by the Ant project as part of their JUnit
runner. The format includes a tree of test cases organized by test suite,
and includes counts of test cases with each status, and the stack trace produced
by test cases which failed or errored.

There is no official schema. There is an unofficial [xsd schema][] available.

[xsd schema]: http://windyroad.com.au/dl/Open%20Source/JUnit.xsd

__Support by producers__

Many testing tools produce this format. It has been the de-facto industry
standard for many years.

__Support for consumers__

rich-test-results includes a robust STaX parser for the XUnit XML format. The
result is currently only represented as a [protocol buffer][].
You may use it as a library, or as a standalone Java program.

[protocol buffer]: https://developers.google.com/protocol-buffers/

Example:

{% highlight bash %}
$ export REPO=~/.m2/repository
$ java -cp $REPO/com/google/testing/results/results/0.1-SNAPSHOT/results-0.1-SNAPSHOT.jar:$REPO/com/google/protobuf/protobuf-java/2.6.1/protobuf-java-2.6.1.jar:$REPO/com/google/guava/guava/17.0/guava-17.0.jar \
  com.google.testing.results.AntXmlParser \
  target/surefire-reports/TEST-com.google.testing.results.AntXmlParserTest.xml
name: "com.google.testing.results.AntXmlParserTest"
elapsed_time_millis: 178
total_count: 11
failure_count: 0
error_count: 0
skipped_count: 1
property {
  name: "java.runtime.name"
  value: "Java(TM) SE Runtime Environment"
}
[ ... ]
{% endhighlight %}
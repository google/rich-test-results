The Rich Test Results project aims to represent most of the data that can be produced by static and dynamic program analysis. This includes static analysis tools, compilers, and various flavors of automated tests, including browser and mobile tests.

Since this format will start out without adoption, we must interoperate with existing formats. The xUnit XML format originally created by the Ant JUnit task is the de-facto industry standard today, so we provide a robust parser for it. We intend to add more parsers as we determine that other formats merit support.

Getting Started:
----------------

* Run: protoc --version and get your protoc version. For rich test results to build and test successfully, protoc version has to be > v2.5.0
* Add toolchains.xml file to $HOME/.m2/toolchains.xml. The content of the file should be as follows:


<?xml version="1.0" encoding="UTF-8"?>
<toolchains>
  <toolchain>
    <type>protobuf</type>
    <provides>
      <version><!-- PROTOC_VERSION (e.g. 2.5.0)#-->2.5.0</version>
    </provides>
    <configuration>
      <protocExecutable>/usr/bin/protoc</protocExecutable>
    </configuration>
  </toolchain>
</toolchains>

* Run mvn package to make sure everything builds and tests finish successfully.
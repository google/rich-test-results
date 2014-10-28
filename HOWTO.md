# Prerequisites

* [protocol buffers compiler](https://github.com/google/protobuf)
* [Maven](http://maven.apache.org/download.cgi)
  * On Ubuntu you can `sudo apt-get install maven`

# Setup your environment
The project uses maven. To get build and test to work there are a few
steps required:

1. Run: protoc --version and get your protoc version. 
 For rich test results to build and test successfully, protoc version has to
 be > v2.5.0

1. Maven needs to be told where the protocol buffer compiler installed,
 since it is not distributed as a java library.
 Create a `$HOME/.m2/toolchains.xml` file with the following content:

        <?xml version="1.0" encoding="UTF-8"?>
        <toolchains>
          <toolchain>
            <type>protobuf</type>
            <provides>
              <version>2.5.0</version>
            </provides>
            <configuration>
              <protocExecutable>[path eg /usr/bin/protoc]</protocExecutable>
            </configuration>
          </toolchain>
        </toolchains>

1. If you are unsing IntelliJ install the `Maven Integration` plugin and
   in `Project Settings > Maven` set the `Maven Home Directory` to wherever
   you put maven in step 1 (it should point to the directory which contains
   the `bin` directory).

You should now be able to build and test from IntelliJ or from the command line
using `mvn compile` and `mvn test`.

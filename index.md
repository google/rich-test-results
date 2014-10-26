---
title: rich-test-results
layout: master
---

# About
rich-test-results aims to provide an interoperable format for
representing all the results of a test execution, making it
much easier to reason about failures and identify the root cause.

Even for successful builds, we still want to track performance
metrics about the code under test as well as the tests, and present
warnings such as static analysis results.

Finally, we find that a typical test setup starts simple, but
grows in complexity until engineers on the team don't know how the
build is configured, or how to run one part of the test in isolation.
Recording detailed information from the test helps to document
the wiring of the test fixtures and testing environment.

## Design
To understand a test result, users need information from many
sources, such as the continuous build,
the build system, the test runner, the test itself, and library code
used by the test.
They need the information presented with minimal
noise, highlighting likely causes of failure and linking them
together, for example showing events that coincide in time with the
error event.

At a high level, we have some guiding design principles:

* __Uncoordinated.__ Many different processes produce results,
 perhaps on different machines, and they are minimally integrated
 with each other.
* __Extensible.__ We don’t know every tool that produces test 
 outputs, nor should we try.
* __Streaming.__ We should make it possible for a user to know about
 an error before the entire test process has completed.
* __Cross-platform.__ We don't make any assumptions about the
 languages or environments where the test executes.
 
### Summary

__Producers__ have test result data to write.

The __Environment__ is the configuration of how the 
producers are to write the test results, and must be propagated
from one tool as it invokes the next.

The __Filesystem__ is the repository where the data is written. Since
filesystem interfaces are everywhere, this is the easiest way to be
sure that even a bash script can easily record some test output.

A __MANIFEST__ file contains everything a Consumer would need
to understand one of the data files written by a Producer.

__Consumers__ are tools which need to
understand the data, usually to present it to a user or make a
decision like whether the failure is temporary so the test should be
retried. A consumer might also be a producer, for example it might
read all the result data and then re-publish it to a hosted testcase
viewing service.

### Producers
A producer has data to write. A typical producer is an existing
tool such as Maven, Gradle, Espresso, Junit, Jenkins, Travis,
etc. It might produce screenshots, 

### The Environment
We define a number of environment variables which configure
the producers. If the environment is present, then the producer
should use that configuration, otherwise it is responsible for
creating the environment. Either way, the environment must be
propagated, otherwise later producers in the call chain will
not have outputs recorded.

### The Filesystem
This might be configured as a local filesystem, however, that has
the limitation of running all the test processes on one machine.
If the test makes a service call to a process running somewhere
else, for example on a server deployed into the staging environment,
then we can't expect that server to write test outputs.

It may be a remote filesystem, for example a network share or a 
distributed cloud-hosted filesystem such as Amazon's S3 or 
Google's Cloud Storage. This is better suited for multi-process
tests. It could be implemented by the userspace software,
but has the limitation that virtual filesystem support
depends on libraries in the language where the test is written.

The best solution is therefore a kernel-based FUSE distributed
filesystem, which is available on many platforms for most 
filesystem types. However, this option requires the most
sophisticated configuration work.


### The MANIFEST file
The MANIFEST is written by a producer to annotate other output
files it wrote. It contains metadata about the file, like its
MIME type, a short description for showing in a UI. Each entry
in the MANIFEST needs to point to the file it annotates,
using a path relative to the location of the MANIFEST.
It might also provide more context about the file, for example
it might associate a screenshot with the particular test method
that wrote it.

There may be one or more MANIFEST files. Each one is treated
as one shard of the metadata produced by the whole test.
These files could be merged by one of the consumers.

The format is still undetermined. We need to select something
with a good extensibility story, and wide language support,
such as YAML.

### Consumers
A Consumer could be a website or application which displays
some or all of the data produced by a test. For example, if
the test produces a [har file](http://en.wikipedia.org/wiki/.har)
then the consumer could be the
[har viewer application](http://www.janodvarko.cz/har/viewer/).

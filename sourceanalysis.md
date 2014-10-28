---
title: Source Analysis
layout: master
---

_Status: Proposed_

The source analysis format represents some findings about source code, such
as static analysis warnings. Each finding indicates a range of text in a source
code file, and optionally includes a suggested change to that code.

A trivial example of a producer is a compiler, which outputs warnings and errors
but in a text dump. It is more useful for consumers if this data is parsed and
structured, for example to navigate to that location in the source.

We propose a JSON representation containing this data:

* Category / subcategory
* Severity
* Pointer to the range(s) of characters in the source file
* Description
* A way to find out more, like a URL
* A suggested fix for the problem
* A timestamp

__Support by producers__

None yet. We should be able to adapt the Findbugs XML report to this format.

__Support for consumers__

None yet.

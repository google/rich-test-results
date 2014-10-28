---
title: rich-test-results: example
layout: master
---

A result conforming to our schema is a layout of files
under a base directory. An example:

* basedir/
  * a1b2c3d4.MANIFEST
  * e5f67890.MANIFEST
  * shipshape/
    * shipshape.json
  * espresso/
    * img1.gif
	* img2.gif
  * splitlogs/
    * log1.data
	* log2.data

All files ending with the ".MANIFEST" suffix are read as Manifest files.
Manifest files must be written to the base directory. We suggest using a UUID
as the basename of the Manifest files, so that files written by different
processes or on different machines do not overwrite each other.

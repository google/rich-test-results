/*
 * Copyright 2014 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.testing.results;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.write;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import com.google.jimfs.Configuration;
import com.google.jimfs.Jimfs;
import com.google.testing.results.TestResultsProto.TestResults;
import com.google.testing.results.TestSuiteProto.TestSuite;
import com.google.testing.results.DirectoryBasedOutputsCollector;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author alexeagle@google.com (Alex Eagle)
 */
@RunWith(JUnit4.class)
public class DirectoryBasedOutputsCollectorTest {

  private FileSystem inMemFileSystem;

  @Before
  public void setUp() throws Exception {
    inMemFileSystem = Jimfs.newFileSystem(Configuration.unix());
  }

  @After
  public void tearDown() throws Exception {
    inMemFileSystem.close();
  }

  @Test
  // For outputs written by https://github.com/jenkinsci/google-storage-plugin
  public void testReadsOutputsFromJenkinsGCSUploadPlugin() throws Exception {
    Path root = Files.createDirectory(inMemFileSystem.getPath("/012345"));
    write(root.resolve("build-log.txt"), asList(
        "Started",
        "Building in workspace /jenkins/workspace/myjob",
        "Cloning the remote Git repository",
        "Cloning repository https://code.google.com/id/jVx4k3S3An2"
    ), UTF_8);
    Path testXml = root.resolve("tests/target/surefire-reports/TEST-com.google.Something.xml");
    Files.createDirectories(testXml.getParent());
    write(testXml, asList(
        "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>",
        "<testsuite name=\"MyTest\">",
        "</testsuite>"
    ), UTF_8);
    TestResults testResults = new DirectoryBasedOutputsCollector().parse(root);
    TestResults expected = TestResults.newBuilder()
        .setBuildLog("/012345/build-log.txt")
        .addTestSuite(TestSuite.newBuilder().setName("MyTest"))
        .build();
    assertThat(testResults, is(expected));
  }

  @Test
  public void testSkipFilesWithXmlParseError() throws Exception {
    Path root = Files.createDirectory(inMemFileSystem.getPath("/012345"));
    write(root.resolve("malformed-xml.xml"), asList(
        "<one>",
        "</two"
    ), UTF_8);
    Path testXml = root.resolve("tests/target/surefire-reports/TEST-com.google.Something.xml");
    Files.createDirectories(testXml.getParent());
    write(testXml, asList(
        "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>",
        "<testsuite name=\"MyTest\">",
        "</testsuite>"
    ), UTF_8);
    TestResults testResults = new DirectoryBasedOutputsCollector().parse(root);
    TestResults expected = TestResults.newBuilder()
        .addTestSuite(TestSuite.newBuilder().setName("MyTest"))
        .build();
    assertThat(testResults, is(expected));
  }
}

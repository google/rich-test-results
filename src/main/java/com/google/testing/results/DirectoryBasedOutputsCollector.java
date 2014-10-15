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

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.testing.results.TestResultsProto.TestResults;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Logger;

/**
 * @author alexeagle@google.com (Alex Eagle)
 */
public class DirectoryBasedOutputsCollector {
  private final static Logger logger = Logger.getLogger(
      DirectoryBasedOutputsCollector.class.getName());

  public static final Predicate<Path> LOOKS_LIKE_TEST_DIRECTORY = new Predicate<Path>() {
    @Override
    public boolean apply(Path path) {
      return path.getFileName().toString().contains("test");
    }
  };

  AntXmlParser xmlParser = new AntXmlParser();

  public TestResults parse(final Path root) throws IOException {
    final TestResults.Builder builder = TestResults.newBuilder();

    Files.walkFileTree(root, new FileVisitor<Path>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
          throws IOException {
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        System.out.println("file.getFileName() = " + file.getFileName());
        if (file.getFileName().toString().equals("build-log.txt")) {
          builder.setBuildLog(file.toString());
        }
        if (Iterables.any(file, LOOKS_LIKE_TEST_DIRECTORY) &&
            file.getFileName().toString().endsWith(".xml")) {
          try {
            builder.addAllTestSuite(xmlParser.parse(Files.newInputStream(file), UTF_8));
          } catch (XmlParseException xmlParseError) {
            logger.warning(
                "Failed to parse, file = [" + file + "], exc = [" + xmlParseError + "]");
          }
        }
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        logger.warning("Failed to read, file = [" + file + "], exc = [" + exc + "]");
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        return FileVisitResult.CONTINUE;
      }
    });
    return builder.build();
  }
}

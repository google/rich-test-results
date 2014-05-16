package com.google.testing;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.testing.TestResultsProto.TestResults;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * @author alexeagle@google.com (Alex Eagle)
 */
public class DirectoryBasedOutputsCollector {
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
          builder.addTestSuite(xmlParser.parse(Files.newInputStream(file), UTF_8));
        }

        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
        System.out.println("file = [" + file + "], exc = [" + exc + "]");
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

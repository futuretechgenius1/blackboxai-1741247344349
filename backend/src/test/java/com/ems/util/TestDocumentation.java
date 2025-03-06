package com.ems.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Utility class for generating and managing test documentation
 */
public final class TestDocumentation {

    private static final Logger logger = LoggerFactory.getLogger(TestDocumentation.class);
    private static final String DOCS_DIR = "test-documentation";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final Map<String, TestSuite> suites = new ConcurrentHashMap<>();

    private TestDocumentation() {
        // Private constructor to prevent instantiation
    }

    /**
     * Document a test suite
     */
    public static void documentSuite(String suiteName, String description) {
        suites.put(suiteName, new TestSuite(suiteName, description));
        logger.info("Started documenting test suite: {}", suiteName);
    }

    /**
     * Document a test case
     */
    public static void documentTest(String suiteName, TestCase testCase) {
        TestSuite suite = suites.get(suiteName);
        if (suite != null) {
            suite.addTestCase(testCase);
            logger.debug("Documented test case: {}", testCase.getName());
        }
    }

    /**
     * Generate HTML documentation
     */
    public static void generateHtmlDocs() {
        try {
            String content = generateHtmlContent();
            writeDocumentation("index", content, "html");
            logger.info("Generated HTML documentation");
        } catch (IOException e) {
            logger.error("Failed to generate HTML documentation", e);
        }
    }

    /**
     * Generate Markdown documentation
     */
    public static void generateMarkdownDocs() {
        try {
            String content = generateMarkdownContent();
            writeDocumentation("README", content, "md");
            logger.info("Generated Markdown documentation");
        } catch (IOException e) {
            logger.error("Failed to generate Markdown documentation", e);
        }
    }

    private static String generateHtmlContent() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n")
            .append("<html>\n<head>\n")
            .append("<title>Test Documentation</title>\n")
            .append("<style>\n")
            .append("body { font-family: Arial, sans-serif; margin: 20px; }\n")
            .append(".suite { margin-bottom: 30px; }\n")
            .append(".test { margin: 10px 0; padding: 10px; border: 1px solid #ddd; }\n")
            .append(".passed { border-left: 5px solid green; }\n")
            .append(".failed { border-left: 5px solid red; }\n")
            .append(".skipped { border-left: 5px solid orange; }\n")
            .append("</style>\n</head>\n<body>\n")
            .append("<h1>Test Documentation</h1>\n")
            .append("<p>Generated: ").append(LocalDateTime.now()).append("</p>\n");

        suites.values().forEach(suite -> {
            html.append("<div class=\"suite\">\n")
                .append("<h2>").append(suite.getName()).append("</h2>\n")
                .append("<p>").append(suite.getDescription()).append("</p>\n");

            suite.getTestCases().forEach(test -> {
                html.append("<div class=\"test ").append(test.getStatus().toLowerCase()).append("\">\n")
                    .append("<h3>").append(test.getName()).append("</h3>\n")
                    .append("<p><strong>Description:</strong> ").append(test.getDescription()).append("</p>\n")
                    .append("<p><strong>Category:</strong> ").append(test.getCategory()).append("</p>\n")
                    .append("<p><strong>Status:</strong> ").append(test.getStatus()).append("</p>\n");
                
                if (test.getError() != null) {
                    html.append("<p><strong>Error:</strong> ").append(test.getError()).append("</p>\n");
                }
                
                html.append("</div>\n");
            });

            html.append("</div>\n");
        });

        html.append("</body>\n</html>");
        return html.toString();
    }

    private static String generateMarkdownContent() {
        StringBuilder md = new StringBuilder();
        md.append("# Test Documentation\n\n")
          .append("Generated: ").append(LocalDateTime.now()).append("\n\n");

        suites.values().forEach(suite -> {
            md.append("## ").append(suite.getName()).append("\n\n")
              .append(suite.getDescription()).append("\n\n");

            suite.getTestCases().forEach(test -> {
                md.append("### ").append(test.getName()).append("\n\n")
                  .append("- **Description:** ").append(test.getDescription()).append("\n")
                  .append("- **Category:** ").append(test.getCategory()).append("\n")
                  .append("- **Status:** ").append(test.getStatus()).append("\n");
                
                if (test.getError() != null) {
                    md.append("- **Error:** ").append(test.getError()).append("\n");
                }
                
                md.append("\n");
            });
        });

        return md.toString();
    }

    private static void writeDocumentation(String name, String content, String extension) throws IOException {
        Path docsDir = Paths.get(DOCS_DIR);
        if (!Files.exists(docsDir)) {
            Files.createDirectories(docsDir);
        }

        String timestamp = LocalDateTime.now().format(DATE_FORMATTER);
        String fileName = String.format("%s_%s.%s", name, timestamp, extension);
        Path docPath = docsDir.resolve(fileName);

        try (FileWriter writer = new FileWriter(docPath.toFile())) {
            writer.write(content);
        }
    }

    /**
     * Test suite documentation class
     */
    private static class TestSuite {
        private final String name;
        private final String description;
        private final List<TestCase> testCases;

        TestSuite(String name, String description) {
            this.name = name;
            this.description = description;
            this.testCases = new ArrayList<>();
        }

        void addTestCase(TestCase testCase) {
            testCases.add(testCase);
        }

        String getName() { return name; }
        String getDescription() { return description; }
        List<TestCase> getTestCases() { return testCases; }
    }

    /**
     * Test case documentation class
     */
    public static class TestCase {
        private final String name;
        private final String description;
        private final String category;
        private final String status;
        private final String error;

        private TestCase(Builder builder) {
            this.name = builder.name;
            this.description = builder.description;
            this.category = builder.category;
            this.status = builder.status;
            this.error = builder.error;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getCategory() { return category; }
        public String getStatus() { return status; }
        public String getError() { return error; }

        public static class Builder {
            private String name;
            private String description;
            private String category;
            private String status;
            private String error;

            public Builder name(String name) {
                this.name = name;
                return this;
            }

            public Builder description(String description) {
                this.description = description;
                return this;
            }

            public Builder category(String category) {
                this.category = category;
                return this;
            }

            public Builder status(String status) {
                this.status = status;
                return this;
            }

            public Builder error(String error) {
                this.error = error;
                return this;
            }

            public TestCase build() {
                return new TestCase(this);
            }
        }
    }
}

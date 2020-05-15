/*
 * Example Plugin for SonarQube
 * Copyright (C) 2009-2020 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.georgimirchev.plugins.testperformance.rules;

import com.georgimirchev.plugins.testperformance.Constants;
import com.georgimirchev.plugins.testperformance.models.TestResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TestPerformanceSensor implements Sensor {
    private static final Logger LOGGER = Loggers.get(TestPerformanceSensor.class);

    protected static final String TEST_PERFORMANCE_REPORT_PATH_KEY = "sonar.testperformance.report";
    protected static final String TEST_PERFORMANCE_THRESHOLD_MS_KEY = "sonar.testperformance.threshold_ms";

    private static final double ARBITRARY_GAP = 2.0;
    private static final int LINE_1 = 1;
    private static final int DEFAULT_THRESHOLD_MS = 1000;

    @Override
    public void describe(SensorDescriptor descriptor) {
        descriptor.name("Check performance time of individual tests");

        // optimisation to disable execution of sensor if project does
        // not contain Java files or if the example rule is not activated
        // in the Quality profile
        descriptor.onlyOnLanguage(Constants.KOTLIN_LANGUAGE);
        descriptor.createIssuesForRuleRepositories(Constants.REPOSITORY);
    }

    @Override
    public void execute(SensorContext context) {
        Configuration config = context.config();
        Optional<String> reportPath = config.get(TEST_PERFORMANCE_REPORT_PATH_KEY);
        if (!reportPath.isPresent()) {
            LOGGER.error("sonar.testperformance.report is not present");
            return;
        }

        List<TestResult> results = null;
        try {
            Reader reader = Files.newBufferedReader(Paths.get(reportPath.get()));
            Gson gson = new GsonBuilder().create();
            results = gson.fromJson(reader, new TypeToken<List<TestResult>>() {}.getType());
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.error("Error read report path", e);
            return;
        }

        if (results == null || results.isEmpty()) {
            LOGGER.error("Test performance results are empty!");
            return;
        }

        int thresholdMs = getTestExecutionThresholdMs(config);
        List<TestResult> filteredResults = getFilteredResultsByThreshold(results, thresholdMs);

        FileSystem fs = context.fileSystem();
        Iterable<InputFile> testFiles = fs.inputFiles(fs.predicates().hasLanguage(Constants.KOTLIN_LANGUAGE));
        List<MethodsExecution> slowMethods = new ArrayList<>();
        for (TestResult testResult : filteredResults) {
            InputFile file = findFileByName(testFiles, testResult.getClassName());
            MethodsExecution methodsExecution = findCachedMethodExecution(slowMethods, file);
            if (methodsExecution != null) {
                methodsExecution.addTestResult(testResult);
            } else {
                if (file != null) {
                    methodsExecution = new MethodsExecution(file);
                    methodsExecution.addTestResult(testResult);
                    slowMethods.add(methodsExecution);
                }
            }
        }

        for (MethodsExecution fileWithResult : slowMethods) {
            for (TestResult testResult : fileWithResult.getTestResults()) {
                int line = getLineNumberForString(fileWithResult.getInputFile(), testResult.getName());
                String issueText = "Test execution took " + testResult.getDurationMs() + " ms";
                createIssueAtLine(context, fileWithResult.getInputFile(), line, issueText);
            }
        }
    }

    private void createIssueAtLine(SensorContext context, InputFile inputFile, int line, String text) {
        NewIssue newIssue = context.newIssue()
                .forRule(KotlinRulesDefinition.TEST_PERFORMANCE_RULE)
                .gap(ARBITRARY_GAP);

        NewIssueLocation primaryLocation = newIssue.newLocation()
                .on(inputFile)
                .at(inputFile.selectLine(line))
                .message(text);
        newIssue.at(primaryLocation);
        newIssue.save();
    }

    private int getLineNumberForString(InputFile inputFile, String text) {
        try {
            InputStream inputStream = inputFile.inputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            int lines = 0;
            while (reader.ready()) {
                String line = reader.readLine();
                lines++;
                if (line.contains(text)) {
                    reader.close();
                    return lines;
                }
            }

            return LINE_1;
        } catch (IOException e) {
            e.printStackTrace();
            return LINE_1;
        }
    }

    private List<TestResult> getFilteredResultsByThreshold(List<TestResult> results, int thresholdMs) {
        List<TestResult> filteredResults = new ArrayList<>();
        for(TestResult testResult : results) {
            if(testResult.getDurationMs() > thresholdMs) {
                filteredResults.add(testResult);
            }
        }
        return filteredResults;
    }

    private MethodsExecution findCachedMethodExecution(List<MethodsExecution> methodsPerFile, InputFile inputFile) {
        for(MethodsExecution method: methodsPerFile) {
            if (method.getInputFile() == inputFile) {
                return method;
            }
        }

        return null;
    }

    private InputFile findFileByName(Iterable<InputFile> inputFiles, String name) {
        for (InputFile inputFile : inputFiles) {
            if (inputFile.filename().contains(name)) {
                return inputFile;
            }
        }
        return null;
    }

    private int getTestExecutionThresholdMs(Configuration config) {
        Optional<Integer> threshold = config.getInt(TEST_PERFORMANCE_THRESHOLD_MS_KEY);
        return threshold.orElse(DEFAULT_THRESHOLD_MS);
    }
}



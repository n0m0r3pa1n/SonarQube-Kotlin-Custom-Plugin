package com.georgimirchev.plugins.testperformance.rules;

import org.sonar.api.batch.fs.InputFile;
import com.georgimirchev.plugins.testperformance.models.TestResult;

import java.util.ArrayList;
import java.util.List;

public class MethodsExecution {
    private InputFile inputFile;
    private List<TestResult> testResults;

    public MethodsExecution(InputFile inputFile) {
        this.inputFile = inputFile;
        this.testResults = new ArrayList<>();
    }

    public void addTestResult(TestResult testResult) {
        testResults.add(testResult);
    }

    public InputFile getInputFile() {
        return inputFile;
    }

    public List<TestResult> getTestResults() {
        return testResults;
    }
}

package com.georgimirchev.plugins.testperformance.models;


import com.google.gson.annotations.SerializedName;

public class TestResult {
    @SerializedName("class_name")
    private String className;
    private String name;
    @SerializedName("package_name")
    private String packageName;
    @SerializedName("duration_ms")
    private int durationMs;
    private String status;

    public String getClassName() {
        return className;
    }

    public String getName() {
        return name;
    }

    public String getPackageName() {
        return packageName;
    }

    public int getDurationMs() {
        return durationMs;
    }

    public String getStatus() {
        return status;
    }
}

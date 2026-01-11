package com.gateway.jobs;

public interface BaseJob {
    String getJobType();

    void execute();
}

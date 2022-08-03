package com.github.proteus1989.airexplorerdecryptersimpleclient.processor.listeners;

@FunctionalInterface
public interface TaskProcessorOnStartListener {
    void onStart(String srcFilePath, String srcName, String destFilePath, String destName);
}
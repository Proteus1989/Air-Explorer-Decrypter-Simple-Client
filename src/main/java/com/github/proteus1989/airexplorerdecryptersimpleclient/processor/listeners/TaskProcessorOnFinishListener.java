package com.github.proteus1989.airexplorerdecryptersimpleclient.processor.listeners;

@FunctionalInterface
public interface TaskProcessorOnFinishListener {
    void onFinish(String srcFolderPath, String srcName, String destFolderPath, String destName);
}
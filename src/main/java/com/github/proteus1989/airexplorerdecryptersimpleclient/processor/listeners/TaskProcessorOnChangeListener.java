package com.github.proteus1989.airexplorerdecryptersimpleclient.processor.listeners;

@FunctionalInterface
public interface TaskProcessorOnChangeListener {
    void onChange(double percent, long size, long processed,
                  String srcFolderPath, String srcName, String destFolderPath, String destName);
}
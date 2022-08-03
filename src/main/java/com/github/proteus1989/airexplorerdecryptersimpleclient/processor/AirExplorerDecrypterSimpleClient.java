package com.github.proteus1989.airexplorerdecryptersimpleclient.processor;

import com.github.proteus1989.airexplorerdecrypter.AirExplorerDecrypter;
import com.github.proteus1989.airexplorerdecrypter.AirExplorerInputStream;
import com.github.proteus1989.airexplorerdecryptersimpleclient.processor.listeners.TaskProcessorOnChangeListener;
import com.github.proteus1989.airexplorerdecryptersimpleclient.processor.listeners.TaskProcessorOnFinishListener;
import com.github.proteus1989.airexplorerdecryptersimpleclient.processor.listeners.TaskProcessorOnStartListener;
import com.github.proteus1989.airexplorerdecryptersimpleclient.utils.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

public class AirExplorerDecrypterSimpleClient {

    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    List<TaskProcessorOnStartListener> onTaskStartCallbacks = new LinkedList<>();
    List<TaskProcessorOnChangeListener> onTaskChangeCallbacks = new LinkedList<>();
    List<TaskProcessorOnFinishListener> onTaskFinishCallbacks = new LinkedList<>();

    private <T> List<T> toUnmodifiableList(List<T> list) {
        return Collections.unmodifiableList(list);
    }

    public void addOnStartListener(TaskProcessorOnStartListener listener) {
        onTaskStartCallbacks.add(listener);
    }

    public void addOnFinishListener(TaskProcessorOnFinishListener listener) {
        onTaskFinishCallbacks.add(listener);
    }

    public void addOnChangeListener(TaskProcessorOnChangeListener listener) {
        onTaskChangeCallbacks.add(listener);
    }

    public Pair<List<Future<TaskData>>, List<File>> enqueueTasks(List<File> files, File output1, String password) {
        List<TaskData> tasksData = new LinkedList<>();
        List<File> wrongPassFiles = new LinkedList<>();

        for (File file : files) {
            try {
                File output = Objects.nonNull(output1) ? output1 : file.getParentFile();
                tasksData.add(createTask(file, password, output));
            } catch (Exception ex) {
                wrongPassFiles.add(file);
            }
        }

        List<Future<TaskData>> enqueuedTasks = tasksData.stream().map(taskData -> new Task(taskData,
                        toUnmodifiableList(onTaskStartCallbacks),
                        toUnmodifiableList(onTaskChangeCallbacks),
                        toUnmodifiableList(onTaskFinishCallbacks)))
                .map(task -> executor.submit(task))
                .collect(Collectors.toList());

        return new Pair<>(enqueuedTasks, wrongPassFiles);
    }

    private TaskData createTask(File file, String pass, File output) throws IOException {
        String name = file.getName();

        FileInputStream fis = new FileInputStream(file);
        AirExplorerInputStream stream = new AirExplorerInputStream(fis, pass);
        String parsedName;
        if (name.toLowerCase().endsWith(".cloudencoded2")) {
            parsedName = AirExplorerDecrypter.decryptName(name, pass);
        } else {
            parsedName = name.replace(".cloudencoded", "");
        }

        return new TaskData(stream, file, output, parsedName);
    }

}

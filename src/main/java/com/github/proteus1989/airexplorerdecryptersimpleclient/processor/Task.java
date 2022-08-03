package com.github.proteus1989.airexplorerdecryptersimpleclient.processor;

import com.github.proteus1989.airexplorerdecryptersimpleclient.processor.listeners.TaskProcessorOnChangeListener;
import com.github.proteus1989.airexplorerdecryptersimpleclient.processor.listeners.TaskProcessorOnFinishListener;
import com.github.proteus1989.airexplorerdecryptersimpleclient.processor.listeners.TaskProcessorOnStartListener;
import lombok.RequiredArgsConstructor;

import java.io.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

@RequiredArgsConstructor
public class Task implements Callable<TaskData> {

    private final TaskData taskData;

    private final List<TaskProcessorOnStartListener> onStartListeners;
    private final List<TaskProcessorOnChangeListener> onChangeListeners;
    private final List<TaskProcessorOnFinishListener> onFinishListeners;

    @Override
    public TaskData call() {
        process();
        return taskData;
    }

    private void process() {
        try {
            onStartListeners.forEach((listener) -> listener.onStart(taskData.file().getParentFile().getPath(), taskData.file().getName(), taskData.output().getAbsolutePath(), taskData.filename()));

            String filepath = taskData.output().getCanonicalPath() + File.separator + taskData.filename();
            try (OutputStream out = new FileOutputStream(filepath);
                 InputStream in = taskData.stream()) {

                long total = 96; // Metadata header already consumed in AirExplorerInputStream
                byte[] data = new byte[1024];
                int bytesRead = in.read(data);

                while (bytesRead != -1) {
                    total += bytesRead;
                    out.write(data, 0, bytesRead);
                    bytesRead = in.read(data);

                    for (TaskProcessorOnChangeListener onChangeListener : onChangeListeners)
                        onChangeListener.onChange((total * 1.0 / taskData.file().length()) * 100,
                                taskData.file().length(),
                                total,
                                taskData.file().getParent(),
                                taskData.file().getName(),
                                taskData.file().getParent(),
                                taskData.filename());
                }

                onFinishListeners.forEach((listener) -> listener.onFinish(taskData.file().getParentFile().getPath(), taskData.file().getName(), taskData.output().getAbsolutePath(), taskData.filename()));
            }
        } catch (IOException ex) {
            Logger.getLogger(Task.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}

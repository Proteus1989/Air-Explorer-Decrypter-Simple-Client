package com.suapp.airexplorerfiledecrypter.processor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.IOException;

public class TaskExecutor implements Callable<Task>, Runnable
{

    private final Task task;

    List<TaskProcessorOnStartListener> onStartListeners;
    List<TaskProcessorOnProgressListener> onProgressListeners;
    List<TaskProcessorOnFinishListener> onFinishListeners;

    protected TaskExecutor(Task task, List<TaskProcessorOnStartListener> onStartListeners, List<TaskProcessorOnProgressListener> onProgressListeners, List<TaskProcessorOnFinishListener> onFinishListeners)
    {
        this.task = task;
        this.onStartListeners = onStartListeners;
        this.onProgressListeners = onProgressListeners;
        this.onFinishListeners = onFinishListeners;
    }

    @Override
    public void run()
    {
        process();
    }

    @Override
    public Task call()
    {
        process();
        return task;
    }

    private void process()
    {
        try
        {
            onStartListeners.forEach((listener) -> listener.onStart(task.getFile().getParent(), task.getFile().getName(), task.getFile().getParent(), task.getStream().getProcessedName()));

            String filepath = task.getOutputPath() + File.separator + task.getStream().getProcessedName();
            try (OutputStream out = new FileOutputStream(new File(filepath));
                    InputStream in = task.getStream())
            {

                long total = 96;
                byte[] data = new byte[1024];
                int bytesRead = in.read(data);

                while (bytesRead != -1)
                {
                    total += bytesRead;
                    out.write(data, 0, bytesRead);
                    bytesRead = in.read(data);

                    for (TaskProcessorOnProgressListener onProgressListener : onProgressListeners)
                        onProgressListener.onChangeProgress((total * 1.0 / task.getFile().length() * 1.0) * 100, task.getFile().length(), total);

                }

                onFinishListeners.forEach((listener) -> listener.onFinish(task.getFile().getParent(), task.getFile().getName(), task.getFile().getParent(), task.getStream().getProcessedName()));

            }
        } catch (IOException ex)
        {
            Logger.getLogger(TaskExecutor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static interface TaskProcessorOnStartListener
    {
        void onStart(String srcFilePath, String srcName, String destFilePath, String destName);
    }

    public static interface TaskProcessorOnProgressListener
    {
        void onChangeProgress(double percent, long size, long processed);
    }

    public interface TaskProcessorOnFinishListener
    {
        void onFinish(String srcFilePath, String srcName, String destFilePath, String destName);
    }

}

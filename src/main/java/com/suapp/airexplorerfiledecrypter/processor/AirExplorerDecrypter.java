package com.suapp.airexplorerfiledecrypter.processor;

import com.suapp.airexplorerfiledecrypter.processor.TaskExecutor.TaskProcessorOnFinishListener;
import com.suapp.airexplorerfiledecrypter.processor.TaskExecutor.TaskProcessorOnProgressListener;
import com.suapp.airexplorerfiledecrypter.processor.TaskExecutor.TaskProcessorOnStartListener;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class AirExplorerDecrypter
{

    ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
    List<TaskProcessorOnStartListener> onStartListeners = new LinkedList<>();
    List<TaskProcessorOnProgressListener> onProgressListeners = new LinkedList<>();
    List<TaskProcessorOnFinishListener> onFinishListeners = new LinkedList<>();

    public void addTasks(List<Task> tasks)
    {
        tasks.forEach(task -> addTask(task));
    }

    public void addTask(Task task)
    {
        executor.execute(new TaskExecutor(task, onStartListeners, onProgressListeners, onFinishListeners));
    }

    public void addSyncTasks(List<Task> tasks)
    {
        tasks.forEach(task -> executor.execute(new TaskExecutor(task, onStartListeners, onProgressListeners, onFinishListeners)));
    }

    public void executeSyncTask(Task task)
    {
        Task _task = new TaskExecutor(task, onStartListeners, onProgressListeners, onFinishListeners).call();
    }

    public void addOnStartListener(TaskProcessorOnStartListener listener)
    {
        onStartListeners.add(listener);
    }

    public void addOnFinishListener(TaskProcessorOnFinishListener listener)
    {
        onFinishListeners.add(listener);
    }

    public void addOnChangeListener(TaskProcessorOnProgressListener listener)
    {
        onProgressListeners.add(listener);
    }

}

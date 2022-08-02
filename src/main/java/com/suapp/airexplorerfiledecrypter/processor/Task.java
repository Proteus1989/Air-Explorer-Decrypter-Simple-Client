package com.suapp.airexplorerfiledecrypter.processor;

import com.suapp.airexplorerfiledecrypter.streamwrapper.AirExplorerInputStreamWrapper;
import java.io.File;

public class Task
{

    AirExplorerInputStreamWrapper stream;
    File file;
    String output = null;

    public Task(AirExplorerInputStreamWrapper stream, File file)
    {
        this.stream = stream;
        this.file = file;
    }

    public AirExplorerInputStreamWrapper getStream()
    {
        return stream;
    }

    public void setStream(AirExplorerInputStreamWrapper stream)
    {
        this.stream = stream;
    }

    public File getFile()
    {
        return file;
    }

    public void setFile(File file)
    {
        this.file = file;
    }

    public void setOutputPath(String output)
    {
        this.output = output;
    }

    public String getOutputPath()
    {
        if(output != null)
            return output;
        return file.getParent();
    }

    

}

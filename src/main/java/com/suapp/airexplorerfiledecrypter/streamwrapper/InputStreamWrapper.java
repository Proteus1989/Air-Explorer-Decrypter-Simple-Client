package com.suapp.airexplorerfiledecrypter.streamwrapper;

import com.suapp.airexplorerfiledecrypter.utils.Document;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamWrapper extends InputStream
{
    protected InputStream in;
    protected Document params;
    protected String processedName = null;

    protected InputStreamWrapper(InputStream in) throws Exception
    {
        this.in = in;
    }

    protected InputStreamWrapper(InputStream in, Document params)
    {
        this.in = in;
        this.params = params;
    }

    @Override
    public int read() throws IOException
    {
        return in.read();
    }

    public String getProcessedName()
    {
        return processedName;
    }

    public void setProcessedName(String processedName)
    {
        this.processedName = processedName;
    }
    
    
    
}

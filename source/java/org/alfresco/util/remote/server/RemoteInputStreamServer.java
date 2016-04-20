package org.alfresco.util.remote.server;

import java.io.IOException;
import java.rmi.RemoteException;

/**
 * Interface for remote input stream support.
 * 
 * @author <a href="mailto:Michael.Shavnev@effective-soft.com">Michael Shavnev</a>
 * @since Alfresco 2.2
 */
public interface RemoteInputStreamServer
{
    public String start(String host, int port) throws RemoteException;

    public int read() throws IOException;

    public int read(byte[] bytes) throws IOException;

    public int read(byte[] bytes, int off, int len) throws IOException;

    public long skip(long n) throws IOException;

    public int available() throws IOException;

    public void mark(int readlimit);

    public boolean markSupported();

    public void reset() throws IOException;

    public void close() throws IOException;
}

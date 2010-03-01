/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
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

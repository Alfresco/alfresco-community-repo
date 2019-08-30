/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2019 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.web.scripts;

import java.io.File;

/**
 * Factory for {@link TempOutputStream}
 */
public class TempOutputStreamFactory
{
    /**
     * A temporary directory, i.e. <code>isDir == true</code>, that will be used as
     * parent directory for creating temp file backed streams.
     */
    private final File tempDir;
    private int memoryThreshold;
    private long maxContentSize;
    private boolean encrypt;
    private boolean deleteTempFileOnClose;

    /**
     * Creates a {@link TempOutputStream} factory.
     * 
     * @param tempDir
     *            the temporary directory, i.e. <code>isDir == true</code>, that
     *            will be used as * parent directory for creating temp file backed
     *            streams
     * @param memoryThreshold
     *            the memory threshold in B
     * @param maxContentSize
     *            the max content size in B
     * @param encrypt
     *            true if temp files should be encrypted
     * @param deleteTempFileOnClose
     *            true if temp files should be deleted on output stream close
     *            (useful if we need to cache the content for further reads). If
     *            this is false then we need to make sure we call
     *            {@link TempOutputStream}.destroy to clean up properly.
     */
    public TempOutputStreamFactory(File tempDir, int memoryThreshold, long maxContentSize, boolean encrypt, boolean deleteTempFileOnClose)
    {
        this.tempDir = tempDir;
        this.memoryThreshold = memoryThreshold;
        this.maxContentSize = maxContentSize;
        this.encrypt = encrypt;
        this.deleteTempFileOnClose = deleteTempFileOnClose;
    }

    /**
     * Creates a new {@link TempOutputStream} object
     */
    public TempOutputStream createOutputStream()
    {
        return new TempOutputStream(tempDir, memoryThreshold, maxContentSize, encrypt, deleteTempFileOnClose);
    }

    public File getTempDir()
    {
        return tempDir;
    }

    public int getMemoryThreshold()
    {
        return memoryThreshold;
    }

    public long getMaxContentSize()
    {
        return maxContentSize;
    }

    public boolean isEncrypt()
    {
        return encrypt;
    }

    public boolean isDeleteTempFileOnClose()
    {
        return deleteTempFileOnClose;
    }
}

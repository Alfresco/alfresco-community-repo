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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Key;
import java.util.function.Supplier;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;

import org.alfresco.repo.content.ContentLimitViolationException;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * An output stream implementation that keeps the data in memory if is less then
 * the specified <b>memoryThreshold</b> otherwise it writes it to a temp file.
 * <p/>
 *
 * Close the stream before any call to
 * {@link TempOutputStream}.getInputStream().
 * <p/>
 * 
 * If <b>deleteTempFileOnClose</b> is false then use proper try-finally patterns
 * to ensure that the temp file is destroyed after it is no longer needed.
 * 
 * <pre>
 *   <code>try
 *   {
 *      StreamUtils.copy(new BufferedInputStream(new FileInputStream(file)), tempOutputStream);
 *      tempOutputStream.close();
 *   }
 *   finally
 *   {
 *       tempOutputStream.destroy();
 *   }
 *   </code>
 * </pre>
 */
public class TempOutputStream extends OutputStream
{
    private static final Log logger = LogFactory.getLog(TempOutputStream.class);

    private static final int DEFAULT_MEMORY_THRESHOLD = 4 * 1024 * 1024; // 4mb
    private static final String ALGORITHM = "AES";
    private static final String MODE = "CTR";
    private static final String PADDING = "PKCS5Padding";
    private static final String TRANSFORMATION = ALGORITHM + '/' + MODE + '/' + PADDING;
    private static final int KEY_SIZE = 128;
    public static final String TEMP_FILE_PREFIX = "tempStreamFile-";

    private final File tempDir;
    private final int memoryThreshold;
    private final long maxContentSize;
    private final boolean encrypt;

    private long length = 0;
    private OutputStream outputStream;
    private File tempFile;

    private Key symKey;
    private byte[] iv;

    /**
     * Creates a TempOutputStream.
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
     */
    public TempOutputStream(File tempDir, int memoryThreshold, long maxContentSize, boolean encrypt)
    {
        this.tempDir = tempDir;
        this.memoryThreshold = (memoryThreshold < 0) ? DEFAULT_MEMORY_THRESHOLD : memoryThreshold;
        this.maxContentSize = maxContentSize;
        this.encrypt = encrypt;

        this.outputStream = new ByteArrayOutputStream();
    }

    /**
     * Returns the data as an InputStream
     */
    public InputStream toNewInputStream() throws IOException
    {
        closeOutputStream();

        if (tempFile == null)
        {
            return new ByteArrayInputStream(((ByteArrayOutputStream) outputStream).toByteArray());
        }
        if (!encrypt)
        {
            return new BufferedInputStream(new FileInputStream(tempFile));
        }
        try
        {
            final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, symKey, new IvParameterSpec(iv));

            return new BufferedInputStream(new CipherInputStream(new FileInputStream(tempFile), cipher));
        }
        catch (Exception e)
        {
            destroy();

            if (logger.isErrorEnabled())
            {
                logger.error("Cannot initialize decryption cipher", e);
            }

            throw new IOException("Cannot initialize decryption cipher", e);
        }
    }

    @Override
    public void write(int b) throws IOException
    {
        update(1);
        outputStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException
    {
        update(len);
        outputStream.write(b, off, len);
    }

    @Override
    public void flush() throws IOException
    {
        outputStream.flush();
    }

    @Override
    public void close() throws IOException
    {
        closeOutputStream();
    }

    /**
     * Closes the stream and removes the backing file (if present).
     * <p/>
     * 
     * If <b>deleteTempFileOnClose</b> is false then use proper try-finally patterns
     * to ensure that the temp file is destroyed after it is no longer needed.
     *
     * <pre>
     *   <code>try
     *   {
     *      StreamUtils.copy(new BufferedInputStream(new FileInputStream(file)), tempOutputStream);
     *      tempOutputStream.close();
     *   }
     *   finally
     *   {
     *       tempOutputStream.destroy();
     *   }
     *   </code>
     * </pre>
     */
    public void destroy() throws IOException
    {
        closeOutputStream();

        deleteTempFile();
    }

    public long getLength()
    {
        return length;
    }

    private void closeOutputStream()
    {
        if (outputStream != null)
        {
            try
            {
                outputStream.flush();
            }
            catch (IOException e)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Flushing the output stream failed", e);
                }
            }

            try
            {
                outputStream.close();
            }
            catch (IOException e)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Closing the output stream failed", e);
                }
            }
        }
    }

    private void deleteTempFile()
    {
        if (tempFile != null)
        {
            try
            {
                boolean isDeleted = tempFile.delete();
                if (!isDeleted)
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Temp file could not be deleted: " + tempFile.getAbsolutePath());
                    }
                }
                else
                {
                    if (logger.isDebugEnabled())
                    {
                        logger.debug("Deleted temp file: " + tempFile.getAbsolutePath());
                    }
                }
            }
            finally
            {
                tempFile = null;
            }
        }
    }

    private BufferedOutputStream createFileOutputStream(final File file) throws IOException
    {
        if (!encrypt)
        {
            return new BufferedOutputStream(new FileOutputStream(file));
        }
        try
        {
            // Generate a symmetric key
            final KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
            keyGen.init(KEY_SIZE);
            symKey = keyGen.generateKey();

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, symKey);

            iv = cipher.getIV();

            return new BufferedOutputStream(new CipherOutputStream(new FileOutputStream(file), cipher));
        }
        catch (Exception e)
        {
            if (logger.isErrorEnabled())
            {
                logger.error("Cannot initialize encryption cipher", e);
            }

            throw new IOException("Cannot initialize encryption cipher", e);
        }
    }

    private void update(int len) throws IOException
    {
        if (surpassesMaxContentSize(len))
        {
            destroy();
            throw new ContentLimitViolationException("Content size violation, limit = " + maxContentSize);
        }

        if (surpassesThreshold(len))
        {
            tempFile = TempFileProvider.createTempFile(TEMP_FILE_PREFIX, ".bin", tempDir);

            final BufferedOutputStream fileOutputStream = createFileOutputStream(tempFile);
            fileOutputStream.write(((ByteArrayOutputStream) outputStream).toByteArray());
            fileOutputStream.flush();

            try
            {
                outputStream.close();
            }
            catch (IOException ignore)
            {
                // Ignore exception
            }

            outputStream = fileOutputStream;
        }

        length += len;
    }

    private boolean surpassesMaxContentSize(final int len)
    {
        return maxContentSize >= 0 && length + len > maxContentSize;
    }

    private boolean surpassesThreshold(final int len)
    {
        return tempFile == null && length + len > memoryThreshold;
    }

    /**
     * Creates a {@link TempOutputStream} factory/supplier.
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
     */
    public static Supplier<TempOutputStream> factory(final File tempDir, final int memoryThreshold,
        final long maxContentSize, final boolean encrypt)
    {
        return () -> new TempOutputStream(tempDir, memoryThreshold, maxContentSize, encrypt);
    }
}

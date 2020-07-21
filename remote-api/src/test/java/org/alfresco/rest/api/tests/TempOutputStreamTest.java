
/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.rest.api.tests;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.stream.Stream;

import org.alfresco.repo.content.ContentLimitViolationException;
import org.alfresco.repo.web.scripts.TempOutputStream;
import org.alfresco.repo.web.scripts.TempOutputStreamFactory;
import org.alfresco.util.TempFileProvider;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.StreamUtils;

/**
 * Tests basic {@link TempOutputStream} functionality
 */
public class TempOutputStreamTest
{
    private static final String TEMP_DIRECTORY_NAME = "TempOutputStreamTest";
    private static final String FILE_PREFIX = TempOutputStream.TEMP_FILE_PREFIX;
    private static final int MEMORY_THRESHOLD = 4 * 1024 * 1024;
    private static final long MAX_CONTENT_SIZE = 1024 * 1024 * 1024;
    private static final File bufferTempDirectory = TempFileProvider.getTempDir(TEMP_DIRECTORY_NAME);

    @Test
    public void testInMemoryStream() throws IOException
    {
        TempOutputStreamFactory streamFactory = new TempOutputStreamFactory(bufferTempDirectory, MEMORY_THRESHOLD, MAX_CONTENT_SIZE, false, false);

        File file = createTextFileWithRandomContent(MEMORY_THRESHOLD - 1024L);
        {
            TempOutputStream outputStream = streamFactory.createOutputStream();

            long countBefore = countFilesInDirectoryWithPrefix(bufferTempDirectory);

            // Copy the stream
            StreamUtils.copy(new BufferedInputStream(new FileInputStream(file)), outputStream);

            long countAfter = countFilesInDirectoryWithPrefix(bufferTempDirectory);

            Assert.assertEquals(countBefore, countAfter);
            outputStream.destroy();
        }
        file.delete();
    }

    @Test
    public void testFileBackedStream() throws IOException
    {
        File file = createTextFileWithRandomContent(MEMORY_THRESHOLD + 1024L);

        {
            // Create stream factory that doesn't delete temp file on stream close
            TempOutputStreamFactory streamFactory = new TempOutputStreamFactory(bufferTempDirectory, MEMORY_THRESHOLD, MAX_CONTENT_SIZE, false, false);
            TempOutputStream outputStream = streamFactory.createOutputStream();

            long countBefore = countFilesInDirectoryWithPrefix(bufferTempDirectory);

            StreamUtils.copy(new BufferedInputStream(new FileInputStream(file)), outputStream);

            // Check that temp file was created
            long countAfter = countFilesInDirectoryWithPrefix(bufferTempDirectory);
            Assert.assertEquals(countBefore + 1, countAfter);

            outputStream.close();

            // Check that file wasn't deleted on output stream close
            countAfter = countFilesInDirectoryWithPrefix(bufferTempDirectory);
            Assert.assertEquals(countBefore + 1, countAfter);

            outputStream.destroy();

            // Check that file was deleted
            countAfter = countFilesInDirectoryWithPrefix(bufferTempDirectory);
            Assert.assertEquals(countBefore, countAfter);
        }

        {
            // Create stream factory that deletes temp file on stream close
            TempOutputStreamFactory streamFactory = new TempOutputStreamFactory(bufferTempDirectory, MEMORY_THRESHOLD, MAX_CONTENT_SIZE, false, true);
            TempOutputStream outputStream = streamFactory.createOutputStream();

            long countBefore = countFilesInDirectoryWithPrefix(bufferTempDirectory);

            StreamUtils.copy(new BufferedInputStream(new FileInputStream(file)), outputStream);

            // Check that temp file was created
            long countAfter = countFilesInDirectoryWithPrefix(bufferTempDirectory);
            Assert.assertEquals(countBefore + 1, countAfter);

            outputStream.close();

            // Check that file was deleted on close
            countAfter = countFilesInDirectoryWithPrefix(bufferTempDirectory);
            Assert.assertEquals(countBefore, countAfter);
        }

        file.delete();
    }

    @Test
    public void testMaxContentSize() throws IOException
    {
        // In memory stream
        {
            long contentSize = MEMORY_THRESHOLD - 512;
            long maxContentSize = MEMORY_THRESHOLD - 1024;

            File file = createTextFileWithRandomContent(contentSize);

            // Create stream factory that deletes temp file on stream close
            TempOutputStreamFactory streamFactory = new TempOutputStreamFactory(bufferTempDirectory, MEMORY_THRESHOLD, maxContentSize, false, true);
            TempOutputStream outputStream = streamFactory.createOutputStream();

            long countBefore = countFilesInDirectoryWithPrefix(bufferTempDirectory);

            try
            {
                StreamUtils.copy(new BufferedInputStream(new FileInputStream(file)), outputStream);
                Assert.fail("Content size limit violation exception was expected");
            }
            catch (ContentLimitViolationException e)
            {
                // Expected
            }

            // Check that file was already deleted on close
            long countAfter = countFilesInDirectoryWithPrefix(bufferTempDirectory);
            Assert.assertEquals(countBefore, countAfter);

            file.delete();
        }

        // File backed stream
        {
            long contentSize = MEMORY_THRESHOLD + 1024;
            long maxContentSize = MEMORY_THRESHOLD + 512;

            File file = createTextFileWithRandomContent(contentSize);

            // Create stream factory that deletes temp file on stream close
            TempOutputStreamFactory streamFactory = new TempOutputStreamFactory(bufferTempDirectory, MEMORY_THRESHOLD, maxContentSize, false, true);
            TempOutputStream outputStream = streamFactory.createOutputStream();

            long countBefore = countFilesInDirectoryWithPrefix(bufferTempDirectory);

            try
            {
                StreamUtils.copy(new BufferedInputStream(new FileInputStream(file)), outputStream);
                Assert.fail("Content size limit violation exception was expected");
            }
            catch (ContentLimitViolationException e)
            {
                // Expected
            }

            // Check that file was already deleted on close
            long countAfter = countFilesInDirectoryWithPrefix(bufferTempDirectory);
            Assert.assertEquals(countBefore, countAfter);

            file.delete();
        }
    }

    @Test
    public void testEncryptContent() throws IOException
    {
        File file = createTextFileWithRandomContent(MEMORY_THRESHOLD + 1024L);

        // Create stream factory that doesn't delete temp file on stream close
        TempOutputStreamFactory streamFactory = new TempOutputStreamFactory(bufferTempDirectory, MEMORY_THRESHOLD, MAX_CONTENT_SIZE, true, false);

        TempOutputStream outputStream = streamFactory.createOutputStream();

        long countBefore = countFilesInDirectoryWithPrefix(bufferTempDirectory);

        StreamUtils.copy(new BufferedInputStream(new FileInputStream(file)), outputStream);

        // Check that temp file was created
        long countAfter = countFilesInDirectoryWithPrefix(bufferTempDirectory);
        Assert.assertEquals(countBefore + 1, countAfter);

        outputStream.close();

        // Check that file wasn't deleted on output stream close
        countAfter = countFilesInDirectoryWithPrefix(bufferTempDirectory);
        Assert.assertEquals(countBefore + 1, countAfter);

        // Compare content
        String contentWriten = StreamUtils.copyToString(new BufferedInputStream(new FileInputStream(file)), Charset.defaultCharset());
        String contentRead = StreamUtils.copyToString(outputStream.getInputStream(), Charset.defaultCharset());
        Assert.assertEquals(contentWriten, contentRead);

        outputStream.destroy();

        // Check that file was deleted
        countAfter = countFilesInDirectoryWithPrefix(bufferTempDirectory);
        Assert.assertEquals(countBefore, countAfter);

        file.delete();
    }

    private File createTextFileWithRandomContent(long contentSize) throws IOException
    {
        File txtFile = TempFileProvider.createTempFile(getClass().getSimpleName(), ".txt");
        txtFile.deleteOnExit();

        RandomAccessFile file = new RandomAccessFile(txtFile.getPath(), "rw");
        file.setLength(contentSize);
        file.close();

        return txtFile;
    }

    private long countFilesInDirectoryWithPrefix(File directory) throws IOException
    {
        Stream<File> fileStream = Arrays.stream(directory.listFiles());
        return fileStream.filter(f -> f.getName().startsWith(FILE_PREFIX)).count();
    }
}

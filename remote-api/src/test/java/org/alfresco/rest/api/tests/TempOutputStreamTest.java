
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
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.util.StreamUtils;

import org.alfresco.repo.content.ContentLimitViolationException;
import org.alfresco.repo.web.scripts.TempOutputStream;
import org.alfresco.util.TempFileProvider;

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
        Supplier<TempOutputStream> streamFactory = TempOutputStream.factory(bufferTempDirectory,
                MEMORY_THRESHOLD, MAX_CONTENT_SIZE, false);

        File file = createTextFileWithRandomContent(MEMORY_THRESHOLD - 1024L);
        TempOutputStream outputStream = streamFactory.get();
        try (TempOutputStream closeableStream = outputStream)
        {
            long countBefore = countFilesInDirectoryWithPrefix(bufferTempDirectory);

            try (BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(file.toPath())))
            {
                StreamUtils.copy(inputStream, outputStream);
            }

            long countAfter = countFilesInDirectoryWithPrefix(bufferTempDirectory);

            Assert.assertEquals(countBefore, countAfter);
        }
        finally
        {
            outputStream.destroy();
            Files.deleteIfExists(file.toPath());
        }
    }

    @Test
    public void testFileBackedStream() throws IOException
    {
        File file = createTextFileWithRandomContent(MEMORY_THRESHOLD + 1024L);
        long countBefore = countFilesInDirectoryWithPrefix(bufferTempDirectory);
        // Create stream factory that doesn't delete temp file on stream close
        Supplier<TempOutputStream> streamFactory = TempOutputStream.factory(bufferTempDirectory, MEMORY_THRESHOLD, MAX_CONTENT_SIZE, false);
        TempOutputStream outputStream = streamFactory.get();

        try (TempOutputStream closeableStream = outputStream)
        {
            try (BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(file.toPath())))
            {
                StreamUtils.copy(inputStream, outputStream);
            }

            // Check that temp file was created
            long countAfter = countFilesInDirectoryWithPrefix(bufferTempDirectory);
            Assert.assertEquals(countBefore + 1, countAfter);

            outputStream.close();

            // Check that file wasn't deleted on output stream close
            countAfter = countFilesInDirectoryWithPrefix(bufferTempDirectory);
            Assert.assertEquals(countBefore + 1, countAfter);
        }
        finally
        {
            outputStream.destroy();
            Files.deleteIfExists(file.toPath());
        }

        long countAfter = countFilesInDirectoryWithPrefix(bufferTempDirectory);
        Assert.assertEquals(countBefore, countAfter);
    }

    @Test(expected = ContentLimitViolationException.class)
    public void testMaxContentSizeInMemoryStream() throws IOException
    {
        long contentSize = MEMORY_THRESHOLD - 512;
        long maxContentSize = MEMORY_THRESHOLD - 1024;

        File file = createTextFileWithRandomContent(contentSize);

        // Create stream factory that deletes the temp file when the max Size is reached
        Supplier<TempOutputStream> streamFactory = TempOutputStream.factory(bufferTempDirectory, MEMORY_THRESHOLD, maxContentSize, false);
        TempOutputStream outputStream = streamFactory.get();

        long countBefore = countFilesInDirectoryWithPrefix(bufferTempDirectory);

        try (TempOutputStream closeableStream = outputStream)
        {
            try (BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(file.toPath())))
            {
                StreamUtils.copy(inputStream, outputStream);
            }
        }
        finally
        {
            // Check that file was already deleted on error
            long countAfter = countFilesInDirectoryWithPrefix(bufferTempDirectory);
            Assert.assertEquals(countBefore, countAfter);

            outputStream.destroy();
            Files.deleteIfExists(file.toPath());
        }
    }

    @Test(expected = ContentLimitViolationException.class)
    public void testMaxContentSizeFileBackedStream() throws IOException
    {
        long contentSize = MEMORY_THRESHOLD + 1024;
        long maxContentSize = MEMORY_THRESHOLD + 512;

        File file = createTextFileWithRandomContent(contentSize);

        // Create stream factory that deletes the temp file when the max Size is reached
        Supplier<TempOutputStream> streamFactory = TempOutputStream.factory(bufferTempDirectory, MEMORY_THRESHOLD, maxContentSize, false);
        TempOutputStream outputStream = streamFactory.get();

        long countBefore = countFilesInDirectoryWithPrefix(bufferTempDirectory);

        try (TempOutputStream closeableStream = outputStream)
        {
            try (BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(file.toPath())))
            {
                StreamUtils.copy(inputStream, outputStream);
            }
        }
        finally
        {
            // Check that file was already deleted on error
            long countAfter = countFilesInDirectoryWithPrefix(bufferTempDirectory);
            Assert.assertEquals(countBefore, countAfter);

            outputStream.destroy();
            Files.deleteIfExists(file.toPath());
        }
    }

    @Test(expected = ContentLimitViolationException.class)
    public void testToNewInputStreamAfterMaxContentSizeExceededForFileBackedStream() throws IOException
    {
        long maxContentSize = MEMORY_THRESHOLD + 512;
        File file = createTextFileWithRandomContent(MEMORY_THRESHOLD + 1024L);

        Supplier<TempOutputStream> streamFactory = TempOutputStream.factory(bufferTempDirectory, MEMORY_THRESHOLD,
                maxContentSize, false);
        TempOutputStream outputStream = streamFactory.get();

        try (TempOutputStream closeableStream = outputStream)
        {
            try (BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(file.toPath())))
            {
                StreamUtils.copy(inputStream, outputStream);
            }
        }
        catch (ContentLimitViolationException e)
        {
            // Expected during setup.
        }

        try
        {
            outputStream.toNewInputStream();
        }
        finally
        {
            outputStream.destroy();
            Files.deleteIfExists(file.toPath());
        }
    }

    @Test
    public void testEncryptContent() throws IOException
    {
        File file = createTextFileWithRandomContent(MEMORY_THRESHOLD + 1024L);

        // Create stream factory that doesn't delete temp file on stream close
        Supplier<TempOutputStream> streamFactory = TempOutputStream.factory(bufferTempDirectory, MEMORY_THRESHOLD, MAX_CONTENT_SIZE, true);

        TempOutputStream outputStream = streamFactory.get();
        long countBefore = countFilesInDirectoryWithPrefix(bufferTempDirectory);

        try (TempOutputStream closeableStream = outputStream)
        {
            try (BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(file.toPath())))
            {
                StreamUtils.copy(inputStream, outputStream);
            }

            // Check that temp file was created
            long countAfter = countFilesInDirectoryWithPrefix(bufferTempDirectory);
            Assert.assertEquals(countBefore + 1, countAfter);

            outputStream.close();

            // Check that file wasn't deleted on output stream close
            countAfter = countFilesInDirectoryWithPrefix(bufferTempDirectory);
            Assert.assertEquals(countBefore + 1, countAfter);

            // Compare content
            String contentWriten;
            try (BufferedInputStream inputStream = new BufferedInputStream(Files.newInputStream(file.toPath())))
            {
                contentWriten = StreamUtils.copyToString(inputStream, Charset.defaultCharset());
            }

            String contentRead;
            try (BufferedInputStream inputStream = new BufferedInputStream(outputStream.toNewInputStream()))
            {
                contentRead = StreamUtils.copyToString(inputStream, Charset.defaultCharset());
            }
            Assert.assertEquals(contentWriten, contentRead);
        }
        finally
        {
            outputStream.destroy();
            Files.deleteIfExists(file.toPath());
        }

        long countAfter = countFilesInDirectoryWithPrefix(bufferTempDirectory);
        Assert.assertEquals(countBefore, countAfter);
    }

    private File createTextFileWithRandomContent(long contentSize) throws IOException
    {
        File txtFile = TempFileProvider.createTempFile(getClass().getSimpleName(), ".txt");
        txtFile.deleteOnExit();

        try (RandomAccessFile file = new RandomAccessFile(txtFile.getPath(), "rw"))
        {
            file.setLength(contentSize);
        }

        return txtFile;
    }

    private long countFilesInDirectoryWithPrefix(File directory) throws IOException
    {
        Stream<File> fileStream = Arrays.stream(directory.listFiles());
        return fileStream.filter(f -> f.getName().startsWith(FILE_PREFIX)).count();
    }
}

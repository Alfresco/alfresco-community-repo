
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

import org.alfresco.repo.web.scripts.BufferedResponse;
import org.alfresco.repo.web.scripts.TempOutputStream;
import org.alfresco.repo.web.scripts.TempOutputStreamFactory;
import org.alfresco.util.TempFileProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Test that BufferedResponse uses a temp file instead of buffering the entire output stream in memory
 *
 * @author Andrei Zapodeanu
 * @author azapodeanu
 */
public class BufferedResponseTest
{
    private static final String TEMP_FOLDER_PATH = TempFileProvider.getTempDir().getAbsolutePath();

    private static final String TEMP_DIRECTORY_NAME = "testLargeFile";
    private static final String LARGE_FILE_NAME = "largeFile.tmp";
    private static final String FILE_PREFIX = TempOutputStream.TEMP_FILE_PREFIX;

    private static final Integer LARGE_FILE_SIZE_BYTES = 5 * 1024 * 1024;
    private static final Integer MEMORY_THRESHOLD = 4 * 1024 * 1024;
    private static final Integer MAX_CONTENT_SIZE = 1024 * 1024 * 1024;

    @Before
    public void createSourceFile() throws IOException
    {
        createRandomFileInDirectory(TEMP_FOLDER_PATH, LARGE_FILE_NAME, LARGE_FILE_SIZE_BYTES);
    }

    @After
    public void tearDown() throws Exception
    {
        File largeFileSource = new File(TEMP_FOLDER_PATH, LARGE_FILE_NAME);
        largeFileSource.delete();
    }

    /**
     * Test that the output stream creates a temp file to cache its content when file size was bigger than its memory threshold ( 5 > 4 MB )
     * MNT-19833
     */
    @Test
    public void testOutputStream() throws IOException
    {
        File bufferTempDirectory = TempFileProvider.getTempDir(TEMP_DIRECTORY_NAME);
        TempOutputStreamFactory streamFactory = new TempOutputStreamFactory(bufferTempDirectory, MEMORY_THRESHOLD, MAX_CONTENT_SIZE, false,true);
        BufferedResponse response = new BufferedResponse(null, 0, streamFactory);

        long countBefore = countFilesInDirectoryWithPrefix(bufferTempDirectory, FILE_PREFIX );
        copyFileToOutputStream(response);
        long countAfter = countFilesInDirectoryWithPrefix(bufferTempDirectory, FILE_PREFIX);
        
        response.getOutputStream().close();

        Assert.assertEquals(countBefore + 1, countAfter);

    }

    private void copyFileToOutputStream(BufferedResponse response) throws IOException
    {
        File largeFileSource = new File(TEMP_FOLDER_PATH, LARGE_FILE_NAME);
        OutputStream testOutputStream = response.getOutputStream();
        Files.copy(largeFileSource.toPath(), testOutputStream);
    }

    private void createRandomFileInDirectory(String path, String fileName, int size) throws IOException
    {
        String fullPath = new File(path, fileName).getPath();
        RandomAccessFile file = new RandomAccessFile(fullPath,"rw");
        file.setLength(size);
        file.close();
    }

    private long countFilesInDirectoryWithPrefix(File directory, String filePrefix) throws IOException
    {
        Stream<File> fileStream = Arrays.stream(directory.listFiles());
        return fileStream.filter( f -> f.getName().startsWith(filePrefix)).count();
    }
}


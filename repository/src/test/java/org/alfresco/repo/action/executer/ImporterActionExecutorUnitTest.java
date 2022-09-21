/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.repo.action.executer;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.alfresco.util.GUID;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.junit.Test;

/**
 * Unit tests for import action executor.
 *
 */
public class ImporterActionExecutorUnitTest
{
    private static final String TEST_RESOURCE_PATHNAME = "import-archive-test";

    @Test
    public void testExtractFileDoesNotFailWhenCalledWithNoTracker() throws Exception
    {
        // create test folder in which to extract the zip file
        final File destinationDir = TempFileProvider.getTempDir(GUID.generate());

        // zip file to be extracted
        final ZipFile zipFile = getZipResource("exceedsRatio.zip");

        // extract the zip file to the destination folder
        assertTrue("Expected destination folder to be empty.", destinationDir.list().length == 0);
        ImporterActionExecuter.extractFile(zipFile, destinationDir.getPath());
        assertTrue("Expected destination folder to contain at least one file.", destinationDir.list().length > 0);

        ImporterActionExecuter.deleteDir(destinationDir);
    }

    @SuppressWarnings("SameParameterValue")
    private ZipFile getZipResource(String zipFileName) throws URISyntaxException, IOException
    {
        final String zipFilePath = TEST_RESOURCE_PATHNAME + File.separator + zipFileName;
        final URL url = ImporterActionExecutorUnitTest.class.getClassLoader().getResource(zipFilePath);
        final Path path = Paths.get(url.toURI());
        final File file = new File(path.toString());
        return new ZipFile(file, "UTF-8", true);
    }
}
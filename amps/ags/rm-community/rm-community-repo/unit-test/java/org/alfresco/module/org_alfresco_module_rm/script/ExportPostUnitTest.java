/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.script;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the ACP archive injection performed by {@link ExportPost}.
 *
 * @author Alfresco
 */
public class ExportPostUnitTest
{
    private ExportPost exportPost;
    private File archive;

    @Before
    public void setUp() throws IOException
    {
        exportPost = new ExportPost();
        archive = createZipArchive();
    }

    @After
    public void tearDown()
    {
        if (archive != null)
        {
            archive.delete();
            archive = null;
        }
    }

    @Test
    public void addFileToArchiveAddsEntryWithContents() throws IOException
    {
        File payload = File.createTempFile("payload", ".txt");
        try
        {
            Files.write(payload.toPath(), "hello world".getBytes(StandardCharsets.UTF_8));

            exportPost.addFileToArchive(archive, payload, "added.txt");

            String content = readEntry(archive, "added.txt");
            assertNotNull("Expected the injected entry to be present in the archive", content);
            assertTrue("Injected entry should carry the source file contents", content.contains("hello world"));
        }
        finally
        {
            payload.delete();
        }
    }

    @Test
    public void addSearchResultsCsvEmbedsTimestampedCsv() throws IOException
    {
        JSONArray headers = new JSONArray();
        headers.put("ID");
        headers.put("Name");

        JSONArray row = new JSONArray();
        row.put("1");
        row.put("Report");

        JSONArray rows = new JSONArray();
        rows.put(row);

        JSONObject items = new JSONObject();
        items.put(SearchResultsCSVWriter.PARAM_HEADERS, headers);
        items.put(SearchResultsCSVWriter.PARAM_ROWS, rows);

        exportPost.addSearchResultsCsv(archive, items);

        String entryName = findEntryMatching(archive, SearchResultsCSVWriter.CSV_FILE_NAME_PREFIX, ".csv");
        assertNotNull("ACP should contain the embedded search-results CSV entry", entryName);

        String csv = readEntry(archive, entryName);
        assertTrue("CSV should contain the supplied headers", csv.contains("ID"));
        assertTrue("CSV should contain the supplied row data", csv.contains("Report"));
    }

    private static File createZipArchive() throws IOException
    {
        File file = File.createTempFile("export", ".acp");
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(file.toPath())))
        {
            zos.putNextEntry(new ZipEntry("export.acp"));
            zos.write("<acp/>".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        return file;
    }

    private static String findEntryMatching(File zip, String prefix, String suffix) throws IOException
    {
        try (ZipInputStream in = new ZipInputStream(Files.newInputStream(zip.toPath())))
        {
            ZipEntry entry = in.getNextEntry();
            while (entry != null)
            {
                String name = entry.getName();
                if (name.startsWith(prefix) && name.endsWith(suffix))
                {
                    return name;
                }
                entry = in.getNextEntry();
            }
        }
        return null;
    }

    private static String readEntry(File zip, String entryName) throws IOException
    {
        try (ZipInputStream in = new ZipInputStream(Files.newInputStream(zip.toPath())))
        {
            ZipEntry entry = in.getNextEntry();
            while (entry != null)
            {
                if (entry.getName().equals(entryName))
                {
                    return new String(readAllBytes(in), StandardCharsets.UTF_8);
                }
                entry = in.getNextEntry();
            }
        }
        return null;
    }

    private static byte[] readAllBytes(InputStream in) throws IOException
    {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        copy(in, out);
        return out.toByteArray();
    }

    private static void copy(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[4096];
        int read = in.read(buffer);
        while (read != -1)
        {
            out.write(buffer, 0, read);
            read = in.read(buffer);
        }
    }
}

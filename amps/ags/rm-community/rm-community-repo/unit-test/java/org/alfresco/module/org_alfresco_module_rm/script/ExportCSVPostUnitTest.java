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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.springframework.extensions.webscripts.WebScriptException;

/**
 * Unit tests for {@link ExportCSVPost}.
 *
 * These tests exercise {@link ExportCSVPost#createCSVFile(JSONObject)} directly and therefore do not require a Spring
 * application context.
 */
public class ExportCSVPostUnitTest
{
    private final ExportCSVPost exportCSVPost = new ExportCSVPost();

    /** Reads the given CSV file as a UTF-8 string, deleting it afterwards. */
    private String readAndDelete(File file) throws Exception
    {
        try
        {
            return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        }
        finally
        {
            file.delete();
        }
    }

    private JSONObject csvBody(JSONArray headers, JSONArray rows)
    {
        JSONObject items = new JSONObject();
        items.put("headers", headers);
        if (rows != null)
        {
            items.put("rows", rows);
        }
        return new JSONObject().put("items", items);
    }

    @Test
    public void headersAndRowsAreWritten() throws Exception
    {
        JSONArray headers = new JSONArray().put("ID").put("Name").put("Author");
        JSONArray rows = new JSONArray().put(
                new JSONArray().put("2026-1").put("record.docx").put("Jane Doe"));

        File csv = exportCSVPost.createCSVFile(csvBody(headers, rows));
        String content = readAndDelete(csv);

        // UTF-8 BOM is written first so spreadsheets detect the encoding
        assertEquals('\uFEFF', content.charAt(0));
        assertTrue(content.contains("ID,Name,Author"));
        assertTrue(content.contains("2026-1,record.docx,Jane Doe"));
        // rows are separated by CRLF
        assertTrue(content.contains("\r\n"));
    }

    @Test
    public void missingItemsIsRejected()
    {
        try
        {
            exportCSVPost.createCSVFile(new JSONObject());
            org.junit.Assert.fail("Expected a WebScriptException");
        }
        catch (WebScriptException e)
        {
            assertEquals(400, e.getStatus());
        }
    }

    @Test
    public void missingHeadersIsRejected()
    {
        JSONObject items = new JSONObject();
        items.put("rows", new JSONArray());
        JSONObject body = new JSONObject().put("items", items);

        try
        {
            exportCSVPost.createCSVFile(body);
            org.junit.Assert.fail("Expected a WebScriptException");
        }
        catch (WebScriptException e)
        {
            assertEquals(400, e.getStatus());
        }
    }

    @Test
    public void formulaValuesAreSanitised() throws Exception
    {
        JSONArray headers = new JSONArray().put("Name");
        JSONArray rows = new JSONArray().put(new JSONArray().put("=SUM(A1:A2)"));

        File csv = exportCSVPost.createCSVFile(csvBody(headers, rows));
        String content = readAndDelete(csv);

        // the leading '=' must be neutralised with a single quote to avoid CSV formula injection
        assertTrue(content.contains("'=SUM(A1:A2)"));
    }

    @Test
    public void raggedRowsArePaddedAndTruncatedToColumnCount() throws Exception
    {
        JSONArray headers = new JSONArray().put("A").put("B").put("C");
        JSONArray rows = new JSONArray()
                .put(new JSONArray().put("only-one"))
                .put(new JSONArray().put("x").put("y").put("z").put("extra"));

        File csv = exportCSVPost.createCSVFile(csvBody(headers, rows));
        String content = readAndDelete(csv);

        // short row padded to three columns
        assertTrue(content.contains("only-one,,"));
        // long row truncated to three columns (the fourth value dropped)
        assertTrue(content.contains("x,y,z"));
        assertFalse(content.contains("extra"));
    }

    @Test
    public void nullCellsAreRenderedAsEmpty() throws Exception
    {
        JSONArray headers = new JSONArray().put("A").put("B");
        JSONArray row = new JSONArray();
        row.put("value");
        row.put(JSONObject.NULL);
        JSONArray rows = new JSONArray().put(row);

        File csv = exportCSVPost.createCSVFile(csvBody(headers, rows));
        String content = readAndDelete(csv);

        assertTrue(content.contains("value,"));
    }
}

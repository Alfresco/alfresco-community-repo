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
import static org.junit.Assert.fail;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;

/**
 * Unit tests for {@link SearchResultsCSVWriter}.
 *
 * @author Alfresco
 */
public class SearchResultsCSVWriterUnitTest
{
    private SearchResultsCSVWriter writer;
    private File generated;

    @Before
    public void setUp()
    {
        writer = new SearchResultsCSVWriter();
    }

    @After
    public void tearDown()
    {
        if (generated != null)
        {
            generated.delete();
            generated = null;
        }
    }

    private String csvContent(JSONObject items) throws Exception
    {
        generated = writer.createCSVFile(items);
        String content = new String(Files.readAllBytes(generated.toPath()), StandardCharsets.UTF_8);
        // strip the leading UTF-8 BOM for easier assertions
        if (!content.isEmpty() && content.charAt(0) == '\uFEFF')
        {
            content = content.substring(1);
        }
        return content;
    }

    @Test
    public void headersAndRowsAreWritten() throws Exception
    {
        JSONObject items = new JSONObject();
        items.put("headers", new JSONArray().put("ID").put("Name"));
        items.put("rows", new JSONArray().put(new JSONArray().put("2026-1").put("record.docx")));

        String content = csvContent(items);
        assertEquals("ID,Name\r\n2026-1,record.docx\r\n", content);
    }

    @Test
    public void missingItemsIsRejected()
    {
        try
        {
            writer.createCSVFile(null);
            fail("Expected a WebScriptException for missing items");
        }
        catch (WebScriptException e)
        {
            assertEquals(Status.STATUS_BAD_REQUEST, e.getStatus());
        }
    }

    @Test
    public void missingHeadersIsRejected()
    {
        JSONObject items = new JSONObject();
        items.put("rows", new JSONArray());
        try
        {
            writer.createCSVFile(items);
            fail("Expected a WebScriptException for missing headers");
        }
        catch (WebScriptException e)
        {
            assertEquals(Status.STATUS_BAD_REQUEST, e.getStatus());
        }
    }

    @Test
    public void formulaValuesAreSanitised() throws Exception
    {
        JSONObject items = new JSONObject();
        items.put("headers", new JSONArray().put("Value"));
        items.put("rows", new JSONArray().put(new JSONArray().put("=1+1")));

        String content = csvContent(items);
        assertTrue("Formula value should be prefixed with a single quote", content.contains("'=1+1"));
    }

    @Test
    public void raggedRowsArePaddedAndTruncatedToColumnCount() throws Exception
    {
        JSONObject items = new JSONObject();
        items.put("headers", new JSONArray().put("A").put("B").put("C"));
        JSONArray rows = new JSONArray();
        rows.put(new JSONArray().put("1"));                          // short row -> padded
        rows.put(new JSONArray().put("x").put("y").put("z").put("w")); // long row -> truncated
        items.put("rows", rows);

        String content = csvContent(items);
        assertEquals("A,B,C\r\n1,,\r\nx,y,z\r\n", content);
    }

    @Test
    public void nullCellsAreRenderedAsEmpty() throws Exception
    {
        JSONObject items = new JSONObject();
        items.put("headers", new JSONArray().put("A").put("B"));
        items.put("rows", new JSONArray().put(new JSONArray().put(JSONObject.NULL).put("y")));

        String content = csvContent(items);
        // commons-csv quotes an empty leading field as "" to avoid an ambiguous empty line
        assertEquals("A,B\r\n\"\",y\r\n", content);
        assertFalse(content.contains("null"));
    }
}

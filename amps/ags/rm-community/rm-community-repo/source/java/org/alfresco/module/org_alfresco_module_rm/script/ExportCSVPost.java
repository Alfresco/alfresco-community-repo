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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import org.alfresco.repo.web.scripts.content.ContentStreamer;
import org.alfresco.util.TempFileProvider;

/**
 * Exports the tabular data of a records search result as a CSV file and streams it back to the client.
 * <p>
 * Only the metadata that is displayed in the search results is exported; no node content is included. The caller supplies the already rendered table (column headers and row values) so that the generated CSV matches exactly what the user sees on screen. The body of the POST should be in the form:
 *
 * <pre>
 * {
 *    "items": {
 *       "headers": ["ID", "Name", ...],
 *       "rows": [["2026-1", "record.docx", ...], ...]
 *    }
 * }
 * </pre>
 *
 * @author Alfresco
 */
public class ExportCSVPost extends AbstractWebScript
{
    /** Logger */
    private static final Log logger = LogFactory.getLog(ExportCSVPost.class);

    protected static final String TEMP_FILE_PREFIX = "export_";
    protected static final String CSV_EXTENSION = "csv";

    /** Friendly download name used for the CSV attachment. */
    protected static final String CSV_FILE_NAME = "search-results.csv";

    /** Body elements holding the tabular data for the CSV export. */
    protected static final String PARAM_ITEMS = "items";
    protected static final String PARAM_HEADERS = "headers";
    protected static final String PARAM_ROWS = "rows";

    /** Content Streamer */
    private ContentStreamer contentStreamer;

    /**
     * @param contentStreamer
     *            the content streamer used to stream the generated CSV back to the client
     */
    public void setContentStreamer(ContentStreamer contentStreamer)
    {
        this.contentStreamer = contentStreamer;
    }

    /**
     * @see org.springframework.extensions.webscripts.WebScript#execute(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse)
     */
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        File tempCSVFile = null;
        try
        {
            JSONObject json = new JSONObject(new JSONTokener(req.getContent().getContent()));

            // create a CSV of the supplied search result table
            tempCSVFile = createCSVFile(json);

            // stream the CSV back to the client as an attachment (forcing save as)
            contentStreamer.streamContent(req, res, tempCSVFile, null, true, CSV_FILE_NAME, null);
        }
        catch (IOException ioe)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Could not read content from req.", ioe);
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Could not parse JSON from req.", je);
        }
        catch (Exception e)
        {
            if (logger.isDebugEnabled())
            {
                StringWriter stack = new StringWriter();
                e.printStackTrace(new PrintWriter(stack));
                logger.debug("Caught exception; decorating with appropriate status template : " + stack.toString());
            }

            throw createStatusException(e, req, res);
        }
        finally
        {
            // try and delete the temporary file
            if (tempCSVFile != null)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Deleting temporary CSV file: " + tempCSVFile.getAbsolutePath());
                }

                tempCSVFile.delete();
            }
        }
    }

    /**
     * Builds a CSV file from the tabular search-result data supplied in the request body and returns it as a temporary file.
     *
     * @param json
     *            the parsed request body
     * @return a temporary CSV file (the caller is responsible for deleting it)
     */
    protected File createCSVFile(JSONObject json)
    {
        if (!json.has(PARAM_ITEMS))
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Mandatory 'items' parameter was not provided in request body");
        }

        try
        {
            JSONObject items = json.getJSONObject(PARAM_ITEMS);
            JSONArray headers = items.optJSONArray(PARAM_HEADERS);
            if (headers == null || headers.length() == 0)
            {
                throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Mandatory 'headers' were not provided for CSV export");
            }

            int columnCount = headers.length();
            JSONArray rows = items.optJSONArray(PARAM_ROWS);

            File csvFile = TempFileProvider.createTempFile(TEMP_FILE_PREFIX, "." + CSV_EXTENSION);

            try (Writer writer = new OutputStreamWriter(new FileOutputStream(csvFile), StandardCharsets.UTF_8);
                    CSVPrinter printer = new CSVPrinter(writer, CSVFormat.DEFAULT.withRecordSeparator("\r\n")))
            {
                // UTF-8 BOM so spreadsheet applications correctly detect the encoding
                writer.write('\uFEFF');

                printer.printRecord(toSanitisedList(headers, columnCount));

                if (rows != null)
                {
                    for (int i = 0; i < rows.length(); i++)
                    {
                        printer.printRecord(toSanitisedList(rows.getJSONArray(i), columnCount));
                    }
                }

                printer.flush();
            }

            if (logger.isDebugEnabled())
            {
                logger.debug("Created temporary CSV file: " + csvFile.getAbsolutePath());
            }

            return csvFile;
        }
        catch (JSONException je)
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST,
                    "Could not parse CSV data from request body.", je);
        }
        catch (IOException ioe)
        {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR,
                    "Failed to create CSV file.", ioe);
        }
    }

    /**
     * Converts a JSON array of cell values into a fixed-length list of {@code columnCount} strings. Missing cells are rendered as empty strings and each value is guarded against CSV formula injection.
     *
     * @param values
     *            the JSON array of cell values
     * @param columnCount
     *            the number of columns to produce
     * @return the sanitised, fixed-length list of cell values
     */
    private List<String> toSanitisedList(JSONArray values, int columnCount) throws JSONException
    {
        List<String> record = new ArrayList<>(columnCount);
        for (int c = 0; c < columnCount; c++)
        {
            String cell = (c < values.length() && !values.isNull(c)) ? values.getString(c) : "";
            record.add(sanitiseForCsv(cell));
        }
        return record;
    }

    /**
     * Guards against CSV formula injection by prefixing values that a spreadsheet application could interpret as a formula with a single quote.
     *
     * @param value
     *            the cell value
     * @return the safe cell value
     */
    private String sanitiseForCsv(String value)
    {
        if (value != null && !value.isEmpty())
        {
            char first = value.charAt(0);
            if (first == '=' || first == '+' || first == '-' || first == '@' || first == '\t' || first == '\r')
            {
                return "'" + value;
            }
        }
        return value;
    }
}

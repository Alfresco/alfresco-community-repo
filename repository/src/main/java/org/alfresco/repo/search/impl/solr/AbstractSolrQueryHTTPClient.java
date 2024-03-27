/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2024 Alfresco Software Limited
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
package org.alfresco.repo.search.impl.solr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.alfresco.repo.search.QueryParserException;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public abstract class AbstractSolrQueryHTTPClient
{
    /** Logger for the class. */
    private static final Log LOGGER = LogFactory.getLog(AbstractSolrQueryHTTPClient.class);

    public static final int DEFAULT_SAVEPOST_BUFFER = 4096;
    
    // Constants copied from org.apache.solr.common.params.HighlightParams (solr-solrj:1.4.1)
    // These values have been moved to this Alfresco class to avoid using solr-solrj library as dependency
    public static final String HIGHLIGHT_PARAMS_HIGHLIGHT = "hl";
    public static final String HIGHLIGHT_PARAMS_FIELDS = HIGHLIGHT_PARAMS_HIGHLIGHT + ".fl";
    public static final String HIGHLIGHT_PARAMS_SNIPPETS = HIGHLIGHT_PARAMS_HIGHLIGHT + ".snippets";
    public static final String HIGHLIGHT_PARAMS_FRAGSIZE = HIGHLIGHT_PARAMS_HIGHLIGHT + ".fragsize";
    public static final String HIGHLIGHT_PARAMS_INCREMENT = HIGHLIGHT_PARAMS_HIGHLIGHT + ".increment";
    public static final String HIGHLIGHT_PARAMS_MAX_CHARS = HIGHLIGHT_PARAMS_HIGHLIGHT + ".maxAnalyzedChars";
    public static final String HIGHLIGHT_PARAMS_FORMATTER = HIGHLIGHT_PARAMS_HIGHLIGHT + ".formatter";
    public static final String HIGHLIGHT_PARAMS_FRAGMENTER = HIGHLIGHT_PARAMS_HIGHLIGHT + ".fragmenter";
    public static final String HIGHLIGHT_PARAMS_FIELD_MATCH = HIGHLIGHT_PARAMS_HIGHLIGHT + ".requireFieldMatch";
    public static final String HIGHLIGHT_PARAMS_ALTERNATE_FIELD = HIGHLIGHT_PARAMS_HIGHLIGHT + ".alternateField";
    public static final String HIGHLIGHT_PARAMS_ALTERNATE_FIELD_LENGTH = HIGHLIGHT_PARAMS_HIGHLIGHT + ".maxAlternateFieldLength";

    public static final String HIGHLIGHT_PARAMS_USE_PHRASE_HIGHLIGHTER = HIGHLIGHT_PARAMS_HIGHLIGHT + ".usePhraseHighlighter";
    public static final String HIGHLIGHT_PARAMS_HIGHLIGHT_MULTI_TERM = HIGHLIGHT_PARAMS_HIGHLIGHT + ".highlightMultiTerm";

    public static final String HIGHLIGHT_PARAMS_MERGE_CONTIGUOUS_FRAGMENTS = HIGHLIGHT_PARAMS_HIGHLIGHT + ".mergeContiguous";
    // Formatter
    public static final String HIGHLIGHT_PARAMS_SIMPLE = "simple";
    public static final String HIGHLIGHT_PARAMS_SIMPLE_PRE = HIGHLIGHT_PARAMS_HIGHLIGHT + "." + HIGHLIGHT_PARAMS_SIMPLE + ".pre";
    public static final String HIGHLIGHT_PARAMS_SIMPLE_POST = HIGHLIGHT_PARAMS_HIGHLIGHT + "." + HIGHLIGHT_PARAMS_SIMPLE + ".post";

    // Regex fragmenter
    public static final String HIGHLIGHT_PARAMS_REGEX = "regex";
    public static final String HIGHLIGHT_PARAMS_SLOP = HIGHLIGHT_PARAMS_HIGHLIGHT + "." + HIGHLIGHT_PARAMS_REGEX + ".slop";
    public static final String HIGHLIGHT_PARAMS_PATTERN = HIGHLIGHT_PARAMS_HIGHLIGHT + "." + HIGHLIGHT_PARAMS_REGEX + ".pattern";
    public static final String HIGHLIGHT_PARAMS_MAX_RE_CHARS = HIGHLIGHT_PARAMS_HIGHLIGHT + "." + HIGHLIGHT_PARAMS_REGEX + ".maxAnalyzedChars";

    /** List of SOLR Exceptions that should be returning HTTP 501 status code in Remote API. */
    private static final List<String> STATUS_CODE_501_EXCEPTIONS = List.of("java.lang.UnsupportedOperationException");
    
    protected JSONObject postQuery(HttpClient httpClient, String url, JSONObject body) throws IOException, JSONException
    {
        PostMethod post = createNewPostMethod(url);
        if (body.toString().length() > DEFAULT_SAVEPOST_BUFFER)
        {
            post.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, true);
        }
        StringRequestEntity requestEntity = new StringRequestEntity(body.toString(), "application/json", "UTF-8");
        post.setRequestEntity(requestEntity);
        try
        {
            httpClient.executeMethod(post);
            if(post.getStatusCode() == HttpStatus.SC_MOVED_PERMANENTLY || post.getStatusCode() == HttpStatus.SC_MOVED_TEMPORARILY)
            {
                Header locationHeader = post.getResponseHeader("location");
                if (locationHeader != null)
                {
                    String redirectLocation = locationHeader.getValue();
                    post.setURI(new URI(redirectLocation, true));
                    httpClient.executeMethod(post);
                }
            }
            String responseBodyStr = post.getResponseBodyAsString();
            if (post.getStatusCode() != HttpServletResponse.SC_OK)
            {
                String trace = null;
                try
                {
                    trace = new JSONObject(responseBodyStr).getJSONObject("error").getString("trace");
                }
                catch (JSONException jsonException)
                {
                    LOGGER.warn("Node 'error.trace' is not present in Search Services error response: " + responseBodyStr);
                    LOGGER.warn("A generic error message will be provided. Check SOLR log file in order to find the root cause for this issue");
                }

                int httpStatusCode = post.getStatusCode();
                String message = "Solr request failed with " + httpStatusCode + " " + url;

                // Override the status code for certain exceptions with 501.
                if (trace != null)
                {
                    String traceException = trace.substring(0, trace.indexOf(":")).trim();
                    if (STATUS_CODE_501_EXCEPTIONS.contains(traceException))
                    {
                        httpStatusCode = org.apache.http.HttpStatus.SC_NOT_IMPLEMENTED;
                    }
                }
                throw new QueryParserException(message, httpStatusCode);
            }

            Reader reader = new BufferedReader(new InputStreamReader(post.getResponseBodyAsStream(), post.getResponseCharSet()));
            // TODO - replace with streaming-based solution e.g. SimpleJSON ContentHandler
            JSONObject json = new JSONObject(new JSONTokener(reader));
            return json;
        }
        finally
        {
            post.releaseConnection();
        }
    }

    /** Helper method that can be overridden by unit tests. */
    protected PostMethod createNewPostMethod(String url)
    {
        return new PostMethod(url);
    }
}

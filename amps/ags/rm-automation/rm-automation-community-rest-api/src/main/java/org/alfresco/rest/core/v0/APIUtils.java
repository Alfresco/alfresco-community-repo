/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.rest.core.v0;

import javax.json.Json;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper methods for use with REST APIs.
 *
 * @author Tom Page
 * @since 2.6
 */
public class APIUtils
{
    /** Logger for this class. */
    private static final Logger LOGGER = LoggerFactory.getLogger(APIUtils.class);
    /** The ISO instant formatter that formats or parses an instant in UTC, such as '2011-12-03T10:15:305Z'
     * similar with {@link DateTimeFormatter#ISO_INSTANT}, but with only 3 nanoseconds*/
    public static final DateTimeFormatter ISO_INSTANT_FORMATTER =
            new DateTimeFormatterBuilder().appendInstant(3).toFormatter();

    /** Private constructor for helper class. */
    private APIUtils()
    {
    }

    /**
     * Extract the body of a HTTP response as a JSON object.
     *
     * @param httpResponse The HTTP response.
     * @return A JSON representation of the object.
     */
    public static JSONObject convertHTTPResponseToJSON(HttpResponse httpResponse)
    {
        String source = null;
        try
        {
            source = IOUtils.toString(httpResponse.getEntity().getContent(), "UTF-8");
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException("Could not extract JSON from HTTP response.", e);
        }
        LOGGER.info("Response body:\n{}", source);
        return new JSONObject(source);
    }

    /**
     * Util method to extract the message string from the HTTP response
     *
     * @param httpResponse http response
     * @return error message from the http response
     */
    public static String extractErrorMessageFromHttpResponse(HttpResponse httpResponse)
    {
        final HttpEntity entity = httpResponse.getEntity();
        JsonReader reader = null;
        try
        {
            final InputStream responseStream = entity.getContent();
            reader = Json.createReader(responseStream);
            return reader.readObject().getString("message");
        }
        catch (JSONException error)
        {

            LOGGER.error("Converting message body to JSON failed. Body: {}", httpResponse, error);
        }
        catch (ParseException | IOException error)
        {

            LOGGER.error("Parsing message body failed.", error);
        }
        finally
        {
            if (reader != null)
            {
                reader.close();
            }
        }
        return null;
    }
}

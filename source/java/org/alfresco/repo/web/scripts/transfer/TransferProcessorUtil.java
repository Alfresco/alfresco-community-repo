/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.web.scripts.transfer;

import java.io.IOException;
import java.io.StringWriter;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.service.cmr.transfer.TransferException;
import org.springframework.extensions.webscripts.json.JSONWriter;

public class TransferProcessorUtil
{
    public static String writeError(TransferException ex) throws IOException
    {
        StringWriter stringWriter = new StringWriter(300);
        JSONWriter jsonWriter = new JSONWriter(stringWriter);
        writeError(ex, jsonWriter);
        return stringWriter.toString();
    }

    public static void writeError(Throwable ex, JSONWriter jsonWriter) throws IOException
    {
        jsonWriter.startObject();
        jsonWriter.writeValue("errorType", JSONWriter.encodeJSONString(ex.getClass().getName()));
        if (AlfrescoRuntimeException.class.isAssignableFrom(ex.getClass()))
        {
            AlfrescoRuntimeException alfEx = (AlfrescoRuntimeException)ex;
            jsonWriter.writeValue("errorId", JSONWriter.encodeJSONString(alfEx.getMsgId()));
            jsonWriter.startValue("errorParams");
            jsonWriter.startArray();
            Object[] msgParams = alfEx.getMsgParams();
            if (msgParams != null)
            {
                for (Object param : msgParams) 
                {
                    if (param != null)
                    {
                        jsonWriter.writeValue(JSONWriter.encodeJSONString(param.toString()));
                    }
                    else
                    {
                        jsonWriter.writeNullValue();
                    }
                }
            }
            jsonWriter.endArray();
        }
        jsonWriter.endObject();
    }

    /**
     * @param stringWriter
     * @param msgParams
     */
    public static String writeErrorParams(Object[] msgParams)
    {
        if (msgParams == null) return "";
        StringWriter writer = new StringWriter(300);
        boolean first = true;
        for (Object param : msgParams) {
            if (!first) {
                writer.write(",");
            }
            if (param != null) {
                writer.write("\"");
                writer.write(JSONWriter.encodeJSONString(param.toString()));
                writer.write("\"");
            } else {
                writer.write("null");
            }
            first = false;
        }
        return writer.toString();
    }


}

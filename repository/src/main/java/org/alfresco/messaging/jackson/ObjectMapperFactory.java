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
package org.alfresco.messaging.jackson;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.io.StringWriter;

import org.apache.commons.io.IOUtils;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class ObjectMapperFactory
{
    private ObjectMapperFactory()
    {
        //no instantiation
    }

    public static ObjectMapper createInstance()
    {
        QpidJsonBodyCleanerObjectMapper mapper = new QpidJsonBodyCleanerObjectMapper();
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
        return mapper;
    }

    private static class QpidJsonBodyCleanerObjectMapper extends ObjectMapper
    {
        @Serial
        private static final long serialVersionUID = 2568701685293341501L;

        private static final String DEFAULT_ENCODING = "utf8";

        public <T> T readValue(InputStream inputStream, Class<T> valueType) throws IOException
        {
            try
            {
                // Try to unmarshal normally
                if (inputStream.markSupported())
                {
                    inputStream.mark(1024 * 512);
                }
                return super.readValue(inputStream, valueType);
            }
            catch (JsonParseException e)
            {
                if (!inputStream.markSupported())
                {
                    // We can't reset this stream, bail out
                    throw e;
                }
                // Reset the stream
                inputStream.reset();
            }
            // Clean the message body and try again
            StringWriter writer = new StringWriter();
            IOUtils.copy(inputStream, writer, DEFAULT_ENCODING);
            String content = writer.toString();
            content = content.substring(content.indexOf('{'));
            return readValue(content, valueType);
        }
    }
}

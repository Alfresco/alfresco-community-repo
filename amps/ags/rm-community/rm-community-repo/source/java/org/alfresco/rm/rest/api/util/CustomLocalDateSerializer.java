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
package org.alfresco.rm.rest.api.util;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Custom Local Date serializer for formatting org.joda.time.LocalDate
 *
 * @author Rodica Sutu
 * @since 3.0
 */
public class CustomLocalDateSerializer extends StdSerializer<LocalDate>
{
    /** Local date format yyyy-MM-dd*/
    private final static DateTimeFormatter DATE_FORMAT = ISODateTimeFormat.date();

    public CustomLocalDateSerializer()
    {
        super(LocalDate.class);
    }

    protected CustomLocalDateSerializer(Class<LocalDate> t)
    {
        super(t);
    }

    /**
     * Custom serialize method to convert the org.joda.time.LocalDate into string value using the DATE_FORMAT
     *
     * @param value local date value
     * @param jgen
     * @param provider
     * @throws IOException
     */
    @Override
    public void serialize(LocalDate value, JsonGenerator jgen, SerializerProvider provider) throws IOException
    {
        jgen.writeString(DATE_FORMAT.print(value));
    }
}

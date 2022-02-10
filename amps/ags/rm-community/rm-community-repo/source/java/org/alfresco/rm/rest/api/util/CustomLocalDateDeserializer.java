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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;


/**
 * Custom Local Date deserializer converting a string to org.joda.time.LocalDate when the time is optional;
 *
 * @author Rodica Sutu
 * @since 3.0
 */
public class CustomLocalDateDeserializer extends StdDeserializer<LocalDate>
{
    /** Date time format with time optional  */
    private final static DateTimeFormatter LOCAL_DATE_OPTIONAL_TIME_PARSER = ISODateTimeFormat.localDateOptionalTimeParser();

    public CustomLocalDateDeserializer()
    {
        super(LocalDate.class);
    }

    /**
     * Custom deserialize method to convert string to the org.joda.time.LocalDate type with LOCAL_DATE_OPTIONAL_TIME_PARSER
     *
     * @param jp    local date value
     * @param ctxt
     * @throws IOException
     */
    @Override
    public LocalDate deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException
    {
        String str = jp.getText().trim();
        if (str.length() == 0)
        {
            return null;
        }
       return  LOCAL_DATE_OPTIONAL_TIME_PARSER.parseLocalDate(str);
    }
}


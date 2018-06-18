/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import org.joda.time.LocalDate;

/**
 * Custom Local Date deserializer for converting a string value with time into a org.joda.time.LocalDate value
 *
 * @author Rodica Sutu
 * @since 3.0
 */
public class CustomLocalDateDeserializer extends StdDeserializer<LocalDate>
{
    /** Local date format */
    private final static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");


    public CustomLocalDateDeserializer()
    {
        super(LocalDate.class);
    }
    protected CustomLocalDateDeserializer(Class<LocalDate> vc)
    {
        super(vc);
    }

    /**
     * Custom deserialize method to convert a string value into a org.joda.time.LocalDate value using the DATE_FORMAT
     *
     * @param p   local date value
     * @param ctx
     * @throws IOException
     */
    @Override
    public LocalDate deserialize(JsonParser p, DeserializationContext ctx) throws IOException
    {
        Date date = null;
        try
        {
            // convert the string with time into a date value using the format DATE_FORMAT
            date = DATE_FORMAT.parse(p.getText());
        }
        catch (ParseException e)
        {
            e.printStackTrace();
        }
        // convert the date into a LocalDate
        return LocalDate.fromDateFields(date);
    }
}

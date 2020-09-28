/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.rest.framework.jacksonextensions;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
import java.io.IOException;

/**
 * Rest Api string deserializer that ensures that empty strings are treated as null strings.
 *
 * @author steveglover
 * @author amukha
 */
public class RestApiStringDeserializer extends StringDeserializer
{
    private static final long serialVersionUID = 1L;

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
    {
        String ret = super.deserialize(p, ctxt);
        // Return null for empty string
        if(ret != null && ret.length() == 0)
        {
            ret = null;
        }
        return ret;
    }
}
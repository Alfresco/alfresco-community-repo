/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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

import java.io.IOException;

import org.alfresco.rest.framework.resource.SerializablePagedCollection;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.webscripts.ResourceWebScriptHelper;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.std.SerializerBase;

/**
 * Serializes SerializablePagedCollection into the correct response format, with Paging information and entries
 * 
 * @author Gethin James
 */
@SuppressWarnings("rawtypes")
public class SerializerOfCollectionWithPaging extends SerializerBase<SerializablePagedCollection>
{

    protected SerializerOfCollectionWithPaging()
    {
        super(SerializablePagedCollection.class);
    }
    
    @Override
    public void serialize(SerializablePagedCollection pagedCol, JsonGenerator jgen, SerializerProvider provider)
                throws IOException, JsonGenerationException
    {
        if (pagedCol != null)
        {
            jgen.writeStartObject();
            jgen.writeFieldName("list");
                jgen.writeStartObject();
                serializePagination(pagedCol, jgen);
    			jgen.writeObjectField("entries", pagedCol.getCollection());
                jgen.writeEndObject(); 
            jgen.writeEndObject();  
        }
    }

    private void serializePagination(SerializablePagedCollection pagedCol, JsonGenerator jgen) throws IOException,
    JsonProcessingException
    {
        jgen.writeFieldName("pagination");
        jgen.writeStartObject();
        jgen.writeNumberField("count", pagedCol.getCollection().size());
        jgen.writeBooleanField("hasMoreItems", pagedCol.hasMoreItems());
        Integer totalItems = pagedCol.getTotalItems();
        if(totalItems != null)
        {
        	jgen.writeNumberField("totalItems", totalItems);
        }
        if (pagedCol.getPaging() != null)
        {
            jgen.writeNumberField(ResourceWebScriptHelper.PARAM_PAGING_SKIP, pagedCol.getPaging().getSkipCount());
            jgen.writeNumberField(ResourceWebScriptHelper.PARAM_PAGING_MAX, pagedCol.getPaging().getMaxItems());            
        }
        jgen.writeEndObject();
    }
}

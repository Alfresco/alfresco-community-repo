package org.alfresco.rest.framework.jacksonextensions;

import java.io.IOException;

import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.webscripts.ResourceWebScriptHelper;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.std.SerializerBase;

/**
 * Serializes CollectionWithPagingInfo into the correct response format, with Paging information and entries//
 * 
 * @author Gethin James
 */
@SuppressWarnings("rawtypes")
public class SerializerOfCollectionWithPaging extends SerializerBase<CollectionWithPagingInfo>
{

    protected SerializerOfCollectionWithPaging()
    {
        super(CollectionWithPagingInfo.class);
    }
    
    @Override
    public void serialize(CollectionWithPagingInfo pagedCol, JsonGenerator jgen, SerializerProvider provider)
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

    private void serializePagination(CollectionWithPagingInfo pagedCol, JsonGenerator jgen) throws IOException,
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

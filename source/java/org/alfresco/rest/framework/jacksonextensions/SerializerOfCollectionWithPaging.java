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
                serializeIncludedSource(pagedCol, jgen);
                jgen.writeEndObject(); 
            jgen.writeEndObject();  
        }
    }

    private void serializeIncludedSource(SerializablePagedCollection pagedCol, JsonGenerator jgen) throws IOException,
            JsonProcessingException
    {
        if (pagedCol.getSourceEntity() != null)
        {
            jgen.writeObjectField("source",pagedCol.getSourceEntity());
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

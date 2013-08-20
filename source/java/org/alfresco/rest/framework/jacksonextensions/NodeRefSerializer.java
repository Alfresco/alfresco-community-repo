package org.alfresco.rest.framework.jacksonextensions;

import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.ser.std.SerializerBase;

public class NodeRefSerializer extends SerializerBase<NodeRef>
{
    protected NodeRefSerializer()
    {
        super(NodeRef.class);
    }

    @Override
    public void serialize(NodeRef nodeRef, JsonGenerator jgen, SerializerProvider provider)
                throws IOException, JsonGenerationException
    {
    	jgen.writeString(nodeRef.getId());
    }

}

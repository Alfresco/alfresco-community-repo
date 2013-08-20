package org.alfresco.rest.framework.jacksonextensions;

import java.io.IOException;

import org.alfresco.rest.api.model.Document;
import org.alfresco.rest.api.model.DocumentTarget;
import org.alfresco.rest.api.model.Folder;
import org.alfresco.rest.api.model.FolderTarget;
import org.alfresco.rest.api.model.Site;
import org.alfresco.rest.api.model.SiteImpl;
import org.alfresco.rest.api.model.SiteTarget;
import org.alfresco.rest.api.model.Target;
import org.alfresco.service.cmr.favourites.FavouritesService.Type;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.BeanProperty;
import org.codehaus.jackson.map.BeanProperty.Std;
import org.codehaus.jackson.map.DeserializationContext;
import org.codehaus.jackson.map.JsonDeserializer;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.type.SimpleType;
import org.codehaus.jackson.type.JavaType;

public class TargetDeserializer extends JsonDeserializer<Target>
{
	@Override
	public Target deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException
	{
		Target target = null;
        JsonToken curr = jp.getCurrentToken();
        
        if (curr == JsonToken.START_OBJECT)
        {
            while(jp.nextToken() != JsonToken.END_OBJECT)
            {
        		String fieldname = jp.getCurrentName();
        		if(Type.SITE.toString().equals(fieldname.toUpperCase()))
        		{
        			jp.nextToken();
        			try
        			{
        		        JavaType t = SimpleType.construct(SiteImpl.class);
        		        BeanProperty p = new Std("", t, null, null);
        		        JsonDeserializer<?> siteDeserializer = ctxt.getDeserializerProvider().findValueDeserializer(ctxt.getConfig(), t, p);

        				Site site = (Site)siteDeserializer.deserialize(jp, ctxt);
                    	target = new SiteTarget(site);
        			}
        			catch(JsonMappingException e)
        			{
        				throw new IllegalArgumentException("Target body is invalid for target type");
        			}
        		}
        		else if(Type.FILE.toString().equals(fieldname.toUpperCase()))
        		{
        			jp.nextToken();
        			try
        			{
        				JavaType t = SimpleType.construct(Document.class);
        				BeanProperty p = new Std("", t, null, null);
        		        JsonDeserializer<?> documentDeserializer = ctxt.getDeserializerProvider().findValueDeserializer(ctxt.getConfig(), t, p);

	        			Document document = (Document)documentDeserializer.deserialize(jp, ctxt);
	                	target = new DocumentTarget(document);
        			}
        			catch(JsonMappingException e)
        			{
        				throw new IllegalArgumentException("Target body is invalid for target type");
        			}
        		}
        		else if(Type.FOLDER.toString().equals(fieldname.toUpperCase()))
        		{
        			jp.nextToken();
        			try
        			{
        				JavaType t = SimpleType.construct(Folder.class);
        				BeanProperty p = new Std("", t, null, null);
        		        JsonDeserializer<?> folderDeserializer = ctxt.getDeserializerProvider().findValueDeserializer(ctxt.getConfig(), t, p);
        		        
	        			Folder folder = (Folder)folderDeserializer.deserialize(jp, ctxt);
	        			target = new FolderTarget(folder);
        			}
        			catch(JsonMappingException e)
        			{
        				throw new IllegalArgumentException("Target body is invalid for target type");
        			}
        		}
            }

        	return target;
        }
        else
        {
        	throw new IOException("Unable to deserialize favourite: " + curr.asString());
        }
	}
}

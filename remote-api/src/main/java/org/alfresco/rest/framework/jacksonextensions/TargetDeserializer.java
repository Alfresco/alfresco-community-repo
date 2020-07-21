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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.PropertyName;
import com.fasterxml.jackson.databind.type.SimpleType;
import java.io.IOException;

import org.alfresco.rest.api.model.Document;
import org.alfresco.rest.api.model.DocumentTarget;
import org.alfresco.rest.api.model.Folder;
import org.alfresco.rest.api.model.FolderTarget;
import org.alfresco.rest.api.model.Site;
import org.alfresco.rest.api.model.SiteTarget;
import org.alfresco.rest.api.model.Target;
import org.alfresco.service.cmr.favourites.FavouritesService.Type;

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
        		        JavaType t = SimpleType.construct(Site.class);
        		        BeanProperty p = new BeanProperty.Std(new PropertyName(""), t, null, null, null, null);
        		        JsonDeserializer<?> siteDeserializer = ctxt.findContextualValueDeserializer(t, p);

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
                        BeanProperty p = new BeanProperty.Std(new PropertyName(""), t, null, null, null, null);
                        JsonDeserializer<?> documentDeserializer = ctxt.findContextualValueDeserializer(t, p);

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
                        BeanProperty p = new BeanProperty.Std(new PropertyName(""), t, null, null, null, null);
                        JsonDeserializer<?> folderDeserializer = ctxt.findContextualValueDeserializer(t, p);

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

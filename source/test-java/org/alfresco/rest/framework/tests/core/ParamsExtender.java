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
package org.alfresco.rest.framework.tests.core;

import static org.mockito.Mockito.mock;

import java.io.InputStream;
import java.util.Map;

import org.alfresco.rest.framework.jacksonextensions.BeanPropertiesFilter;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Just extends the Params class for testing purposes
 *
 * @author Gethin James
 */
public class ParamsExtender extends Params
{

    private ParamsExtender(String entityId, String relationshipId, Object passedIn, InputStream stream, String addressedProperty, RecognizedParams recognizedParams)
    {
        super(entityId, relationshipId, passedIn, stream, addressedProperty, recognizedParams, null, mock(WebScriptRequest.class));
    }
    
    public static Params valueOf(Map<String, BeanPropertiesFilter> rFilter, String entityId)
    {
        return new ParamsExtender(entityId, null, null, null, null, new Params.RecognizedParams(null, null, null, rFilter, null, null, null, null, false));
    }

    public static Params valueOf(boolean includeSource, String entityId)
    {
        return new ParamsExtender(entityId, null, null, null, null, new Params.RecognizedParams(null, null, null, null, null, null, null, null, includeSource));
    }

    public static Params valueOf(Paging paging, String entityId)
    {
        return new ParamsExtender(entityId, null, null, null, null, new Params.RecognizedParams(null, paging, null, null, null, null, null, null, false));
    }

    public static Params valueOf(Map<String, String[]> params)
    {
        return new ParamsExtender(null, null, null, null, null, new Params.RecognizedParams(params, null, null, null, null, null, null, null, false));
    }

}

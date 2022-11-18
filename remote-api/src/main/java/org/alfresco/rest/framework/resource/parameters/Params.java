/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.rest.framework.resource.parameters;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.jacksonextensions.BeanPropertiesFilter;
import org.alfresco.rest.framework.resource.content.BasicContentInfo;
import org.alfresco.rest.framework.resource.content.ContentInfoImpl;
import org.alfresco.rest.framework.resource.parameters.where.Query;
import org.alfresco.rest.framework.resource.parameters.where.QueryImpl;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Parameters passed in from a Rest client for use in calls to the rest api.
 *
 * @author Gethin James
 */
public class Params implements Parameters
{
    // Note: isCollectionResource maps to "readAll". It can apply to these levels, eg ...
    //            /entities,
    //            /entities/{entityAId}/relationship1,
    //            /entities/{entityAId}/relationship1/{entityBId}/relationship2
    private final boolean isCollectionResource;

    private final String entityId;
    private final String relationshipId;
    private final String relationship2Id;

    private final Object passedIn;
    private final InputStream stream;
    private final RecognizedParams recognizedParams;
    private final String addressedProperty;
    private final BasicContentInfo contentInfo;
    private final WebScriptRequest request;

    //Constants
    private static final RecognizedParams NULL_PARAMS = new RecognizedParams(null, null, null, null, null, null, null, null, false);
    private static final BasicContentInfo DEFAULT_CONTENT_INFO = new ContentInfoImpl(MimetypeMap.MIMETYPE_BINARY, "UTF-8", -1, null);
    
    protected Params(Boolean isCollectionResource, String entityId, String relationshipId, String relationship2Id, Object passedIn, InputStream stream, String addressedProperty, RecognizedParams recognizedParams, BasicContentInfo contentInfo, WebScriptRequest request)
    {
        super();
        this.isCollectionResource = (isCollectionResource != null ? isCollectionResource : (entityId == null));
        this.entityId = entityId;
        this.relationshipId = relationshipId;
        this.relationship2Id = relationship2Id;
        this.passedIn = passedIn;
        this.stream = stream;
        this.recognizedParams = recognizedParams;
        this.addressedProperty = addressedProperty;
        this.request = request;
        this.contentInfo = contentInfo==null?DEFAULT_CONTENT_INFO:contentInfo;
    }

    public static Params valueOf(BeanPropertiesFilter paramFilter, String entityId, WebScriptRequest request)
    {
        return new Params(null, entityId, null, null, null, null, null, new RecognizedParams(null, null, paramFilter, null, null, null, null, null, false), null, request);
    }

    public static Params valueOf(String entityId, String relationshipId, WebScriptRequest request)
    {
        return new Params(null, entityId, relationshipId, null, null,null, null, NULL_PARAMS, null, request);
    }
    
    public static Params valueOf(RecognizedParams recognizedParams, String entityId, String relationshipId, WebScriptRequest request)
    {
        return new Params(null, entityId, relationshipId, null, null, null, null, recognizedParams, null, request);
    }
    
    public static Params valueOf(String entityId, RecognizedParams recognizedParams, Object passedIn, WebScriptRequest request)
    {
        return new Params(null, entityId, null, null, passedIn, null, null, recognizedParams, null, request);
    }

    public static Params valueOf(String entityId, String relationshipId, RecognizedParams recognizedParams, Object passedIn, WebScriptRequest request)
    {
        return new Params(null, entityId, relationshipId, null, passedIn, null, null, recognizedParams, null, request);
    }

    public static Params valueOf(String entityId, String relationshipId, Object passedIn, InputStream stream,
                                 String addressedProperty, RecognizedParams recognizedParams, BasicContentInfo contentInfo, WebScriptRequest request)
    {
        return new Params(null, entityId, relationshipId, null, passedIn, stream, addressedProperty, recognizedParams, contentInfo, request);
    }

    public static Params valueOf(boolean isCollectionResource, String entityId, String relationshipId, String relationship2Id, Object passedIn, InputStream stream,
                                 String addressedProperty, RecognizedParams recognizedParams, BasicContentInfo contentInfo, WebScriptRequest request)
    {
        return new Params(isCollectionResource, entityId, relationshipId, relationship2Id, passedIn, stream, addressedProperty, recognizedParams, contentInfo, request);
    }
    
    public String getEntityId()
    {
        return this.entityId;
    }

    public Object getPassedIn()
    {
        return this.passedIn;
    }

    public String getRelationshipId()
    {
        return this.relationshipId;
    }

    public String getRelationship2Id()
    {
        return this.relationship2Id;
    }

    public boolean isCollectionResource()
    {
        return this.isCollectionResource;
    }

    public Query getQuery()
    {
        return this.recognizedParams.query;
    }

    public Paging getPaging()
    {
        return this.recognizedParams.paging;
    }

    public BeanPropertiesFilter getFilter()
    {
        return this.recognizedParams.filter;
    }

    @Override
    public boolean includeSource()
    {
        return this.recognizedParams.includeSource;
    }

    public Map<String, BeanPropertiesFilter> getRelationsFilter()
    {
        return this.recognizedParams.relationshipFilter;
    }

    public InputStream getStream()
    {
        return this.stream;
    }

    /*
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Params [entityId=");
        builder.append(this.entityId);
        builder.append(", relationshipId=");
        builder.append(this.relationshipId);
        builder.append(", relationship2Id=");
        builder.append(this.relationship2Id);
        builder.append(", passedIn=");
        builder.append(this.passedIn);
        builder.append(", paging=");
        builder.append(this.recognizedParams.paging);
        builder.append(", query=");
        builder.append(this.recognizedParams.query);
        builder.append(", sorting=");
        builder.append(this.recognizedParams.sorting);
        builder.append(", include=");
        builder.append(this.recognizedParams.include);
        builder.append(", select=");
        builder.append(this.recognizedParams.select);
        builder.append(", filter=");
        builder.append(this.recognizedParams.filter);
        builder.append(", relationshipFilter=");
        builder.append(this.recognizedParams.relationshipFilter);
        builder.append(", includeSource=");
        builder.append(this.recognizedParams.includeSource);
        builder.append(", addressedProperty=");
        builder.append(this.addressedProperty);
        builder.append("]");
        return builder.toString();
    }
    
    @Override
    /**
     * Similar to the standard HTTPRequest method.  Just returns the first parameter value or NULL.
     */
    public String getParameter(String parameterName)
    {
        if (recognizedParams.requestParameters!= null && !recognizedParams.requestParameters.isEmpty())
        {
            String[] vals = recognizedParams.requestParameters.get(parameterName);
            if (vals!= null && vals.length>0)
            {
                return vals[0]; //Just return the first element.
            }
        }
        return null;
    }

	@Override
	public T getParameter(String parameterName, Class<T> clazz) throws InvalidArgumentException {
		String param = getParameter(parameterName);
		if (param == null) return null;
		Object obj = ConvertUtils.convert(param, clazz);
		if (obj != null && obj.getClass().equals(clazz))
		{
			return (T) obj;
		}
		throw new InvalidArgumentException(InvalidArgumentException.DEFAULT_MESSAGE_ID, new Object[] {parameterName});
	}
	
    @Override
    public boolean hasBinaryProperty(String propertyName)
    {
        return (addressedProperty != null && addressedProperty.equals(propertyName));
    }

    @Override
    public List<SortColumn> getSorting()
    {
        return recognizedParams.sorting;
    }

    @Override
    public String getBinaryProperty()
    {
        return addressedProperty;
    }
    
    @Override
    public List<String> getSelectedProperties()
    {
        return recognizedParams.select;
    }

    @Override
    public List<String> getInclude()
    {
        return recognizedParams.include;
    }
    
    @Override
    public BasicContentInfo getContentInfo()
    {
        return contentInfo;
    }

    @Override
    public WebScriptRequest getRequest()
    {
        return request;
    }

    /**
     * A formal set of params that any rest service could potentially have passed in as request params
     */
    public static class RecognizedParams 
    {
        final Paging paging;
        private final BeanPropertiesFilter filter;
        private final Map<String, BeanPropertiesFilter> relationshipFilter;
        private final Map<String, String[]> requestParameters;
        private final Query query;

        private final List<String> include;
        @Deprecated
        private final List<String> select; // see include

        private final List<SortColumn> sorting;
        private final boolean includeSource;
        
        @SuppressWarnings("unchecked")
        public RecognizedParams(Map<String, String[]> requestParameters, Paging paging, BeanPropertiesFilter filter,
                                Map<String, BeanPropertiesFilter> relationshipFilter, List<String> include, List<String> select,
                                Query query, List<SortColumn> sorting, boolean includeSource)
        {
            super();

            this.requestParameters = requestParameters;
            this.paging = paging==null?Paging.DEFAULT:paging;
            this.filter = filter==null?BeanPropertiesFilter.ALLOW_ALL:filter;
            this.query = query==null?QueryImpl.EMPTY:query;
            this.relationshipFilter = (Map<String, BeanPropertiesFilter>) (relationshipFilter==null?Collections.emptyMap():relationshipFilter);

            this.include = (List<String>) (include==null?Collections.emptyList():include);
            this.select = (List<String>) (select==null?Collections.emptyList():select);

            this.sorting = (List<SortColumn>) (sorting==null?Collections.emptyList():sorting);
            this.includeSource = includeSource;
        }
        
    }
}

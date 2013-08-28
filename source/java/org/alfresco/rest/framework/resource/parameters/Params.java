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

/**
 * Parameters passed in from a Rest client for use in calls to the rest api.
 *
 * @author Gethin James
 */
public class Params implements Parameters
{
    private final String entityId;
    private final String relationshipId;
    private final Object passedIn;
    private final InputStream stream;
    private final RecognizedParams recognizedParams;
    private final String addressedProperty;
    private final BasicContentInfo contentInfo;
    
    //Constants
    private static final RecognizedParams NULL_PARAMS = new RecognizedParams(null, null, null, null, null, null, null);
    private static final BasicContentInfo DEFAULT_CONTENT_INFO = new ContentInfoImpl(MimetypeMap.MIMETYPE_BINARY, "UTF-8", -1, null);
    
    protected Params(String entityId, String relationshipId, Object passedIn, InputStream stream, String addressedProperty, RecognizedParams recognizedParams, BasicContentInfo contentInfo)
    {
        super();
        this.entityId = entityId;
        this.relationshipId = relationshipId;
        this.passedIn = passedIn;
        this.stream = stream;
        this.recognizedParams = recognizedParams;
        this.addressedProperty = addressedProperty;
        this.contentInfo = contentInfo==null?DEFAULT_CONTENT_INFO:contentInfo;
    }

    public static Params valueOf(BeanPropertiesFilter paramFilter, String entityId)
    {
        return new Params(entityId, null, null, null, null, new RecognizedParams(null, null, paramFilter, null, null, null, null), null);
    }

    public static Params valueOf(String entityId, String relationshipId)
    {
        return new Params(entityId, relationshipId, null, null, null, NULL_PARAMS, null);
    }
    
    public static Params valueOf(RecognizedParams recognizedParams, String entityId, String relationshipId)
    {
        return new Params(entityId, relationshipId, null, null, null, recognizedParams, null);
    }
    
    public static Params valueOf(String entityId, RecognizedParams recognizedParams, Object passedIn)
    {
        return new Params(entityId, null, passedIn, null, null, recognizedParams, null);
    }
    
    public static Params valueOf(String entityId, String relationshipId, Object passedIn, InputStream stream, String addressedProperty, RecognizedParams recognizedParams, BasicContentInfo contentInfo)
    {
        return new Params(entityId, relationshipId, passedIn, stream, addressedProperty, recognizedParams, contentInfo);
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
        builder.append(", passedIn=");
        builder.append(this.passedIn);
        builder.append(", paging=");
        builder.append(this.recognizedParams.paging);
        builder.append(", query=");
        builder.append(this.recognizedParams.query);
        builder.append(", sorting=");
        builder.append(this.recognizedParams.sorting);
        builder.append(", select=");
        builder.append(this.recognizedParams.select);
        builder.append(", filter=");
        builder.append(this.recognizedParams.filter);
        builder.append(", relationshipFilter=");
        builder.append(this.recognizedParams.relationshipFilter);
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
	public BasicContentInfo getContentInfo() {
		return contentInfo;
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
        private final List<String> select;
        private final List<SortColumn> sorting;
        
        @SuppressWarnings("unchecked")
        public RecognizedParams(Map<String, String[]> requestParameters, Paging paging, BeanPropertiesFilter filter, Map<String, BeanPropertiesFilter> relationshipFilter, List<String> select,
                    Query query, List<SortColumn> sorting)
        {
            super();
            this.requestParameters = requestParameters;
            this.paging = paging==null?Paging.DEFAULT:paging;
            this.filter = filter==null?BeanPropertiesFilter.ALLOW_ALL:filter;
            this.query = query==null?QueryImpl.EMPTY:query;
            this.relationshipFilter = (Map<String, BeanPropertiesFilter>) (relationshipFilter==null?Collections.emptyMap():relationshipFilter);
            this.select = (List<String>) (select==null?Collections.emptyList():select);
            this.sorting = (List<SortColumn>) (sorting==null?Collections.emptyList():sorting);
        }
        
    }
}

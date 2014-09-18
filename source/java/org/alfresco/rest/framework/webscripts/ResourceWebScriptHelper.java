
package org.alfresco.rest.framework.webscripts;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.alfresco.rest.framework.Api;
import org.alfresco.rest.framework.core.ResourceInspector;
import org.alfresco.rest.framework.core.ResourceInspectorUtil;
import org.alfresco.rest.framework.core.ResourceLocator;
import org.alfresco.rest.framework.core.ResourceWithMetadata;
import org.alfresco.rest.framework.core.exceptions.ApiException;
import org.alfresco.rest.framework.core.exceptions.InvalidArgumentException;
import org.alfresco.rest.framework.core.exceptions.NotFoundException;
import org.alfresco.rest.framework.core.exceptions.PermissionDeniedException;
import org.alfresco.rest.framework.jacksonextensions.BeanPropertiesFilter;
import org.alfresco.rest.framework.jacksonextensions.ExecutionResult;
import org.alfresco.rest.framework.jacksonextensions.JacksonHelper;
import org.alfresco.rest.framework.resource.actions.ActionExecutor;
import org.alfresco.rest.framework.resource.actions.ActionExecutor.ExecutionCallback;
import org.alfresco.rest.framework.resource.content.ContentInfo;
import org.alfresco.rest.framework.resource.parameters.CollectionWithPagingInfo;
import org.alfresco.rest.framework.resource.parameters.InvalidSelectException;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.rest.framework.resource.parameters.Params.RecognizedParams;
import org.alfresco.rest.framework.resource.parameters.SortColumn;
import org.alfresco.rest.framework.resource.parameters.where.InvalidQueryException;
import org.alfresco.rest.framework.resource.parameters.where.Query;
import org.alfresco.rest.framework.resource.parameters.where.QueryImpl;
import org.alfresco.rest.framework.resource.parameters.where.WhereCompiler;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonErrorNode;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.RewriteCardinalityException;
import org.antlr.runtime.tree.Tree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.JsonMappingException;
import org.springframework.beans.BeanUtils;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.http.HttpMethod;

/**
 * Helps a Webscript with various tasks
 * 
 * @author Gethin James
 */
public class ResourceWebScriptHelper
{

    private static Log logger = LogFactory.getLog(ResourceWebScriptHelper.class);
    public static final String PARAM_RELATIONS = "relations";
    public static final String PARAM_FILTER_PROPS = "properties";
    public static final String PARAM_PAGING_SKIP = "skipCount";
    public static final String PARAM_PAGING_MAX = "maxItems";
    public static final String PARAM_ORDERBY = "orderBy";
    public static final String PARAM_WHERE = "where";
    public static final String PARAM_SELECT = "select";
    public static final List<String> KNOWN_PARAMS = Arrays.asList(PARAM_RELATIONS,PARAM_FILTER_PROPS,PARAM_PAGING_SKIP,PARAM_PAGING_MAX, PARAM_ORDERBY, PARAM_WHERE, PARAM_SELECT);
    
    private ResourceLocator locator;

    private ActionExecutor executor;

    /**
     * Takes the web request and looks for a "filter" parameter Parses the
     * parameter and produces a list of bean properties to use as a filter A
     * SimpleBeanPropertyFilter it returned that uses the properties If no
     * filter param is set then a default BeanFilter is returned that will never
     * filter properties (ie. Returns all bean properties).
     * 
     * @param req
     * @return BeanPropertyFilter - if no parameter then returns a new
     *         ReturnAllBeanProperties class
     */
    public static BeanPropertiesFilter getFilter(String filterParams)
    {
        if (filterParams != null)
        {
            StringTokenizer st = new StringTokenizer(filterParams, ",");
            Set<String> filteredProperties = new HashSet<String>(st.countTokens());
            while (st.hasMoreTokens())
            {
                filteredProperties.add(st.nextToken());
            }
            logger.debug("Filtering using the following properties: " + filteredProperties);
            BeanPropertiesFilter filter = new BeanPropertiesFilter(filteredProperties);
            return filter;
        }
        return BeanPropertiesFilter.ALLOW_ALL;
    }

    /**
     * Takes the web request and looks for a "relations" parameter Parses the
     * parameter and produces a list of bean properties to use as a filter A
     * SimpleBeanPropertiesFilter it returned that uses the properties If no
     * filter param is set then a default BeanFilter is returned that will never
     * filter properties (ie. Returns all bean properties).
     * 
     * @param req
     * @return BeanPropertiesFilter - if no parameter then returns a new
     *         ReturnAllBeanProperties class
     */
    public static Map<String, BeanPropertiesFilter> getRelationFilter(String filterParams)
    {
        if (filterParams != null)
        {
            // Split by a comma when not in a bracket
            String[] relations = filterParams.split(",(?![^()]*+\\))");
            Map<String, BeanPropertiesFilter> filterMap = new HashMap<String, BeanPropertiesFilter>(relations.length);

            for (String relation : relations)
            {
                int bracketLocation = relation.indexOf("(");
                if (bracketLocation != -1)
                {
                    // We have properties
                    String relationKey = relation.substring(0, bracketLocation);
                    String props = relation.substring(bracketLocation + 1, relation.length() - 1);
                    filterMap.put(relationKey, getFilter(props));
                }
                else
                {
                    // no properties so just get the String
                    filterMap.put(relation, getFilter(null));
                }
            }
            return filterMap;
        }
        return Collections.emptyMap();
    }
    
    /**
     * Takes the "select" parameter and turns it into a List<String> property names
     * @param selectParam
     * @return List<String> bean property names potentially using JSON Pointer syntax
     */
    @SuppressWarnings("unchecked")
    public static List<String> getSelectClause(String selectParam) throws InvalidArgumentException
    {
        if (selectParam == null) return Collections.emptyList();
        
		try {
			CommonTree selectedPropsTree = WhereCompiler.compileSelectClause(selectParam);
			if (selectedPropsTree instanceof CommonErrorNode)
			{
				logger.debug("Error parsing the SELECT clause "+selectedPropsTree);
				throw new InvalidSelectException(selectedPropsTree);
			}
			if (selectedPropsTree.getChildCount() == 0 && !selectedPropsTree.getText().isEmpty())
			{
				return Arrays.asList(selectedPropsTree.getText());
			}
			List<Tree> children = (List<Tree>) selectedPropsTree.getChildren();
			if (children!= null && !children.isEmpty())
			{
				List<String> properties = new ArrayList<String>(children.size());
				for (Tree child : children) {
					properties.add(child.getText());
				}
				return properties;
			}
		} catch (RewriteCardinalityException re) {  //Catch any error so it doesn't get thrown up the stack
			logger.debug("Unhandled Error parsing the SELECT clause: "+re);
		} catch (RecognitionException e) {
			logger.debug("Error parsing the SELECT clause: "+selectParam);
		}
        //Default to throw out an invalid query
        throw new InvalidSelectException(selectParam); 
    }
    
    /**
     * Takes the "where" parameter and turns it into a Java Object that can be used for querying
     * @param whereParam
     * @return Query a parsed version of the where clause, represented in Java
     */
    public static Query getWhereClause(String whereParam) throws InvalidQueryException
    {
        if (whereParam == null) return QueryImpl.EMPTY;
        
		try {
			CommonTree whereTree = WhereCompiler.compileWhereClause(whereParam);
			if (whereTree instanceof CommonErrorNode)
			{
				logger.debug("Error parsing the WHERE clause "+whereTree);
				throw new InvalidQueryException(whereTree);
			}
	        return new QueryImpl(whereTree);
		} catch (RewriteCardinalityException re) {  //Catch any error so it doesn't get thrown up the stack
			logger.info("Unhandled Error parsing the WHERE clause: "+re);
		} catch (RecognitionException e) {
			whereParam += ", "+WhereCompiler.resolveMessage(e);
			logger.info("Error parsing the WHERE clause: "+whereParam);
		}
        //Default to throw out an invalid query
        throw new InvalidQueryException(whereParam);
    }

    /**
     * Takes the Sort parameter as a String and parses it into a List of SortColumn objects.
     * The format is a comma seperated list of "columnName sortDirection",
     * e.g. "name DESC, age ASC".  It is not case sensitive and the sort direction is optional
     * It default to sort ASCENDING.
     * @param sortParams - String passed in on the request
     * @return List<SortColumn> - the sort columns or an empty list if the params were invalid.
     */
    public static List<SortColumn> getSort(String sortParams)
    {
        if (sortParams != null)
        {
            StringTokenizer st = new StringTokenizer(sortParams, ",");
            List<SortColumn> sortedColumns = new ArrayList<SortColumn>(st.countTokens());
            while (st.hasMoreTokens())
            {
                String token = st.nextToken();
                StringTokenizer columnDesc = new StringTokenizer(token, " ");
                if (columnDesc.countTokens() <= 2)
                {
                String columnName = columnDesc.nextToken();
                String sortOrder = SortColumn.ASCENDING;
                if (columnDesc.hasMoreTokens())
                {
                  String sortDef = columnDesc.nextToken().toUpperCase();  
                  if (SortColumn.ASCENDING.equals(sortDef) || SortColumn.DESCENDING.equals(sortDef))
                  {
                      sortOrder = sortDef;
                  }
                  else
                  {
                      logger.debug("Invalid sort order definition ("+sortDef+").  Valid values are "+SortColumn.ASCENDING+" or "+SortColumn.DESCENDING+".");
                  }
                }
                sortedColumns.add(new SortColumn(columnName, SortColumn.ASCENDING.equals(sortOrder)));
                }
               // filteredProperties.add();
            }
//            logger.debug("Filtering using the following properties: " + filteredProperties);
//            BeanPropertiesFilter filter = new BeanPropertiesFilter(filteredProperties);
            return sortedColumns;
        }
        return Collections.emptyList();
    }

    /**
     * Extracts the body contents from the request
     * 
     * @param req the request
     * @param jsonHelper Jackson Helper
     * @param requiredType the type to return
     * @return the Object in the required type
     */
    public static <T> T extractJsonContent(WebScriptRequest req, JacksonHelper jsonHelper, Class<T> requiredType)
    {
        Reader reader;
        try
        {
            reader = req.getContent().getReader();
            return jsonHelper.construct(reader, requiredType);
        }
        catch (JsonMappingException e)
        {
        	logger.warn("Could not read content from HTTP request body.", e);
            throw new InvalidArgumentException("Could not read content from HTTP request body.");
        }
        catch (IOException e)
        {
            throw new ApiException("Could not read content from HTTP request body.", e.getCause());
        }
    }

    /**
     * Extracts the body contents from the request as a List, the JSON can be an array or just a single value without the [] symbols
     * 
     * @param req the request
     * @param jsonHelper Jackson Helper
     * @param requiredType the type to return (without the List param)
     * @return A List of "Object" as the required type
     */
    public static <T> List<T> extractJsonContentAsList(WebScriptRequest req, JacksonHelper jsonHelper, Class<T> requiredType)
    {
        Reader reader;
        try
        {
            reader = req.getContent().getReader();
            return jsonHelper.constructList(reader, requiredType);
        }
        catch (IOException e)
        {
            throw new ApiException("Could not read content from HTTP request body.", e.getCause());
        }
    }

    /**
     * Set the id of theObj to the uniqueId. Attempts to find a set method and
     * invoke it. If it fails it just swallows the exceptions and doesn't throw
     * them further.
     * 
     * @param theObj
     * @param uniqueId
     */
    public static void setUniqueId(Object theObj, String uniqueId)
    {
        Method annotatedMethod = ResourceInspector.findUniqueIdMethod(theObj.getClass());
        if (annotatedMethod != null)
        {
            PropertyDescriptor pDesc = BeanUtils.findPropertyForMethod(annotatedMethod);
            if (pDesc != null)
            {
                Method writeMethod = pDesc.getWriteMethod();
                if (writeMethod != null)
                {
                    try
                    {
                        writeMethod.invoke(theObj, uniqueId);
                        if (logger.isDebugEnabled())
                        {
                            logger.debug("Unique id set for property: " + pDesc.getName());
                        }
                    }
                    catch (IllegalArgumentException error)
                    {
                        logger.warn("Invocation error", error);
                    }
                    catch (IllegalAccessException error)
                    {
                        logger.warn("IllegalAccessException", error);
                    }
                    catch (InvocationTargetException error)
                    {
                        logger.warn("InvocationTargetException", error);
                    }
                }
                else
                {
                    logger.warn("No setter method for property: " + pDesc.getName());
                }
            }

        }
    }

//    /**
//     * Renders the response result
//     * 
//     * @param response
//     * @param result
//     */
//    public static void renderResponseDep(Map<String, Object> response, Object result)
//    {
//
//        if (result == null) { return; }
//
//        if (result instanceof Collection)
//        {
//            response.put("list", result);
//        }
//        else if (result instanceof CollectionWithPagingInfo)
//        {
//            CollectionWithPagingInfo<?> col = (CollectionWithPagingInfo<?>) result;
//            if (col.getCollection() !=null && !col.getCollection().isEmpty())
//            {
//                response.put("list", col);
//            }
//        }
//        else if (result instanceof Pair<?,?>)
//        {
//            Pair<?,?> aPair = (Pair<?, ?>) result;
//            response.put("entry", aPair.getFirst());
//            response.put("relations", aPair.getSecond());
//        }
//        else
//        {
//            response.put("entry", result);
//        }
//    }

    /**
     * Looks at the object passed in and recursively expands any @EmbeddedEntityResource annotations or related relationship.
     * @EmbeddedEntityResource is expanded by calling the ReadById method for this entity.
     * 
     * Either returns a ExecutionResult object or a CollectionWithPagingInfo containing a collection of ExecutionResult objects.
     * 
     * @param objectToWrap
     * @param result 
     * @return Object - Either ExecutionResult or CollectionWithPagingInfo<ExecutionResult>
     */
    public Object postProcessResponse(Api api, String entityCollectionName, Params params, Object objectToWrap)
    {
        PropertyCheck.mandatory(this, null, params);
        if (objectToWrap == null ) return null;
        if (objectToWrap instanceof CollectionWithPagingInfo<?>)
        {
            CollectionWithPagingInfo<?> collectionToWrap = (CollectionWithPagingInfo<?>) objectToWrap;
            if (!collectionToWrap.getCollection().isEmpty())
            {
                Collection<Object> resultCollection = new ArrayList(collectionToWrap.getCollection().size());
                for (Object obj : collectionToWrap.getCollection())
                {
                    resultCollection.add(postProcessResponse(api,entityCollectionName,params,obj));
                }
                return CollectionWithPagingInfo.asPaged(collectionToWrap.getPaging(), resultCollection, collectionToWrap.hasMoreItems(), collectionToWrap.getTotalItems());
            }
            else
            {   //It is empty so just return it for rendering.
                return objectToWrap;
            }
        }
        else
        {           
            if (BeanUtils.isSimpleProperty(objectToWrap.getClass())  || objectToWrap instanceof Collection)
            {
                //Simple property or Collection that can't be embedded so just return it.
                return objectToWrap;
            }
            final ExecutionResult execRes = new ExecutionResult(objectToWrap, params.getFilter());
            
            Map<String,Pair<String,Method>> embeddded = ResourceInspector.findEmbeddedResources(objectToWrap.getClass());
            if (embeddded != null && !embeddded.isEmpty())
            {
                Map<String, Object> results = executeEmbeddedResources(api, params,objectToWrap, embeddded);
                execRes.addEmbedded(results);
            }
            
            if (params.getRelationsFilter() != null && !params.getRelationsFilter().isEmpty())
            {
                Map<String, ResourceWithMetadata> relationshipResources = locator.locateRelationResource(api,entityCollectionName, params.getRelationsFilter().keySet(), HttpMethod.GET);
                String uniqueEntityId = ResourceInspector.findUniqueId(objectToWrap);
                Map<String,Object> relatedResources = executeRelatedResources(api,params.getRelationsFilter(), relationshipResources, uniqueEntityId);
                execRes.addRelated(relatedResources);
            }

            return execRes; 

        }
    }

    /**
     * Loops through the embedded Resources and executes them.  The results are added to list of embedded results used by
     * the ExecutionResult object.
     * 
     * @param relatedResources
     * @param execRes
     * @param uniqueEntityId
     */
    private Map<String, Object> executeEmbeddedResources(Api api, Params params, Object objectToWrap, Map<String, Pair<String, Method>> embeddded)
    {
        final Map<String,Object> results = new HashMap<String,Object>(embeddded.size());
        for (Entry<String, Pair<String,Method>> embeddedEntry : embeddded.entrySet())
        {
            ResourceWithMetadata res = locator.locateEntityResource(api,embeddedEntry.getValue().getFirst(), HttpMethod.GET);
            if (res != null)
            {
                Object id = ResourceInspectorUtil.invokeMethod(embeddedEntry.getValue().getSecond(), objectToWrap);
                if (id != null)
                {
                    Object execEmbeddedResult = executeRelatedResource(api, params.getRelationsFilter(), String.valueOf(id), embeddedEntry.getKey(), res);
                    if (execEmbeddedResult != null)
                    {
                        if (execEmbeddedResult instanceof ExecutionResult)
                        {
                           ((ExecutionResult) execEmbeddedResult).setAnEmbeddedEntity(true);
                        }
                        results.put(embeddedEntry.getKey(), execEmbeddedResult);
                    }
                }
                else
                {
                    //Call to embedded id for null value, 
                    logger.warn("Cannot embed resource with path "+embeddedEntry.getKey()+". No unique id because the method annotated with @EmbeddedEntityResource returned null.");
                }
            }
        }
        return results;
    }

    /**
     * Loops through the related Resources and executed them.  The results are added to list of embedded results used by
     * the ExecutionResult object.
     * 
     * @param relatedResources
     * @param execRes
     * @param uniqueEntityId
     */
    private Map<String,Object> executeRelatedResources(final Api api, Map<String, BeanPropertiesFilter> filters,
                Map<String, ResourceWithMetadata> relatedResources,
                String uniqueEntityId)
    {
        final Map<String,Object> results = new HashMap<String,Object>(relatedResources.size());
        for (final Entry<String, ResourceWithMetadata> relation : relatedResources.entrySet())
        {
            Object execResult = executeRelatedResource(api, filters, uniqueEntityId, relation.getKey(), relation.getValue());
            if (execResult != null)
            {
              results.put(relation.getKey(), execResult);
            }
        }
        return results;
    }

    /**
     * Executes a single related Resource.  The results are added to list of embedded results used by
     * the ExecutionResult object.
     * 
     * @param relatedResources
     * @param uniqueEntityId
     */
    private Object executeRelatedResource(final Api api, final Map<String, BeanPropertiesFilter> filters,
                final String uniqueEntityId, final String resourceKey, final ResourceWithMetadata resource)
    {
        try
        {
            BeanPropertiesFilter paramFilter = null;
            final Object[] resultOfExecution = new Object[1];
            
            if (filters!=null)
            {
                paramFilter = filters.get(resourceKey);
            }
            final Params executionParams = Params.valueOf(paramFilter, uniqueEntityId);
            executor.execute(resource, executionParams, new ExecutionCallback()
            {
                @Override
                public void onSuccess(Object result, ContentInfo contentInfo)
                {
                    resultOfExecution[0] = result;
                }

            });
            
            return resultOfExecution[0];
        }
        catch(NotFoundException e)
        {
        	// ignore, cannot access the object so don't embed it
            if (logger.isDebugEnabled())
            {
                logger.debug("Ignored error, cannot access the object so can't embed it ", e);
            }
        }
        catch(PermissionDeniedException e)
        {
        	// ignore, cannot access the object so don't embed it
            if (logger.isDebugEnabled())
            {
                logger.debug("Ignored error, cannot access the object so can't embed it ", e);
            }
        }
        
        return null; //default
    }

    /**
     * Finds all request parameters that aren't already know about (eg. not paging or filter params)
     * and returns them for use.
     * 
     * @param req - the WebScriptRequest object
     * @return Map<String, String[]> the request parameters
     */
    public static Map<String, String[]> getRequestParameters(WebScriptRequest req)
    {
        if (req!= null)
        {
            String[] paramNames = req.getParameterNames();
            if (paramNames!= null)
            {
                Map<String, String[]> requestParameteters = new HashMap<String, String[]>(paramNames.length);
                
                for (int i = 0; i < paramNames.length; i++)
                {
                    String paramName = paramNames[i];
                    if (!KNOWN_PARAMS.contains(paramName))
                    {
                        String[] vals = req.getParameterValues(paramName);
                        requestParameteters.put(paramName, vals);
                    }
                }
                return requestParameteters;
            }
        }
        
        return Collections.emptyMap();
    }
    
    /**
     * Finds the formal set of params that any rest service could potentially have passed in as request params
     * @param req WebScriptRequest
     * @return RecognizedParams a POJO containing the params for use with the Params objects
     */
    public static RecognizedParams getRecognizedParams(WebScriptRequest req)
    {
        Paging paging = findPaging(req);
        List<SortColumn> sorting = getSort(req.getParameter(ResourceWebScriptHelper.PARAM_ORDERBY));
        Map<String, BeanPropertiesFilter> relationFilter = getRelationFilter(req.getParameter(ResourceWebScriptHelper.PARAM_RELATIONS));
        BeanPropertiesFilter filter = getFilter(req.getParameter(ResourceWebScriptHelper.PARAM_FILTER_PROPS));
        Query whereQuery = getWhereClause(req.getParameter(ResourceWebScriptHelper.PARAM_WHERE));
        Map<String, String[]> requestParams = getRequestParameters(req);
        List<String> theSelect = getSelectClause(req.getParameter(ResourceWebScriptHelper.PARAM_SELECT));
        return new RecognizedParams(requestParams, paging, filter, relationFilter, theSelect, whereQuery, sorting);
    }
    
    /**
     * Find paging setings based on the request parameters.
     * 
     * @param req
     * @return Paging
     */
    public static Paging findPaging(WebScriptRequest req)
    {
        int skipped = Paging.DEFAULT_SKIP_COUNT;
        int max = Paging.DEFAULT_MAX_ITEMS;
        String skip = req.getParameter(PARAM_PAGING_SKIP);
        String maxItems = req.getParameter(PARAM_PAGING_MAX);

        try
        {
            if (skip != null) { skipped = Integer.parseInt(skip);}
            if (maxItems != null) { max = Integer.parseInt(maxItems); }
            if (max < 0 || skipped < 0)
            {
                throw new InvalidArgumentException("Negative values not supported.");  
            }
        }
        catch (NumberFormatException error)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Invalid paging params skip: " + skip + ",maxItems:" + maxItems);
            }
            throw new InvalidArgumentException();
        }

        return Paging.valueOf(skipped, max);
    }

    public void setLocator(ResourceLocator locator)
    {
        this.locator = locator;
    }

    public void setExecutor(ActionExecutor executor)
    {
        this.executor = executor;
    }
        
}

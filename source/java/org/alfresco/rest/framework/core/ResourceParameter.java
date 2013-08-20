package org.alfresco.rest.framework.core;

/**
 * ResourceParameters are used on ResourceOperations
 *
 * The KIND is one of :
 * QUERY_STRING - A query string parameter as part of the URL
 * HTTP_BODY_OBJECT - A JSON object specified in a HTTP_BODY used by either a POST or PUT
 * URL_PATH - Included as part of the actual url, e.g. entity id. (Does not support multiple values)
 * HTTP_HEADER - Included in the request's HTTP Header
 * 
 * @author Gethin James
 */
public class ResourceParameter
{
    public static enum KIND {QUERY_STRING,HTTP_BODY_OBJECT,URL_PATH,HTTP_HEADER}
    private final String name;
    private final boolean required;
    private final String title;
    private final String description;
    private final Class<?> dataType;
    private final KIND kind;
    private final boolean allowMultiple;
    
    public static final ResourceParameter ENTITY_PARAM = new ResourceParameter("entityId",
                "The unique id of the entity being addressed",
                "The unique id must be a String. It is returned as an 'id' from the entity", true,
                String.class, KIND.URL_PATH, false);
    
    public static final ResourceParameter RELATIONSHIP_PARAM = new ResourceParameter("relationshipId",
                "The unique id of the entity relationship being addressed",
                "The unique id must be a String. It is only valid in the scope of the relationship", true,
                String.class, KIND.URL_PATH, false);
    
    public static final ResourceParameter SKIP_PARAM = new ResourceParameter("skipCount",
                "Skip count",
                "An integer describing how many entities exist in the collection before those included in this list.",
                false,
                Integer.class, KIND.QUERY_STRING,
                false); 
    public static final ResourceParameter MAX_ITEMS_PARAM = new ResourceParameter("maxItems",
                "Maximum items",
                "The maximum items request to return.",
                false,
                Integer.class, KIND.QUERY_STRING,
                false);
    public static final ResourceParameter PROPS_PARAM = new ResourceParameter("properties",
                "Properties to include.",
                "The properties parameter is a comma-separated list of property names. You can use the properties parameter to restrict the returned properties.",
                false,
                Integer.class, KIND.QUERY_STRING,
                false);
    public static final ResourceParameter RELATIONS_PARAM = new ResourceParameter("relations",
                "Use the relations parameter to include one or more child entities in a single response. ",
                "You can reduce network traffic by using the relations parameter to include one or more child entities in a single response.",
                false,
                Integer.class, KIND.QUERY_STRING,
                false); 
    public static final ResourceParameter WHERE_PARAM = new ResourceParameter("where",
                "A sql-like where clause",
                "(EXISTS(propertyName)) is currently the only supported operator.",
                false,
                String.class, KIND.QUERY_STRING,
                false); 

    /**
     * @param name - name used in the request
     * @param title - a short description
     * @param description - a long description
     * @param required - is it mandatory?
     * @param dataType - The expected data type of the parameter
     * @param kind - The kind of parameter it is
     * @param allowMultiple - Can allow multiple values?
     */
    private ResourceParameter(String name, String title, String description, boolean required,
                Class<?> dataType, KIND kind, boolean allowMultiple)
    {
        super();
        this.name = name;
        this.title = title;
        this.description = description;
        this.required = required;
        this.dataType = dataType;
        this.kind = kind; 
        if (KIND.URL_PATH.equals(kind)) allowMultiple = false;  //URL paths can never have multiple values
        this.allowMultiple = allowMultiple;
    }
    
    /**
     * Creates a new ResourceParameter.
     * @param name - name used in the request
     * @param title - a short description
     * @param description - a long description
     * @param required - is it mandatory?
     * @param kind - The kind of parameter it is
     * @param allowMultiple - Can allow multiple values?
     * @param dataType - The expected data type of the parameter
     * @return ResourceParameter
     */
    protected static ResourceParameter valueOf(String name, String title, String description, boolean required, KIND kind, boolean allowMultiple, Class<?> dataType)
    {
        return new ResourceParameter(name,title, description, required, dataType, kind, allowMultiple);
    }
    
    public String getName()
    {
        return this.name;
    }

    public boolean isRequired()
    {
        return this.required;
    }

    public String getTitle()
    {
        return this.title;
    }

    public String getDescription()
    {
        return this.description;
    }

    public Class<?> getDataType()
    {
        return this.dataType;
    }

    public boolean isAllowMultiple()
    {
        return this.allowMultiple;
    }
    
    public KIND getParamType()
    {
        return this.kind;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ResourceParameter [name=");
        builder.append(this.name);
        builder.append(", required=");
        builder.append(this.required);
        builder.append(", title=");
        builder.append(this.title);
        builder.append(", description=");
        builder.append(this.description);
        builder.append(", dataType=");
        builder.append(this.dataType);
        builder.append(", kind=");
        builder.append(this.kind);
        builder.append(", allowMultiple=");
        builder.append(this.allowMultiple);
        builder.append("]");
        return builder.toString();
    }
}

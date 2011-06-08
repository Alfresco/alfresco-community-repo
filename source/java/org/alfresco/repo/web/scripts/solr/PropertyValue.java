package org.alfresco.repo.web.scripts.solr;

/*
 * Represents a property value to be used by Freemarker
 * 
 * @since 4.0
 */
class PropertyValue
{
    // is value actually a string or a JSON object or array
    // if true, enclose the value in double quotes (to represent a JSON string)
    // when converting to a string.
    private boolean isString = true;
    
    private String value;
    
    public PropertyValue(boolean isString, String value)
    {
        super();
        this.isString = isString;
        this.value = value;
    }
    public boolean isString()
    {
        return isString;
    }
    public String getValue()
    {
        return value;
    }
    
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        if(isString)
        {
            sb.append("\""); // for json strings
        }
        sb.append(value);
        if(isString)
        {
            sb.append("\""); // for json strings
        }
        return sb.toString();
    }
}

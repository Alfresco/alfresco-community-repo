
package org.alfresco.util.json;

/**
 *
 * @author Nick Smith
 */
public class DefaultJsonSerializer implements JsonSerializer<Object, Object>
{

    /*
     * @see org.alfresco.util.json.JsonSerializer#deserialize(java.lang.Object)
     */
    public Object deserialize(Object object)
    {
        return object;
    }

    /*
     * @see org.alfresco.util.json.JsonSerializer#serialize(java.lang.Object)
     */
    public Object serialize(Object object)
    {
        return object;
    }

}

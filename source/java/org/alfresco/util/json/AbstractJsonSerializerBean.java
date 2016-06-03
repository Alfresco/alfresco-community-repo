
package org.alfresco.util.json;

/**
 * @author Nick Smith
 */
public abstract class AbstractJsonSerializerBean<T, S> implements JsonSerializer<T, S>
{
    private Class<? extends T> classToSerialize;
    private AlfrescoJsonSerializer jsonSerializer;

    /**
     * @param classToSerialize the classToSerialize to set
     */
    public void setClassToSerialize(Class<? extends T> classToSerialize)
    {
        this.classToSerialize = classToSerialize;
    }

    /**
     * @param jsonSerializer the jsonSerializer to set
     */
    public void setJsonSerializer(AlfrescoJsonSerializer jsonSerializer)
    {
        this.jsonSerializer = jsonSerializer;
    }

    public void init()
    {
        jsonSerializer.register(classToSerialize, this);
    }

}

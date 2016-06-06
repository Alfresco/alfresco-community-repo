package org.alfresco.repo.jscript;

import java.util.Map;

/**
 * Register an additional custom metadata output for the DocLib webscript response
 *
 * @author Will Abson
 */
public final class SlingshotDocLibCustomResponseRegistrar
{
    private Map<String, Object> responsesMap;
    private String key;
    private Object value;

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public Object getValue()
    {
        return value;
    }

    public void setValue(Object value)
    {
        this.value = value;
    }

    public Map<String, Object> getResponsesMap()
    {
        return responsesMap;
    }

    public void setResponsesMap(Map<String, Object> responsesMap)
    {
        this.responsesMap = responsesMap;
    }

    public void addCustomResponse()
    {
        responsesMap.put(key, value);
    }
}

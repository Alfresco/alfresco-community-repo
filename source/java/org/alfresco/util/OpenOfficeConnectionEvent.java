package org.alfresco.util;

import java.util.Map;

import org.springframework.context.ApplicationEvent;

/**
 * A class of event that notifies the listener of the status of the Open Office Connection. Useful for Monitoring
 * purposes.
 * 
 * @author dward
 */
public class OpenOfficeConnectionEvent extends ApplicationEvent
{
    private static final long serialVersionUID = 8834274840220309384L;

    /**
     * The Constructor.
     * 
     * @param metaData
     *            the meta data map
     */
    public OpenOfficeConnectionEvent(Map<String, Object> metaData)
    {
        super(metaData);
    }

    /**
     * Gets the meta data map.
     * 
     * @return the meta data map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getMetaData()
    {
        return (Map<String, Object>) getSource();
    }
}

package org.alfresco.opencmis.mapping;

import java.io.Serializable;

import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;

/**
 * Property accessor for fixed value mapping (eg to null, true, etc)
 * 
 * @author florian.mueller
 */
public class FixedValueProperty extends AbstractProperty
{
    private Serializable value;

    /**
     * Construct
     * 
     * @param serviceRegistry ServiceRegistry
     * @param connector CMISConnector
     * @param propertyName String
     * @param value Serializable
     */
    public FixedValueProperty(ServiceRegistry serviceRegistry, CMISConnector connector, String propertyName,
            Serializable value)
    {
        super(serviceRegistry, connector, propertyName);
        this.value = value;
    }

    public Serializable getValueInternal(CMISNodeInfo nodeInfo)
    {
        return value;
    }
}

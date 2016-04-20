package org.alfresco.opencmis.mapping;

import java.io.Serializable;
import java.util.Collections;

import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISDictionaryService;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.apache.chemistry.opencmis.commons.PropertyIds;

/**
 * Get the CMIS object type id property
 * 
 * @author florian.mueller
 */
public class ObjectTypeIdProperty extends AbstractProperty
{
    /**
     * Construct
     *
     * @param serviceRegistry ServiceRegistry
     * @param connector CMISConnector
     * @param dictionaryService CMISDictionaryService
     */
    public ObjectTypeIdProperty(ServiceRegistry serviceRegistry, CMISConnector connector,
            CMISDictionaryService dictionaryService)
    {
        super(serviceRegistry, connector, PropertyIds.OBJECT_TYPE_ID);
    }

    public Serializable getValueInternal(CMISNodeInfo nodeInfo)
    {
        if(nodeInfo.getType() == null)
        {
            return (Serializable) Collections.emptyList();
        }
        
        return nodeInfo.getType().getTypeId();
    }
}

package org.alfresco.opencmis.mapping;

import java.io.Serializable;

import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;

/**
 * Get the CMIS object type id property
 * 
 * @author florian.mueller
 */
public class BaseTypeIdProperty extends AbstractProperty
{
    /**
     * Construct
     * 
     * @param serviceRegistry ServiceRegistry
     * @param connector CMISConnector
     */
    public BaseTypeIdProperty(ServiceRegistry serviceRegistry, CMISConnector connector)
    {
        super(serviceRegistry, connector, PropertyIds.BASE_TYPE_ID);
    }

    public Serializable getValueInternal(CMISNodeInfo nodeInfo)
    {
        if (nodeInfo.isFolder())
        {
            return BaseTypeId.CMIS_FOLDER.value();
        } 
        else if (nodeInfo.isRelationship())
        {
            return BaseTypeId.CMIS_RELATIONSHIP.value();
        }
        else if(nodeInfo.isItem())
        {
        	return BaseTypeId.CMIS_ITEM.value();
        }

        return BaseTypeId.CMIS_DOCUMENT.value();
    }
}

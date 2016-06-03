package org.alfresco.opencmis.mapping;

import java.io.Serializable;

import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.PropertyIds;

/**
 * Get the CMIS object description property.
 * 
 * @author sergey.scherbovich
 */
public class DescriptionProperty extends AbstractProperty
{
    /**
     * Construct
     * 
     * @param serviceRegistry ServiceRegistry
     * @param connector CMISConnector
     */
    public DescriptionProperty(ServiceRegistry serviceRegistry, CMISConnector connector)
    {
        super(serviceRegistry, connector, PropertyIds.DESCRIPTION);
    }

    @Override
    public Serializable getValueInternal(CMISNodeInfo nodeInfo)
    {
        if (nodeInfo.getNodeRef() != null)
        {
            return nodeInfo.getNodeProps().get(ContentModel.PROP_DESCRIPTION);
        }
        else if (nodeInfo.getAssociationRef() != null)
        {
            return getServiceRegistry().getNodeService().getProperty(
                    nodeInfo.getAssociationRef().getSourceRef(),
                    ContentModel.PROP_DESCRIPTION);
        }
        
        return null;
    }

    @Override
    public QName getMappedProperty()
    {
        return ContentModel.PROP_DESCRIPTION;
    }
}

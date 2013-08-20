package org.alfresco.opencmis.mapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;

import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.PropertyIds;

/**
 * 
 * @author steveglover
 *
 */
public class SecondaryTypesProperty extends AbstractProperty
{
    private CMISMapping cmisMapping;

    /**
     * Construct
     */
    public SecondaryTypesProperty(ServiceRegistry serviceRegistry, CMISConnector connector, CMISMapping cmisMapping)
    {
        super(serviceRegistry, connector, PropertyIds.SECONDARY_OBJECT_TYPE_IDS);
        this.cmisMapping = cmisMapping;
    }

    @Override
    public Serializable getValueInternal(CMISNodeInfo nodeInfo)
    {
        NodeRef nodeRef = nodeInfo.getNodeRef();

        if(nodeRef == null || nodeInfo.getType() == null)
        {
        	// If the nodeRef or type is null, we can't handle it so return an empty list	
        	return (Serializable) Collections.emptyList();
        }

        Set<QName> aspects = connector.getNodeService().getAspects(nodeRef);
        ArrayList<String> results = new ArrayList<String>(aspects.size());
        for (QName aspect : aspects)
        {
        	String typeId = cmisMapping.getCmisTypeId(aspect);
        	if (typeId != null)
        	{
        		results.add(typeId);
        	}
        }
        return results;
    }
}

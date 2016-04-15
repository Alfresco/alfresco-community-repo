package org.alfresco.opencmis.mapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;

import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.apache.chemistry.opencmis.commons.PropertyIds;

/**
 * Get the CMIS allowedChildObjectTypeIds property.
 * 
 * @author florian.mueller
 */
public class AllowedChildObjectTypeIdsProperty extends AbstractProperty
{
    private CMISMapping cmisMapping;

    /**
     * Construct
     *
     * @param serviceRegistry ServiceRegistry
     * @param connector CMISConnector
     * @param cmisMapping CMISMapping
     */
    public AllowedChildObjectTypeIdsProperty(ServiceRegistry serviceRegistry, CMISConnector connector,
            CMISMapping cmisMapping)
    {
        super(serviceRegistry, connector, PropertyIds.ALLOWED_CHILD_OBJECT_TYPE_IDS);
        this.cmisMapping = cmisMapping;
    }

    @Override
    public Serializable getValueInternal(CMISNodeInfo nodeInfo)
    {
        if(nodeInfo.getType() == null)
        {
        	//If the type is null, we can't handle it so return an empty list	
        	return (Serializable) Collections.emptyList();
        }

        TypeDefinition type = getServiceRegistry().getDictionaryService()
                .getType(nodeInfo.getType().getAlfrescoClass());
        if ((type != null) && (type.getChildAssociations() != null) && (!type.getChildAssociations().isEmpty()))
        {
            ArrayList<String> result = new ArrayList<String>();

            for (ChildAssociationDefinition cad : type.getChildAssociations().values())
            {
                String typeId = cmisMapping.getCmisTypeId(cad.getTargetClass().getName());
                if (typeId != null)
                {
                    result.add(typeId);
                }
            }

            return result;
        }

        return (Serializable) Collections.emptyList();
    }
}

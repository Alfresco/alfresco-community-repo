package org.alfresco.opencmis.mapping;

import java.io.Serializable;

import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.apache.chemistry.opencmis.commons.PropertyIds;

/**
 * 'cmis:isPrivateWokringCopy' property accessor for CMIS 1.1 (<a href="https://issues.alfresco.com/jira/browse/MNT-11631">MNT-11631</a>)
 * 
 * @author Dmitry Velichkevich
 */
public class IsPrivateWorkingCopy extends AbstractProperty
{
    protected IsPrivateWorkingCopy(ServiceRegistry serviceRegistry, CMISConnector connector)
    {
        super(serviceRegistry, connector, PropertyIds.IS_PRIVATE_WORKING_COPY);
    }

    @Override
    protected Serializable getValueInternal(CMISNodeInfo nodeInfo)
    {
        if ((null == nodeInfo) || !nodeInfo.isDocument())
        {
            return null;
        }

        return connector.getCheckOutCheckInService().isWorkingCopy(nodeInfo.getNodeRef());
    }
}

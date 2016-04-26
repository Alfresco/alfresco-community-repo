package org.alfresco.opencmis.mapping;

import java.io.Serializable;

import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentData;
import org.apache.chemistry.opencmis.commons.PropertyIds;

/**
 * Accessor for CMIS content stream property id
 * 
 * @author florian.mueller
 */
public class ContentStreamIdProperty extends AbstractProperty
{
    /**
     * Construct
     * 
     * @param serviceRegistry ServiceRegistry
     * @param connector CMISConnector
     */
    public ContentStreamIdProperty(ServiceRegistry serviceRegistry, CMISConnector connector)
    {
        super(serviceRegistry, connector, PropertyIds.CONTENT_STREAM_ID);
    }

    public Serializable getValueInternal(CMISNodeInfo nodeInfo)
    {
        ContentData contentData = getContentData(nodeInfo);

        if (contentData != null)
        {
            return contentData.getContentUrl();
        }
        return null;
    }
}

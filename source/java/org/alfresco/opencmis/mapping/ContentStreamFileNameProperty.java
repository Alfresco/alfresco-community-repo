package org.alfresco.opencmis.mapping;

import java.io.Serializable;

import org.alfresco.model.ContentModel;
import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentData;
import org.apache.chemistry.opencmis.commons.PropertyIds;

/**
 * Accessor for CMIS content stream filename property
 * 
 * @author alex.mukha
 * @since 4.2.4
 */
public class ContentStreamFileNameProperty extends DirectProperty
{
    public ContentStreamFileNameProperty(ServiceRegistry serviceRegistry, CMISConnector connector)
    {
        super(serviceRegistry, connector, PropertyIds.CONTENT_STREAM_FILE_NAME, ContentModel.PROP_NAME);
    }
    
    @Override
    public Serializable getValueInternal(CMISNodeInfo nodeInfo)
    {
        ContentData contentData = getContentData(nodeInfo);
        if (contentData != null && contentData.getSize() > 0)
        {
            return super.getValueInternal(nodeInfo);
        }
        else
        {
            return null;
        }
    }
}

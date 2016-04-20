package org.alfresco.opencmis.mapping;

import java.io.Serializable;

import org.alfresco.opencmis.CMISConnector;
import org.alfresco.opencmis.dictionary.CMISNodeInfo;
import org.alfresco.repo.node.getchildren.GetChildrenCannedQuery;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.PropertyIds;

/**
 * Accessor for CMIS content stream length property
 * 
 * @author florian.mueller
 */
public class ContentStreamLengthProperty extends AbstractProperty
{
    /**
     * Construct
     * 
     * @param serviceRegistry ServiceRegistry
     * @param connector CMISConnector
     */
    public ContentStreamLengthProperty(ServiceRegistry serviceRegistry, CMISConnector connector)
    {
        super(serviceRegistry, connector, PropertyIds.CONTENT_STREAM_LENGTH);
    }

    public Serializable getValueInternal(CMISNodeInfo nodeInfo)
    {
        ContentData contentData = getContentData(nodeInfo);

        if (contentData != null)
        {
            return contentData.getSize();
        }
        return null;
    }

    public QName getMappedProperty()
    {
        // spoof
        return GetChildrenCannedQuery.SORT_QNAME_CONTENT_SIZE;
    }
}

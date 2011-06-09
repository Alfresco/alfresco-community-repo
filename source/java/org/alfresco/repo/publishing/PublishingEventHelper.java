/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.repo.publishing;

import java.io.OutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.publishing.PublishingPackage;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author Brian
 *
 */
public class PublishingEventHelper
{
    private static final Log log = LogFactory.getLog(PublishingEventHelper.class);

    private NodeService nodeService;
    private ContentService contentService;
    private PublishingPackageSerializer serializer;
    
    /**
     * @param nodeService
     *            the nodeService to set
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param contentService
     *            the contentService to set
     */
    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    /**
     * @param serializer the serializer to set
     */
    public void setSerializer(PublishingPackageSerializer serializer)
    {
        this.serializer = serializer;
    }

    public NodeRef create(NodeRef queueId, PublishingPackage publishingPackage, Calendar schedule, String comment)
        throws Exception
    {
        if (schedule == null)
        {
            schedule = Calendar.getInstance();
        }
        Map<QName, Serializable> props = new HashMap<QName, Serializable>();
        String name = GUID.generate();
        props.put(ContentModel.PROP_NAME, name);
        props.put(PublishingModel.PROP_PUBLISHING_EVENT_TIME, schedule.getTime());
        props.put(PublishingModel.PROP_PUBLISHING_EVENT_TIME_ZONE, schedule.getTimeZone().getID());
        if (comment != null)
        {
            props.put(PublishingModel.PROP_PUBLISHING_EVENT_COMMENT, comment);
        }
        ChildAssociationRef newAssoc = nodeService.createNode(queueId, PublishingModel.ASSOC_PUBLISHING_EVENT, QName
                .createQName(PublishingModel.NAMESPACE, name), PublishingModel.TYPE_PUBLISHING_EVENT, props);

        try
        {
            ContentWriter contentWriter = contentService.getWriter(newAssoc.getChildRef(),
                    PublishingModel.PROP_PUBLISHING_EVENT_PAYLOAD, true);
            contentWriter.setEncoding("UTF-8");
            OutputStream os = contentWriter.getContentOutputStream();
            serializer.serialize(publishingPackage, os);
            os.flush();
            os.close();
        }
        catch (Exception ex)
        {
            log.warn("Failed to serialize publishing package", ex);
            throw ex;
        }
        return newAssoc.getChildRef();
    }
}

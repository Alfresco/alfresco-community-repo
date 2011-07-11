/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
package org.alfresco.repo.publishing.slideshare;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.publishing.AbstractChannelType;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

public class SlideShareChannelType extends AbstractChannelType
{
    public final static String ID = "slideshare";
    private final static Set<String> DEFAULT_MIME_TYPES = new TreeSet<String>(); 

    private NodeService nodeService;
    private ActionService actionService;
    private Set<String> permittedMimeTypes = Collections.unmodifiableSet(DEFAULT_MIME_TYPES);
    
    static
    {
        DEFAULT_MIME_TYPES.add(MimetypeMap.MIMETYPE_PPT);
        DEFAULT_MIME_TYPES.add(MimetypeMap.MIMETYPE_PDF);
        DEFAULT_MIME_TYPES.add(MimetypeMap.MIMETYPE_OPENDOCUMENT_PRESENTATION);
        DEFAULT_MIME_TYPES.add(MimetypeMap.MIMETYPE_OPENXML_PRESENTATION);
        DEFAULT_MIME_TYPES.add(MimetypeMap.MIMETYPE_IWORK_KEYNOTE);
        DEFAULT_MIME_TYPES.add(MimetypeMap.MIMETYPE_IWORK_PAGES);
        DEFAULT_MIME_TYPES.add(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        DEFAULT_MIME_TYPES.add(MimetypeMap.MIMETYPE_OPENDOCUMENT_TEXT);
        DEFAULT_MIME_TYPES.add(MimetypeMap.MIMETYPE_TEXT_CSV);
        DEFAULT_MIME_TYPES.add(MimetypeMap.MIMETYPE_EXCEL);
        DEFAULT_MIME_TYPES.add(MimetypeMap.MIMETYPE_OPENXML_WORDPROCESSING);
        DEFAULT_MIME_TYPES.add(MimetypeMap.MIMETYPE_OPENDOCUMENT_SPREADSHEET);
    }
    
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }

    public void setPermittedMimeTypes(Set<String> permittedMimeTypes)
    {
        if (permittedMimeTypes == null)
        {
            permittedMimeTypes = Collections.emptySet();
        }
        this.permittedMimeTypes = Collections.unmodifiableSet(permittedMimeTypes);
    }

    @Override
    public boolean canPublish()
    {
        return true;
    }

    @Override
    public boolean canPublishStatusUpdates()
    {
        return false;
    }

    @Override
    public boolean canUnpublish()
    {
        return false;
    }

    @Override
    public QName getChannelNodeType()
    {
        return SlideSharePublishingModel.TYPE_DELIVERY_CHANNEL;
    }

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public Set<QName> getSupportedContentTypes()
    {
        return Collections.emptySet();
    }

    @Override
    public Set<String> getSupportedMimetypes()
    {
        return permittedMimeTypes;
    }

    @Override
    public void publish(NodeRef nodeToPublish, Map<QName, Serializable> properties)
    {
        Action publishAction = actionService.createAction(SlideSharePublishAction.NAME);
        actionService.executeAction(publishAction, nodeToPublish);
    }

    @Override
    public void unpublish(NodeRef nodeToUnpublish, Map<QName, Serializable> properties)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateStatus(Channel channel, String status, Map<QName, Serializable> properties)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getNodeUrl(NodeRef node)
    {
        String url = null;
        if (node != null && nodeService.exists(node) && nodeService.hasAspect(node, SlideSharePublishingModel.ASPECT_ASSET))
        {
            url = (String)nodeService.getProperty(node, SlideSharePublishingModel.PROP_PLAYER_URL);
        }
        return url;
    }
}

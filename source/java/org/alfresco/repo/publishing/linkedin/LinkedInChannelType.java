package org.alfresco.repo.publishing.linkedin;

import static org.alfresco.repo.publishing.linkedin.LinkedInPublishingModel.TYPE_DELIVERY_CHANNEL;

import org.alfresco.repo.publishing.AbstractOAuth1ChannelType;
import org.alfresco.repo.publishing.linkedin.springsocial.api.AlfrescoLinkedIn;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.social.connect.Connection;

/**
 * @author Nick Smith
 * @author Brian
 * @since 4.0
 */
public class LinkedInChannelType extends AbstractOAuth1ChannelType<AlfrescoLinkedIn>
{
    private final static Log log = LogFactory.getLog(LinkedInChannelType.class);
    public final static String ID = "linkedin";
    
    @Override
    public boolean canPublish()
    {
        return false;
    }

    @Override
    public boolean canPublishStatusUpdates()
    {
        return true;
    }

    @Override
    public boolean canUnpublish()
    {
        return false;
    }

    @Override
    public QName getChannelNodeType()
    {
        return TYPE_DELIVERY_CHANNEL;
    }

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public int getMaximumStatusLength()
    {
        return 700;
    }

    @Override
    public void sendStatusUpdate(Channel channel, String status)
    {
        NodeRef channelNode = new NodeRef(channel.getId());
        Connection<AlfrescoLinkedIn> connection = getConnectionForChannel(channelNode);
        if (log.isInfoEnabled())
        {
            log.info("Posting update to LinkedIn channel " + channel.getName() + ": " + status);
        }
        connection.getApi().shareComment(status);
    }

    @Override
    public String getNodeUrl(NodeRef node)
    {
        return null;
    }
}

package org.alfresco.repo.publishing.twitter;

import org.alfresco.repo.publishing.AbstractOAuth1ChannelType;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.social.connect.Connection;
import org.springframework.social.twitter.api.Twitter;

/**
 * 
 * @author Brian
 * @since 4.0
 */
public class TwitterChannelType extends AbstractOAuth1ChannelType<Twitter>
{
    private final static Log log = LogFactory.getLog(TwitterChannelType.class);
    public final static String ID = "twitter";

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
        return TwitterPublishingModel.TYPE_DELIVERY_CHANNEL;
    }

    @Override
    public String getId()
    {
        return ID;
    }

    @Override
    public int getMaximumStatusLength()
    {
        return 140;
    }

    @Override
    public void sendStatusUpdate(Channel channel, String status)
    {
        Connection<Twitter> connection = getConnectionForChannel(channel.getNodeRef());
        if (log.isInfoEnabled())
        {
            log.info("Posting update to Twitter channel " + channel.getName() + ": " + status);
        }
        connection.getApi().timelineOperations().updateStatus(status);
    }

    @Override
    public String getNodeUrl(NodeRef node)
    {
        return null;
    }
    
}

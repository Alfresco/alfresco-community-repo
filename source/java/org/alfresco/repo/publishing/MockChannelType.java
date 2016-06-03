
package org.alfresco.repo.publishing;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * For test purposes only.
 * 
 * @author Nick Smith
 * @since 4.0
 */
public class MockChannelType extends AbstractChannelType
{
    public final static String ID = "MockChannelType";
    
    /**
    * {@inheritDoc}
    */
    public String getId()
    {
        return ID;
    }

    public Map<String, String> getCapabilities()
    {
        return null;
    }

    public QName getChannelNodeType()
    {
        return PublishingModel.TYPE_DELIVERY_CHANNEL;
    }

    public QName getContentRootNodeType()
    {
        return ContentModel.TYPE_FOLDER;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void publish(NodeRef nodeToPublish, Map<QName, Serializable> properties)
    {
        // NOOP
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void unpublish(NodeRef nodeToUnpublish, Map<QName, Serializable> properties)
    {
        //NOOP
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public void sendStatusUpdate(Channel channel, String status)
    {
        //NOOP
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public boolean canPublish()
    {
        return false;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public boolean canUnpublish()
    {
        return false;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public Set<String> getSupportedMimeTypes()
    {
        return null;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public Set<QName> getSupportedContentTypes()
    {
        return null;
    }

    /**
    * {@inheritDoc}
    */
    @Override
    public boolean canPublishStatusUpdates()
    {
        return false;
    }

    /**
    * {@inheritDoc}
    */
    public String getNodeUrl(NodeRef node)
    {
        return null;
    }

    @Override
    public AuthUrlPair getAuthorisationUrls(Channel channel, String callbackUrl)
    {
        return new AuthUrlPair("", "");
    }
}

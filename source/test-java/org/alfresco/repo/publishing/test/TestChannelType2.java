
package org.alfresco.repo.publishing.test;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.publishing.AbstractChannelType;
import org.alfresco.repo.publishing.PublishingModel;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public class TestChannelType2 extends AbstractChannelType
{

    @Override
    public boolean canPublish()
    {
        return true;
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
        return PublishingModel.TYPE_DELIVERY_CHANNEL;
    }

    @Override
    public String getId()
    {
        return "TestChannelType2";
    }

    @Override
    public int getMaximumStatusLength()
    {
        return 140;
    }

    @Override
    public String getNodeUrl(NodeRef node)
    {
        return node.getId();
    }

    @Override
    public Set<QName> getSupportedContentTypes()
    {
        return null;
    }

    @Override
    public Set<String> getSupportedMimeTypes()
    {
        return null;
    }

    @Override
    public void publish(NodeRef nodeToPublish, Map<QName, Serializable> properties)
    {
        //Deliberately blank
    }

    @Override
    public void unpublish(NodeRef nodeToUnpublish, Map<QName, Serializable> properties)
    {
        //Deliberately blank
    }

    @Override
    public void sendStatusUpdate(Channel channel, String status)
    {
        //Deliberately blank
    }

    @Override
    public AuthUrlPair getAuthorisationUrls(Channel channel, String callbackUrl)
    {
        return null;
    }

}

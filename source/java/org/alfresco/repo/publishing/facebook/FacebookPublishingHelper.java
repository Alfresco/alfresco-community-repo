package org.alfresco.repo.publishing.facebook;

import org.alfresco.repo.node.encryption.MetadataEncryptor;
import org.alfresco.repo.publishing.PublishingModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.social.connect.Connection;
import org.springframework.social.facebook.api.Facebook;
import org.springframework.social.facebook.connect.FacebookConnectionFactory;
import org.springframework.social.oauth2.AccessGrant;

/**
 * @author Brian
 * @since 4.0
 */
public class FacebookPublishingHelper
{
    private NodeService nodeService;
    private FacebookConnectionFactory connectionFactory;
    private MetadataEncryptor encryptor;

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setConnectionFactory(FacebookConnectionFactory connectionFactory)
    {
        this.connectionFactory = connectionFactory;
    }

    public FacebookConnectionFactory getConnectionFactory()
    {
        return connectionFactory;
    }

    public void setEncryptor(MetadataEncryptor encryptor)
    {
        this.encryptor = encryptor;
    }

    public Connection<Facebook> getFacebookConnectionForChannel(NodeRef channelNode)
    {
        Connection<Facebook> connection = null;
        if (nodeService.exists(channelNode)
                && nodeService.hasAspect(channelNode, FacebookPublishingModel.ASPECT_DELIVERY_CHANNEL))
        {
            String tokenValue = (String) encryptor.decrypt(PublishingModel.PROP_OAUTH2_TOKEN, nodeService.getProperty(
                    channelNode, PublishingModel.PROP_OAUTH2_TOKEN));
            Boolean danceComplete = (Boolean) nodeService.getProperty(channelNode, PublishingModel.PROP_AUTHORISATION_COMPLETE);
            
            if (danceComplete)
            {
                AccessGrant token = new AccessGrant(tokenValue);
                connection = connectionFactory.createConnection(token);
            }
        }
        return connection;
    }

}

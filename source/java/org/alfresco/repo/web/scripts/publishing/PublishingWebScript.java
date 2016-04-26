
package org.alfresco.repo.web.scripts.publishing;

import org.alfresco.service.cmr.publishing.PublishingQueue;
import org.alfresco.service.cmr.publishing.PublishingService;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.springframework.extensions.webscripts.DeclarativeWebScript;

/**
 * @author Nick Smith
 * @since 4.0
 */
public abstract class PublishingWebScript extends DeclarativeWebScript
{
    protected final PublishingJsonParser jsonParser = new PublishingJsonParser();
    protected final PublishingModelBuilder builder= new PublishingModelBuilder();

    protected PublishingService publishingService;
    protected ChannelService channelService;

    /**
     * @param publishingService the publishingService to set
     */
    public void setPublishingService(PublishingService publishingService)
    {
        this.publishingService = publishingService;
    }
    
    /**
     * @param channelService the channelService to set
     */
    public void setChannelService(ChannelService channelService)
    {
        this.channelService = channelService;
    }
}
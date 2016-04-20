
package org.alfresco.repo.web.scripts.publishing;

import java.util.List;
import java.util.Map;

import org.alfresco.repo.web.scripts.WebScriptUtil;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Nick Smith
 * @since 4.0
 */
public class ChannelTypesGet extends DeclarativeWebScript
{
    private final PublishingModelBuilder builder = new PublishingModelBuilder();
    private ChannelService channelService;

    /**
    * {@inheritDoc}
    */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        List<ChannelType> types = channelService.getChannelTypes();
        List<Map<String, Object>> channelTypesModel = builder.buildChannelTypes(types);
        return WebScriptUtil.createBaseModel(channelTypesModel);
    }

    /**
     * @param channelService the channelService to set
     */
    public void setChannelService(ChannelService channelService)
    {
        this.channelService = channelService;
    }
}

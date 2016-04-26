
package org.alfresco.repo.web.scripts.publishing;

import java.util.Map;

import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Brian
 * @since 4.0
 */
public class ChannelsPostWebScript extends DeclarativeWebScript
{
    private ChannelService channelService;
    private ChannelAuthHelper channelAuthHelper;

    public void setChannelService(ChannelService channelService)
    {
        this.channelService = channelService;
    }

    public void setChannelAuthHelper(ChannelAuthHelper channelAuthHelper)
    {
        this.channelAuthHelper = channelAuthHelper;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        String channelType = req.getParameter("channelType");
        String channelName = req.getParameter("channelName");

        Channel newChannel = channelService.createChannel(channelType, channelName, null);

        return channelAuthHelper.buildAuthorisationModel(newChannel);
    }
}


package org.alfresco.repo.web.scripts.publishing;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.web.scripts.WebScriptUtil;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.springframework.extensions.surf.util.URLDecoder;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * @author Nick Smith
 * @since 4.0
 */
public class ChannelPut extends AbstractWebScript
{
    private static final  String CHANNEL_ID = "channel_id";
    
    private final PublishingJsonParser parser = new PublishingJsonParser();
    private ChannelService channelService;
    
    
    /**
    * {@inheritDoc}
    */
    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res)
    {
        Map<String, String> params = req.getServiceMatch().getTemplateVars();
        String channelId = URLDecoder.decode(params.get(CHANNEL_ID));
        Channel channel = channelService.getChannelById(channelId);
        if (channel == null)
        {
            String msg = "No channel found for ID: " + channelId;
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, msg);
        }
        String content = null;
        try
        {
            content = WebScriptUtil.getContent(req);
            if (content == null || content.isEmpty())
            {
                throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "No publishing event was posted!");
            }
            parser.updateChannel(channel, content, channelService);
        }
        catch (Exception e)
        {
            String msg = "Failed to Rename Channel: " + channelId + ". POST body: " + content;
            throw new WebScriptException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg, e);
        }
    }

    /**
     * @param channelService the channelService to set
     */
    public void setChannelService(ChannelService channelService)
    {
        this.channelService = channelService;
    }
}


package org.alfresco.repo.web.scripts.publishing;

import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * @author Brian
 * @since 4.0
 */
public class ChannelReauthWebScript extends DeclarativeWebScript
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
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        
        String channelNodeUuid = templateVars.get("node_id");
        String channelNodeStoreProtocol = templateVars.get("store_protocol");
        String channelNodeStoreId = templateVars.get("store_id");

        if (channelNodeStoreId == null || channelNodeStoreProtocol == null || channelNodeUuid == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Missing parameter(s)");
        }
        
        NodeRef channelNodeRef = new NodeRef(channelNodeStoreProtocol, channelNodeStoreId, channelNodeUuid);
        Channel channel = channelService.getChannelById(channelNodeRef.toString());

        if (channel == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Channel not found");
        }
        
        return channelAuthHelper.buildAuthorisationModel(channel);
    }
}

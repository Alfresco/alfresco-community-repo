
package org.alfresco.repo.web.scripts.publishing;

import java.util.Map;
import java.util.TreeMap;

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
public class AuthFormGetWebScript extends DeclarativeWebScript
{
    private ChannelService channelService;

    public void setChannelService(ChannelService channelService)
    {
        this.channelService = channelService;
    }

    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();

        String channelNodeUuid = templateVars.get("node_id");
        String channelNodeStoreProtocol = templateVars.get("store_protocol");
        String channelNodeStoreId = templateVars.get("store_id");

        NodeRef channelNodeRef = new NodeRef(channelNodeStoreProtocol, channelNodeStoreId, channelNodeUuid);
        Channel channel = channelService.getChannelById(channelNodeRef.toString());
        Map<String,Object> model = new TreeMap<String, Object>();

        if (channel == null)
        {
            throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "Channel not found - " + channelNodeRef); 
        }
        else
        {
            model.put("channel", channel);
        }
        return model;
    }
}

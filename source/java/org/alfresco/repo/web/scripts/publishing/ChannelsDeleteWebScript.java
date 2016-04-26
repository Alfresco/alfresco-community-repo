
package org.alfresco.repo.web.scripts.publishing;

import java.io.IOException;
import java.util.Map;

import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

/**
 * @author Brian
 * @since 4.0
 */
public class ChannelsDeleteWebScript extends AbstractWebScript
{
    private ChannelService channelService;

    public void setChannelService(ChannelService channelService)
    {
        this.channelService = channelService;
    }

    @Override
    public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException
    {
        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        
        String channelNodeUuid = templateVars.get("node_id");
        String channelNodeStoreProtocol = templateVars.get("store_protocol");
        String channelNodeStoreId = templateVars.get("store_id");

        if (channelNodeStoreId == null || channelNodeStoreProtocol == null || channelNodeUuid == null)
        {
            res.setStatus(Status.STATUS_BAD_REQUEST);
            return;
        }
        
        NodeRef channelNodeRef = new NodeRef(channelNodeStoreProtocol, channelNodeStoreId, channelNodeUuid);
        Channel channel = channelService.getChannelById(channelNodeRef.toString());
        if (channel == null)
        {
            res.setStatus(Status.STATUS_NOT_FOUND);
            return;
        }

        try
        {
            channelService.deleteChannel(channel);
        }
        catch (AccessDeniedException ex)
        {
            res.setStatus(Status.STATUS_UNAUTHORIZED);
        }
    }
}

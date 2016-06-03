
package org.alfresco.repo.publishing;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.alfresco.repo.action.constraint.BaseParameterConstraint;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;

/**
 * Action parameter constraint that constrains to list of publishing channels
 * 
 * @see PublishContentActionExecuter
 * @author Brian
 * @since 4.0
 */
public class PublishingChannelParameterConstraint extends BaseParameterConstraint
{
    public static final String NAME = "ac-publishing-channels";

    private ChannelService channelService;

    public void setChannelService(ChannelService channelService)
    {
        this.channelService = channelService;
    }

    /**
     * @see org.alfresco.service.cmr.action.ParameterConstraint#getAllowableValues()
     */
    protected Map<String, String> getAllowableValuesImpl()
    {
        List<Channel> channels = channelService.getPublishingChannels(false);
        Map<String, String> result = new TreeMap<String, String>();
        for (Channel channel : channels)
        {
            result.put(channel.getId(), channel.getName());
        }
        return result;
    }

}

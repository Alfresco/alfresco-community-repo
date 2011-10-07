/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.repo.publishing;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.action.ParameterizedItem;
import org.alfresco.service.cmr.action.ParameterizedItemDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.publishing.PublishingDetails;
import org.alfresco.service.cmr.publishing.PublishingService;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleServiceException;

/**
 * This class defines an action that publishes or unpublishes the acted-upon
 * node to a specified publishing channel.
 * 
 * @author Brian
 * @since 4.0
 */
public class PublishContentActionExecuter extends ActionExecuterAbstractBase
{
    public final static String NAME = "publish_content";

    /**
     * A single-valued, optional text parameter that names the publishing
     * channel to which the specified content is to be published. Although this
     * is optional, one of either "publish-channel-name" or "publish-channel-id"
     * MUST be specified. If both are specified then "publish-channel-id" takes
     * precedence.
     * 
     * @see PublishContentActionExecuter#PARAM_PUBLISH_CHANNEL_ID
     */
    public final static String PARAM_PUBLISH_CHANNEL_NAME = "publish-channel-name";

    /**
     * A single-valued, optional text parameter that identifies the publishing
     * channel to which the specified content is to be published. Although this
     * is optional, one of either "publish-channel-name" or "publish-channel-id"
     * MUST be specified. If both are specified then "publish-channel-id" takes
     * precedence.
     * 
     * @see PublishContentActionExecuter#PARAM_PUBLISH_CHANNEL_NAME
     */
    public final static String PARAM_PUBLISH_CHANNEL_ID = "publish-channel-id";

    /**
     * A single-valued, optional boolean parameter that indicates whether the
     * node being acted on should be unpublished (true) or published (false, the
     * default).
     */
    public final static String PARAM_UNPUBLISH = "unpublish";

    /**
     * A single-valued, optional text parameter that specifies the text of a
     * status update that is to be sent to the specified channels upon
     * successful publication
     * 
     * @see PublishContentActionExecuter#PARAM_STATUS_UPDATE_CHANNEL_NAMES
     */
    public final static String PARAM_STATUS_UPDATE = "status-update";

    /**
     * A single-valued, optional boolean parameter that specifies whether a link
     * to the published content should be appended (in shortened form) to the
     * status update. Defaults to true if not set.
     * 
     * @see PublishContentActionExecuter#PARAM_STATUS_UPDATE_CHANNEL_NAMES
     * @see PublishContentActionExecuter#PARAM_STATUS_UPDATE
     */
    public final static String PARAM_INCLUDE_LINK_IN_STATUS_UPDATE = "include-link-in-status-update";

    /**
     * A multi-valued, optional text parameter that identifies by name the
     * publishing channels to which the status update (if any) should be sent.
     * If both this parameter and the "status-update-channel-ids" parameter are
     * given values then they are combined.
     * 
     * @see PublishContentActionExecuter#PARAM_STATUS_UPDATE
     * @see PublishContentActionExecuter#PARAM_STATUS_UPDATE_CHANNEL_IDS
     */
    public final static String PARAM_STATUS_UPDATE_CHANNEL_NAMES = "status-update-channel-names";

    /**
     * A multi-valued, optional text parameter that identifies the publishing
     * channels to which the status update (if any) should be sent. If both this
     * parameter and the "status-update-channel-names" parameter are given
     * values then they are combined.
     * 
     * @see PublishContentActionExecuter#PARAM_STATUS_UPDATE
     * @see PublishContentActionExecuter#PARAM_STATUS_UPDATE_CHANNEL_NAMES
     */
    public final static String PARAM_STATUS_UPDATE_CHANNEL_IDS = "status-update-channel-ids";

    /**
     * A single-valued, optional datetime parameter that specifies when the
     * publish should happen.
     */
    public final static String PARAM_SCHEDULED_TIME = "scheduled-time";

    /**
     * A single-valued, optional text parameter that is stored on the publishing
     * event that is created by this action.
     */
    public final static String PARAM_COMMENT = "comment";

    private static final String MSG_CHANNEL_NOT_FOUND = "publishing.channelNotFound";
    private static final String MSG_NEITHER_CHANNEL_NAME_NOR_ID_SPECIFIED = "publishing.neitherNameNorIdSpecified";

    private PublishingService publishingService;
    private ChannelService channelService;

    public void setPublishingService(PublishingService publishingService)
    {
        this.publishingService = publishingService;
    }

    public void setChannelService(ChannelService channelService)
    {
        this.channelService = channelService;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void executeImpl(Action action, NodeRef actionedUponNodeRef)
    {
        Boolean isUnpublish = (Boolean) action.getParameterValue(PARAM_UNPUBLISH);
        boolean unpublish = ((isUnpublish == null) || isUnpublish);
        String publishChannelId = (String) action.getParameterValue(PARAM_PUBLISH_CHANNEL_ID);
        String publishChannelName = (String) action.getParameterValue(PARAM_PUBLISH_CHANNEL_NAME);
        String statusUpdate = (String) action.getParameterValue(PARAM_STATUS_UPDATE);
        List<String> statusUpdateChannelNames = buildStringList(action.getParameterValue(PARAM_STATUS_UPDATE_CHANNEL_NAMES));
        List<String> statusUpdateChannelIds = buildStringList(action.getParameterValue(PARAM_STATUS_UPDATE_CHANNEL_IDS));
        Boolean includeLinkInStatusUpdate = (Boolean) action.getParameterValue(PARAM_INCLUDE_LINK_IN_STATUS_UPDATE);
        boolean appendLink = ((includeLinkInStatusUpdate == null) || includeLinkInStatusUpdate);
        Date scheduledTime = (Date) action.getParameterValue(PARAM_SCHEDULED_TIME);
        String comment = (String) action.getParameterValue(PARAM_COMMENT);

        Channel publishChannel = publishChannelId == null ? channelService.getChannelByName(publishChannelName) :
            channelService.getChannelById(publishChannelId);
        if (publishChannel != null)
        {
            PublishingDetails details = publishingService.createPublishingDetails();
            details.setPublishChannelId(publishChannel.getId());
            if (unpublish)
            {
                details.addNodesToUnpublish(actionedUponNodeRef);
            }
            else
            {
                details.addNodesToPublish(actionedUponNodeRef);
            }
            if (statusUpdateChannelNames != null)
            {
                for (String statusUpdateChannelName : statusUpdateChannelNames)
                {
                    Channel statusUpdateChannel = channelService.getChannelByName(statusUpdateChannelName);
                    if (statusUpdateChannel != null)
                    {
                        details.addStatusUpdateChannels(statusUpdateChannel.getId());
                    }
                }
            }
            if (statusUpdateChannelIds != null)
            {
                for (String statusUpdateChannelId : statusUpdateChannelIds)
                {
                    Channel statusUpdateChannel = channelService.getChannelById(statusUpdateChannelId);
                    if (statusUpdateChannel != null)
                    {
                        details.addStatusUpdateChannels(statusUpdateChannel.getId());
                    }
                }
            }
            if (!details.getStatusUpdateChannels().isEmpty())
            {
                details.setStatusMessage(statusUpdate);
                if (appendLink)
                {
                    details.setStatusNodeToLinkTo(actionedUponNodeRef);
                }
            }
            if (scheduledTime != null)
            {
                Calendar cal = Calendar.getInstance();
                cal.setTime(scheduledTime);
                details.setSchedule(cal);
            }
            details.setComment(comment);
            publishingService.scheduleNewEvent(details);
        }
        else
        {
            throw new AlfrescoRuntimeException(MSG_CHANNEL_NOT_FOUND, new Object[] { publishChannelId == null ? publishChannelName : publishChannelId});
        }
    }

    private List<String> buildStringList(Serializable parameterValue)
    {
        List<String> result = null;
        if (parameterValue != null && String.class.isAssignableFrom(parameterValue.getClass()))
        {
            String[] split = ((String)parameterValue).split(",");
            result = Arrays.asList(split);
        }
        return result;
    }

    @Override
    protected void addParameterDefinitions(List<ParameterDefinition> paramList)
    {
        paramList.add(new ParameterDefinitionImpl(PARAM_PUBLISH_CHANNEL_NAME, DataTypeDefinition.TEXT, false,
                getParamDisplayLabel(PARAM_PUBLISH_CHANNEL_NAME), false));

        paramList.add(new ParameterDefinitionImpl(PARAM_PUBLISH_CHANNEL_ID, DataTypeDefinition.TEXT, false,
                getParamDisplayLabel(PARAM_PUBLISH_CHANNEL_ID), false, "ac-publishing-channels"));

        paramList.add(new ParameterDefinitionImpl(PARAM_UNPUBLISH, DataTypeDefinition.BOOLEAN, false,
                getParamDisplayLabel(PARAM_UNPUBLISH), false));

        paramList.add(new ParameterDefinitionImpl(PARAM_STATUS_UPDATE, DataTypeDefinition.TEXT, false,
                getParamDisplayLabel(PARAM_STATUS_UPDATE), false));

        paramList.add(new ParameterDefinitionImpl(PARAM_INCLUDE_LINK_IN_STATUS_UPDATE, DataTypeDefinition.BOOLEAN,
                false, getParamDisplayLabel(PARAM_INCLUDE_LINK_IN_STATUS_UPDATE), false));

        paramList.add(new ParameterDefinitionImpl(PARAM_STATUS_UPDATE_CHANNEL_NAMES, DataTypeDefinition.TEXT, false,
                getParamDisplayLabel(PARAM_STATUS_UPDATE_CHANNEL_NAMES), true));

        paramList.add(new ParameterDefinitionImpl(PARAM_STATUS_UPDATE_CHANNEL_IDS, DataTypeDefinition.TEXT, false,
                getParamDisplayLabel(PARAM_STATUS_UPDATE_CHANNEL_IDS), true, "ac-status-update-channels"));

        paramList.add(new ParameterDefinitionImpl(PARAM_SCHEDULED_TIME, DataTypeDefinition.DATETIME, false,
                getParamDisplayLabel(PARAM_SCHEDULED_TIME), false));

        paramList.add(new ParameterDefinitionImpl(PARAM_COMMENT, DataTypeDefinition.TEXT, false,
                getParamDisplayLabel(PARAM_COMMENT), false));
    }

    @Override
    protected void checkMandatoryProperties(ParameterizedItem ruleItem, ParameterizedItemDefinition ruleItemDefinition)
    {
        super.checkMandatoryProperties(ruleItem, ruleItemDefinition);
        String publishChannelName = (String) ruleItem.getParameterValue(PARAM_PUBLISH_CHANNEL_NAME);
        String publishChannelId = (String) ruleItem.getParameterValue(PARAM_PUBLISH_CHANNEL_ID);
        if (publishChannelId == null && publishChannelName == null)
        {
            throw new RuleServiceException(MSG_NEITHER_CHANNEL_NAME_NOR_ID_SPECIFIED);
        }
    }
}

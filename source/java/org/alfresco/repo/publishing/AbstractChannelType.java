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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.alfresco.repo.transfer.CompositeNodeFilter;
import org.alfresco.repo.transfer.CompositeNodeFinder;
import org.alfresco.repo.transfer.PrimaryParentNodeFinder;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.publishing.channels.Channel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.publishing.channels.ChannelType;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.transfer.NodeFilter;
import org.alfresco.service.cmr.transfer.NodeFinder;
import org.alfresco.util.ParameterCheck;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Nick Smith
 * @since 4.0
 * 
 */
public abstract class AbstractChannelType implements ChannelType, InitializingBean
{
    private ServiceRegistry serviceRegistry;
    protected NodeFinder nodeFinder;
    protected NodeFilter nodeFilter;

    public void setChannelService(ChannelService channelService)
    {
        channelService.register(this);
    }

    /**
     * @param serviceRegistry
     *            the serviceRegistry to set
     */
    public void setServiceRegistry(ServiceRegistry serviceRegistry)
    {
        this.serviceRegistry = serviceRegistry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void afterPropertiesSet() throws Exception
    {

        Collection<NodeFinder> finders = getAllNodeFInders();
        CompositeNodeFinder finder = new CompositeNodeFinder(finders);
        finder.setServiceRegistry(serviceRegistry);
        finder.init();
        this.nodeFinder = finder;

        Collection<NodeFilter> filters = getAllNodeFIlters();
        CompositeNodeFilter filter = new CompositeNodeFilter(filters);
        filter.setServiceRegistry(serviceRegistry);
        finder.init();
        this.nodeFilter = filter;
    }

    /**
     * @return a collection of {@link NodeFilter}s to be included in the
     *         {@link CompositeNodeFilter} returned by the getNodeFilter()
     *         method.
     */
    protected Collection<NodeFilter> getAllNodeFIlters()
    {
        return Collections.emptyList();
    }

    /**
     * @return a collection of {@link NodeFinder}s to be included in the
     *         {@link CompositeNodeFinder} returned by the getNodeFinder()
     *         method.
     */
    protected Collection<NodeFinder> getAllNodeFInders()
    {
        // TODO Add dependency node finder.
        NodeFinder parentFinder = new PrimaryParentNodeFinder();
        return Arrays.asList(parentFinder);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeFilter getNodeFilter()
    {
        return nodeFilter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeFinder getNodeFinder()
    {
        return nodeFinder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaximumStatusLength()
    {
        return -1;
    }

    @Override
    public String getAuthorisationUrl(Channel channel, String callbackUrl)
    {
        //Returning a null here to indicate that we should use our own credential-gathering mechanism.
        return null;
    }

    @Override
    public boolean acceptAuthorisationCallback(Channel channel, Map<String, String[]> callbackHeaders,
            Map<String, String[]> callbackParams)
    {
        boolean result = false;
        
        ParameterCheck.mandatory("channel", channel);
        ParameterCheck.mandatory("callbackHeaders", callbackHeaders);
        ParameterCheck.mandatory("callbackParams", callbackParams);
        if (!getId().equals(channel.getChannelType().getId()))
        {
            throw new IllegalArgumentException("Supplied channel is of the incorrect type. Expected " + getId()
                    + "; Received " + channel.getChannelType().getId());
        }

        NodeRef channelNodeRef = channel.getNodeRef();
        NodeService nodeService = serviceRegistry.getNodeService();
        
        String[] username = callbackParams.get("username");
        String[] password = callbackParams.get("password");
        if (username != null && password != null)
        {
            nodeService.setProperty(channelNodeRef, PublishingModel.PROP_CHANNEL_USERNAME, username[0]);
            nodeService.setProperty(channelNodeRef, PublishingModel.PROP_CHANNEL_PASSWORD, password[0]);
            //TODO: BJR: 20110707: Should test the connection here
            result = true;
        }
        return result;
    }
}

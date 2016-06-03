/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.publishing.test;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.service.cmr.publishing.channels.ChannelService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Brian Remmington
 */
public class SiteType
{
    /** Log */
    private final static Log log = LogFactory.getLog(SiteType.class);

    private PolicyComponent policyComponent;
    private ChannelService channelService;

    /**
     * This is the list of collections that will be created automatically for
     * any new section.
     */
    /**
     * Set the policy component
     * 
     * @param policyComponent
     *            policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    public void setChannelService(ChannelService channelService)
    {
        this.channelService = channelService;
    }

    /**
     * Init method. Binds model behaviours to policies.
     */
    public void init()
    {
        // Register the association behaviours
        policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, SiteModel.TYPE_SITE,
                new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));
    }

    /**
     * On create node behaviour
     * 
     * @param childAssocRef
     *            child association reference
     */
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        if (log.isDebugEnabled())
        {
            log.debug("onCreateNode " + childAssocRef);
        }
        String siteId = childAssocRef.getQName().getLocalName();
        channelService.createChannel("TestChannelType1", "Test Channel One", null);
        channelService.createChannel("TestChannelType2", "Test Channel Two", null);
        channelService.createChannel("TestChannelType3", "Test Channel Three", null);
    }

}

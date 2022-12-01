/*-
 * #%L
 * alfresco-tas-restapi
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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
package org.alfresco.rest.model;

import java.util.List;

import org.alfresco.utility.constants.NetworkSubscriptionLevel;
import org.alfresco.utility.model.TestModel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Cristina Axinte on 9/26/2016.
 */
public class RestPersonNetworkModel extends TestModel
{
    @JsonProperty(required = true)
    private String id;

    private boolean homeNetwork;

    @JsonProperty(required = true)
    private boolean isEnabled;

    private String createdAt;
    private boolean paidNetwork;
    private NetworkSubscriptionLevel subscriptionLevel;

    private List<RestNetworkQuotaModel> quotas;

    public RestPersonNetworkModel()
    {
    }

    public RestPersonNetworkModel(String id, boolean isEnabled)
    {
        this.id = id;
        this.isEnabled = isEnabled;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public boolean isHomeNetwork()
    {
        return homeNetwork;
    }

    public void setHomeNetwork(boolean homeNetwork)
    {
        this.homeNetwork = homeNetwork;
    }

    public String getCreatedAt()
    {
        return createdAt;
    }

    public void setCreatedAt(String createdAt)
    {
        this.createdAt = createdAt;
    }

    public boolean isPaidNetwork()
    {
        return paidNetwork;
    }

    public void setPaidNetwork(boolean paidNetwork)
    {
        this.paidNetwork = paidNetwork;
    }

    public NetworkSubscriptionLevel getSubscriptionLevel()
    {
        return subscriptionLevel;
    }

    public void setSubscriptionLevel(NetworkSubscriptionLevel subscriptionLevel)
    {
        this.subscriptionLevel = subscriptionLevel;
    }

    public List<RestNetworkQuotaModel> getQuotas()
    {
        return quotas;
    }

    public void setQuotas(List<RestNetworkQuotaModel> quotas)
    {
        this.quotas = quotas;
    }

    public boolean isEnabled()
    {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled)
    {
        this.isEnabled = isEnabled;
    }  

}

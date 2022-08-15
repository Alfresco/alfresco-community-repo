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
/*
 * Copyright (C) 2005-2018 Alfresco Software Limited.
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
package org.alfresco.rest.search;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.alfresco.rest.core.IRestModel;
import org.alfresco.utility.model.TestModel;

/**
 * POJO representing shard info model
 *
 * @author Tuna Aksoy
 */
public class RestShardInfoModel extends TestModel implements IRestModel<RestShardInfoModel>
{
    /** Model */
    @JsonProperty(value = "entry")
    RestShardInfoModel model;

    /**
     * @see org.alfresco.rest.core.IRestModel#onModel()
     */
    @Override
    public RestShardInfoModel onModel()
    {
        return model;
    }

    /**
     * @return the model
     */
    public RestShardInfoModel getModel()
    {
        return this.model;
    }

    /**
     * @param model the model to set
     */
    public void setModel(RestShardInfoModel model)
    {
        this.model = model;
    }

    /** Template */
    private String template;

    /** Low instance shards */
    private String lowInstanceShards;

    /** Missing shards */
    private String missingShards;

    /** Max repository transaction Id */
    private Long maxRepositoryTransactionId;

    /** Max live instances */
    private Integer maxLiveInstances;

    /** Remaining Transactions */
    private Long remainingTransactions;

    /** Number of shards */
    private Integer numberOfShards;

    /** Min active instances */
    private Integer minActiveInstances;

    /** Max change set Id */
    private Long maxChangesetId;

    /** Mode */
    private String mode;

    /** Stores */
    private String stores;

    /** Has content */
    private Boolean hasContent;

    /** Shard method */
    private String shardMethod;

    /** Shards */
    private List<RestShardModel> shards;

    /**
     * @return the template
     */
    public String getTemplate()
    {
        return this.template;
    }

    /**
     * @param template the template to set
     */
    public void setTemplate(String template)
    {
        this.template = template;
    }

    /**
     * @return the lowInstanceShards
     */
    public String getLowInstanceShards()
    {
        return this.lowInstanceShards;
    }

    /**
     * @param lowInstanceShards the lowInstanceShards to set
     */
    public void setLowInstanceShards(String lowInstanceShards)
    {
        this.lowInstanceShards = lowInstanceShards;
    }

    /**
     * @return the missingShards
     */
    public String getMissingShards()
    {
        return this.missingShards;
    }

    /**
     * @param missingShards the missingShards to set
     */
    public void setMissingShards(String missingShards)
    {
        this.missingShards = missingShards;
    }

    /**
     * @return the maxRepositoryTransactionId
     */
    public Long getMaxRepositoryTransactionId()
    {
        return this.maxRepositoryTransactionId;
    }

    /**
     * @param maxRepositoryTransactionId the maxRepositoryTransactionId to set
     */
    public void setMaxRepositoryTransactionId(Long maxRepositoryTransactionId)
    {
        this.maxRepositoryTransactionId = maxRepositoryTransactionId;
    }

    /**
     * @return the maxLiveInstances
     */
    public Integer getMaxLiveInstances()
    {
        return this.maxLiveInstances;
    }

    /**
     * @param maxLiveInstances the maxLiveInstances to set
     */
    public void setMaxLiveInstances(Integer maxLiveInstances)
    {
        this.maxLiveInstances = maxLiveInstances;
    }

    /**
     * @return the remainingTransactions
     */
    public Long getRemainingTransactions()
    {
        return this.remainingTransactions;
    }

    /**
     * @param remainingTransactions the remainingTransactions to set
     */
    public void setRemainingTransactions(Long remainingTransactions)
    {
        this.remainingTransactions = remainingTransactions;
    }

    /**
     * @return the numberOfShards
     */
    public Integer getNumberOfShards()
    {
        return this.numberOfShards;
    }

    /**
     * @param numberOfShards the numberOfShards to set
     */
    public void setNumberOfShards(Integer numberOfShards)
    {
        this.numberOfShards = numberOfShards;
    }

    /**
     * @return the minActiveInstances
     */
    public Integer getMinActiveInstances()
    {
        return this.minActiveInstances;
    }

    /**
     * @param minActiveInstances the minActiveInstances to set
     */
    public void setMinActiveInstances(Integer minActiveInstances)
    {
        this.minActiveInstances = minActiveInstances;
    }

    /**
     * @return the maxChangesetId
     */
    public Long getMaxChangesetId()
    {
        return this.maxChangesetId;
    }

    /**
     * @param maxChangesetId the maxChangesetId to set
     */
    public void setMaxChangesetId(Long maxChangesetId)
    {
        this.maxChangesetId = maxChangesetId;
    }

    /**
     * @return the mode
     */
    public String getMode()
    {
        return this.mode;
    }

    /**
     * @param mode the mode to set
     */
    public void setMode(String mode)
    {
        this.mode = mode;
    }

    /**
     * @return the stores
     */
    public String getStores()
    {
        return this.stores;
    }

    /**
     * @param stores the stores to set
     */
    public void setStores(String stores)
    {
        this.stores = stores;
    }

    /**
     * @return the hasContent
     */
    public Boolean getHasContent()
    {
        return this.hasContent;
    }

    /**
     * @param hasContent the hasContent to set
     */
    public void setHasContent(Boolean hasContent)
    {
        this.hasContent = hasContent;
    }

    /**
     * @return the shardMethod
     */
    public String getShardMethod()
    {
        return this.shardMethod;
    }

    /**
     * @param shardMethod the shardMethod to set
     */
    public void setShardMethod(String shardMethod)
    {
        this.shardMethod = shardMethod;
    }

    /**
     * @return the shards
     */
    public List<RestShardModel> getShards()
    {
        return this.shards;
    }

    /**
     * @param shards the shards to set
     */
    public void setShards(List<RestShardModel> shards)
    {
        this.shards = shards;
    }
}

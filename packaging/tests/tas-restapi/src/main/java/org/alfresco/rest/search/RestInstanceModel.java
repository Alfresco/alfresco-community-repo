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

/**
 * Representation of a shard instance
 *
 * @author Tuna Aksoy
 */
public class RestInstanceModel
{
    /** Base Url */
    private String baseUrl;

    /** Host */
    private String host;

    /** Last indexed change set date */
    private String lastIndexedChangesetDate;

    /** Last indexed change set Id */
    private Long lastIndexedChangesetId;

    /** Last indexed transaction date */
    private String lastIndexedTransactionDate;

    /** Last indexed transaction Id */
    private Long lastIndexedTransactionId;

    /** Last update date */
    private String lastUpdateDate;

    /** Port */
    private Integer port;

    /** State */
    private String state;

    /** Mode */
    private String mode;

    /** Transactions remaining */
    private Long transactionsRemaining;
    
    /** Sharding Parameters */
    private String shardParams;

    /**
     * @return the baseUrl
     */
    public String getBaseUrl()
    {
        return this.baseUrl;
    }

    /**
     * @param baseUrl the baseUrl to set
     */
    public void setBaseUrl(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    /**
     * @return the host
     */
    public String getHost()
    {
        return this.host;
    }

    /**
     * @param host the host to set
     */
    public void setHost(String host)
    {
        this.host = host;
    }

    /**
     * @return the lastIndexedChangesetDate
     */
    public String getLastIndexedChangesetDate()
    {
        return this.lastIndexedChangesetDate;
    }

    /**
     * @param lastIndexedChangesetDate the lastIndexedChangesetDate to set
     */
    public void setLastIndexedChangesetDate(String lastIndexedChangesetDate)
    {
        this.lastIndexedChangesetDate = lastIndexedChangesetDate;
    }

    /**
     * @return the lastIndexedChangesetId
     */
    public Long getLastIndexedChangesetId()
    {
        return this.lastIndexedChangesetId;
    }

    /**
     * @param lastIndexedChangesetId the lastIndexedChangesetId to set
     */
    public void setLastIndexedChangesetId(Long lastIndexedChangesetId)
    {
        this.lastIndexedChangesetId = lastIndexedChangesetId;
    }

    /**
     * @return the lastIndexedTransactionDate
     */
    public String getLastIndexedTransactionDate()
    {
        return this.lastIndexedTransactionDate;
    }

    /**
     * @param lastIndexedTransactionDate the lastIndexedTransactionDate to set
     */
    public void setLastIndexedTransactionDate(String lastIndexedTransactionDate)
    {
        this.lastIndexedTransactionDate = lastIndexedTransactionDate;
    }

    /**
     * @return the lastIndexedTransactionId
     */
    public Long getLastIndexedTransactionId()
    {
        return this.lastIndexedTransactionId;
    }

    /**
     * @param lastIndexedTransactionId the lastIndexedTransactionId to set
     */
    public void setLastIndexedTransactionId(Long lastIndexedTransactionId)
    {
        this.lastIndexedTransactionId = lastIndexedTransactionId;
    }

    /**
     * @return the lastUpdateDate
     */
    public String getLastUpdateDate()
    {
        return this.lastUpdateDate;
    }

    /**
     * @param lastUpdateDate the lastUpdateDate to set
     */
    public void setLastUpdateDate(String lastUpdateDate)
    {
        this.lastUpdateDate = lastUpdateDate;
    }

    /**
     * @return the port
     */
    public Integer getPort()
    {
        return this.port;
    }

    /**
     * @param port the port to set
     */
    public void setPort(Integer port)
    {
        this.port = port;
    }

    /**
     * @return the state
     */
    public String getState()
    {
        return this.state;
    }

    /**
     * @param state the state to set
     */
    public void setState(String state)
    {
        this.state = state;
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
     * @return the transactionsRemaining
     */
    public Long getTransactionsRemaining()
    {
        return this.transactionsRemaining;
    }

    /**
     * @param transactionsRemaining the transactionsRemaining to set
     */
    public void setTransactionsRemaining(Long transactionsRemaining)
    {
        this.transactionsRemaining = transactionsRemaining;
    }
    
    /**
     * @return the shardParams
     */
    public String getShardParams()
    {
        return this.shardParams;
    }

    /**
     * @param shardParams the shardParams to set
     */
    public void setShardParams(String shardParams)
    {
        this.shardParams = shardParams;
    }

}

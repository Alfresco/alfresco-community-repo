/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2025 Alfresco Software Limited
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
package org.alfresco.repo.node.sizedetails;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ThreadPoolExecutor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.node.sizedetails.NodeSizeDetailsServiceImpl.NodeSizeDetails.STATUS;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchParameters.FieldFacet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;

/**
 * NodeSizeDetailsServiceImpl Executing Alfresco FTS Query to find size details of Folder Node
 */
public class NodeSizeDetailsServiceImpl implements NodeSizeDetailsService, InitializingBean
{
    private static final Logger LOG = LoggerFactory.getLogger(NodeSizeDetailsServiceImpl.class);
    private static final String FIELD_FACET = "content.size";
    private static final String FACET_QUERY = "{!afts}content.size:[0 TO " + Integer.MAX_VALUE + "]";
    private SearchService searchService;
    private SimpleCache<Serializable, NodeSizeDetails> simpleCache;
    private TransactionService transactionService;
    private ThreadPoolExecutor threadPoolExecutor;
    private int defaultItems;

    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    @Override
    public Optional<NodeSizeDetails> getSizeDetails(String id)
    {
        NodeSizeDetails details = simpleCache.get(id);
        return Optional.ofNullable(details)
                .or(() -> {
                    LOG.error("No Size details found for ID: " + id);
                    return Optional.empty();
                });
    }

    public void setSimpleCache(SimpleCache<Serializable, NodeSizeDetails> simpleCache)
    {
        this.simpleCache = simpleCache;
    }

    public void setTransactionService(TransactionService transactionService)
    {
        this.transactionService = transactionService;
    }

    public void setThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor)
    {
        this.threadPoolExecutor = threadPoolExecutor;
    }

    public void setDefaultItems(int defaultItems)
    {
        this.defaultItems = defaultItems;
    }

    @Override
    public void invokeSizeDetailsExecutor(NodeRef nodeRef, String jobId)
    {
        try
        {
            executeSizeCalculation(nodeRef, jobId);
        }
        catch (Exception e)
        {
            LOG.error("Exception occurred while executing invokeSizeDetailsExecutor method ", e);
        }

    }

    @Override
    public void putSizeDetails(String id, NodeSizeDetails nodeSizeDetails)
    {
        simpleCache.put(id, nodeSizeDetails);
    }

    private void executeSizeCalculation(NodeRef nodeRef, String jobId)
    {
        String authenticatedUserName = AuthenticationUtil.getFullyAuthenticatedUser();
        RetryingTransactionCallback<NodeSizeDetails> executionCallback = () -> {

            try
            {
                return calculateTotalSizeFromFacet(nodeRef, jobId);
            }
            catch (Exception ex)
            {
                LOG.error("Exception occurred in executeSizeCalculation:RetryingTransactionCallback ", ex);
                throw ex;
            }
        };

        threadPoolExecutor.execute(() -> {
            NodeSizeDetails nodeSizeDetails = new NodeSizeDetails(nodeRef.getId(), jobId, STATUS.IN_PROGRESS);
            putSizeDetails(nodeRef.getId(), nodeSizeDetails);

            try
            {
                nodeSizeDetails = AuthenticationUtil.runAs(() -> transactionService.getRetryingTransactionHelper()
                        .doInTransaction(executionCallback, true), authenticatedUserName);
            }
            catch (Exception e)
            {
                LOG.error("Exception occurred in executeSizeCalculation", e);
                nodeSizeDetails = new NodeSizeDetails(nodeRef.getId(), 0L, jobId, STATUS.FAILED);
            }
            finally
            {
                putSizeDetails(nodeRef.getId(), nodeSizeDetails);
            }
        });
    }

    private NodeSizeDetails calculateTotalSizeFromFacet(NodeRef nodeRef, String jobId)
    {
        long totalSizeFromFacet = 0;
        int skipCount = 0;
        int totalItems = defaultItems;
        boolean isCalculationCompleted = false;

        try
        {
            ResultSet results = facetQuery(nodeRef);
            int resultsSize = results.getFieldFacet(FIELD_FACET)
                    .size();

            while (!isCalculationCompleted)
            {
                List<Pair<String, Integer>> facetPairs = results.getFieldFacet(FIELD_FACET)
                        .subList(skipCount, Math.min(totalItems, resultsSize));
                totalSizeFromFacet += facetPairs.parallelStream()
                        .mapToLong(pair -> Long.parseLong(pair.getFirst()) * pair.getSecond())
                        .sum();

                if (resultsSize <= totalItems || resultsSize <= defaultItems)
                {
                    isCalculationCompleted = true;
                }
                else
                {
                    skipCount += defaultItems;
                    resultsSize -= totalItems;
                    totalItems += Math.min(resultsSize, defaultItems);
                }
            }
            Date calculationDate = new Date(System.currentTimeMillis());
            NodeSizeDetails nodeSizeDetails = new NodeSizeDetails(nodeRef.getId(), totalSizeFromFacet, calculationDate,
                    results.getNodeRefs()
                            .size(),
                    STATUS.COMPLETED, jobId);
            return nodeSizeDetails;
        }
        catch (Exception e)
        {
            LOG.error("Exception occurred while calculating total size from facet", e);
            throw e;
        }
    }

    private ResultSet facetQuery(NodeRef nodeRef)
    {
        try
        {
            SearchParameters searchParameters = createSearchParameters(nodeRef);
            ResultSet resultsWithoutFacet = searchService.query(searchParameters);
            if (LOG.isDebugEnabled())
            {
                LOG.debug(" After Executing facet query, no. of records found " + resultsWithoutFacet.getNumberFound());
            }
            searchParameters.addFacetQuery(FACET_QUERY);
            FieldFacet fieldFacet = new FieldFacet(FIELD_FACET);
            fieldFacet.setLimitOrNull((int) resultsWithoutFacet.getNumberFound());
            searchParameters.addFieldFacet(fieldFacet);
            resultsWithoutFacet.close();
            return searchService.query(searchParameters);
        }
        catch (Exception e)
        {
            LOG.error("Exception occurred while executing facetQuery ", e);
            throw e;
        }
    }

    private SearchParameters createSearchParameters(NodeRef nodeRef)
    {
        SearchParameters searchParameters = new SearchParameters();
        searchParameters.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);
        searchParameters.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
        searchParameters.setQuery("ANCESTOR:\"" + nodeRef + "\" AND TYPE:\"cm:content\"");
        searchParameters.setTrackTotalHits(-1);
        return searchParameters;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        ParameterCheck.mandatory("searchService", this.searchService);
        ParameterCheck.mandatory("simpleCache", this.simpleCache);
        ParameterCheck.mandatory("transactionService", this.transactionService);
        ParameterCheck.mandatory("threadPoolExecutor", this.threadPoolExecutor);
    }

    /**
     * POJO class to hold node size details.
     */
    public static class NodeSizeDetails implements Serializable
    {
        private static final long serialVersionUID = 1L;
        private String id;
        private Long sizeInBytes;
        private Date calculatedAt;
        private Integer numberOfFiles;
        private String jobId;
        private STATUS status;

        public NodeSizeDetails()
        {}

        public NodeSizeDetails(String jobId)
        {
            this.jobId = jobId;
        }

        public NodeSizeDetails(String id, STATUS status)
        {
            this.id = id;
            this.status = status;
        }

        public NodeSizeDetails(String id, String jobId, STATUS status)
        {
            this.id = id;
            this.jobId = jobId;
            this.status = status;
        }

        public NodeSizeDetails(String id, Long sizeInBytes, String jobId, STATUS status)
        {
            this.id = id;
            this.sizeInBytes = sizeInBytes;
            this.jobId = jobId;
            this.status = status;
        }

        public NodeSizeDetails(String id, Long sizeInBytes, Date calculatedAt, Integer numberOfFiles,
                STATUS currentStatus, String jobId)
        {
            this.id = id;
            this.sizeInBytes = sizeInBytes;
            this.calculatedAt = calculatedAt;
            this.numberOfFiles = numberOfFiles;
            this.status = currentStatus;
            this.jobId = jobId;
        }

        public String getId()
        {
            return id;
        }

        public void setId(String id)
        {
            this.id = id;
        }

        public Long getSizeInBytes()
        {
            return sizeInBytes;
        }

        public void setSizeInBytes(Long sizeInBytes)
        {
            this.sizeInBytes = sizeInBytes;
        }

        public Date getCalculatedAt()
        {
            return calculatedAt;
        }

        public void setCalculatedAt(Date calculatedAt)
        {
            this.calculatedAt = calculatedAt;
        }

        public Integer getNumberOfFiles()
        {
            return numberOfFiles;
        }

        public void setNumberOfFiles(Integer numberOfFiles)
        {
            this.numberOfFiles = numberOfFiles;
        }

        public String getJobId()
        {
            return jobId;
        }

        public void setJobId(String jobId)
        {
            this.jobId = jobId;
        }

        public STATUS getStatus()
        {
            return status;
        }

        public void setStatus(STATUS status)
        {
            this.status = status;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }
            NodeSizeDetails that = (NodeSizeDetails) o;
            return Objects.equals(id, that.id) && Objects.equals(sizeInBytes, that.sizeInBytes) && Objects.equals(
                    calculatedAt, that.calculatedAt) && Objects.equals(numberOfFiles, that.numberOfFiles)
                    && Objects.equals(jobId, that.jobId) && status == that.status;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(id, sizeInBytes, calculatedAt, numberOfFiles, jobId, status);
        }

        @Override
        public String toString()
        {
            return "NodeSizeDetails{" + "id='" + id + '\'' + ", sizeInBytes=" + sizeInBytes + ", calculatedAt="
                    + calculatedAt + ", numberOfFiles=" + numberOfFiles + ", jobId='" + jobId + '\'' + ", status="
                    + status + '}';
        }

        public enum STATUS
        {
            NOT_INITIATED, PENDING, IN_PROGRESS, COMPLETED, FAILED
        }

    }

}
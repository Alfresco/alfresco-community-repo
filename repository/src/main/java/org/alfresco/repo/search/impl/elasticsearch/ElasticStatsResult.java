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
package org.alfresco.repo.search.impl.elasticsearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.StatsResultSet;
import org.alfresco.service.cmr.search.StatsResultStat;

public class ElasticStatsResult implements StatsResultSet
{

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticStatsResult.class);

    private Long numberFound;

    // Summary stats
    private Long sum;
    private Long max;
    private Long mean;

    private List<StatsResultStat> stats;

    public ElasticStatsResult()
    {}

    public ElasticStatsResult(ResultSet result, NodeService nodeService)
    {
        this.stats = new ArrayList<>();
        this.sum = 0L;
        this.max = Long.MIN_VALUE;
        this.mean = 0L;
        this.numberFound = result.getNumberFound();
        processResult(result, nodeService);
    }

    private void processResult(ResultSet result, NodeService nodeService)
    {
        Map<String, List<Long>> mimeSizeMap = new HashMap<>();

        for (ResultSetRow row : result)
        {
            NodeRef nodeRef = row.getNodeRef();
            if (!nodeService.exists(nodeRef))
            {
                continue;
            }

            ContentData contentData = (ContentData) nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
            if (contentData == null)
            {
                continue;
            }

            String mimeType = contentData.getMimetype();
            long size = contentData.getSize();

            mimeSizeMap.computeIfAbsent(mimeType, k -> new ArrayList<>()).add(size);
        }
        if (mimeSizeMap.isEmpty())
        {
            LOGGER.warn("No mimetypes found");
        }

        long maxOverall = Long.MIN_VALUE;
        for (Map.Entry<String, List<Long>> entry : mimeSizeMap.entrySet())
        {
            String mimeType = entry.getKey();
            List<Long> sizes = entry.getValue();

            long count = sizes.size();
            long sum = sizes.stream().mapToLong(Long::longValue).sum();
            long min = sizes.stream().mapToLong(Long::longValue).min().orElse(0L);
            long max = sizes.stream().mapToLong(Long::longValue).max().orElse(0L);
            long mean = count > 0 ? sum / count : 0L;

            stats.add(new StatsResultStat(mimeType, sum, count, min, max, mean));

            this.sum += sum;
            if (this.max > maxOverall)
            {
                maxOverall = this.max;
            }
        }
    }

    @Override
    public long getNumberFound()
    {
        return this.numberFound;
    }

    @Override
    public Long getSum()
    {
        return this.sum;
    }

    @Override
    public Long getMax()
    {
        return this.max;
    }

    @Override
    public Long getMean()
    {
        return this.mean;
    }

    @Override
    public List<StatsResultStat> getStats()
    {
        return this.stats;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ElasticStatsResult [").append(", numberFound=").append(this.numberFound)
                .append(", sum=").append(this.sum).append(", max=").append(this.max)
                .append(", mean=").append(this.mean).append(", stats=").append(this.stats)
                .append("]");
        return builder.toString();
    }
}

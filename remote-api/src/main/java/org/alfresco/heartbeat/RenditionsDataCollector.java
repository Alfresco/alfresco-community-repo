/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2018 Alfresco Software Limited
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
package org.alfresco.heartbeat;

import org.alfresco.heartbeat.datasender.HBData;
import org.alfresco.heartbeat.jobs.HeartBeatJobScheduler;
import org.alfresco.repo.descriptor.DescriptorDAO;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class collects rendition request counts for HeartBeat. A rendition (such as "doclib") is always to the same
 * target mimetype, but there may be different source mimetypes. As a result that may be multiple sets of data with
 * the same rendition. It is also likely there will be multiple renditions reported in the same batch of data.
 * <ul>
 *  <li>Collector ID: <b>acs.repository.renditions</b></li>
 *  <li>Data:
 *      <ul>
 *          <li><b>rendition:</b> String - The name of the rendition.</li>
 *          <li><b>count:</b> Integer - The number of times a rendition and sourceMimetype combination has been requested.</li>
 *          <li><b>sourceMimetype:</b> String - The source mimetype for the rendition.</li>
 *          <li><b>targetMimetype:</b> String - The target mimetype for the rendition.</li>
 *      </ul>
 *  </li>
 * </ul>
 *
 * @author adavis
 */
public class RenditionsDataCollector extends HBBaseDataCollector implements InitializingBean
{
    private static final Log logger = LogFactory.getLog(RenditionsDataCollector.class);

    private DescriptorDAO currentRepoDescriptorDAO;

    // Map keyed on rendition id to a Map keyed on source mimetypes to a count of the number of times it has been requested.
    private final Map<ThumbnailDefinition, Map<String, AtomicInteger>> renditionRequests = new ConcurrentHashMap<>();

    public RenditionsDataCollector(String collectorId, String collectorVersion, String cronExpression,
                                    HeartBeatJobScheduler hbJobScheduler)
    {
        super(collectorId, collectorVersion, cronExpression, hbJobScheduler);
    }

    public void setCurrentRepoDescriptorDAO(DescriptorDAO currentRepoDescriptorDAO)
    {
        this.currentRepoDescriptorDAO = currentRepoDescriptorDAO;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        PropertyCheck.mandatory(this, "currentRepoDescriptorDAO", currentRepoDescriptorDAO);
    }

    public void recordRenditionRequest(ThumbnailDefinition rendition, String sourceMimetype)
    {
        // Increment the count of renditions. Atomically creates missing parts of the Map structures.
        renditionRequests.computeIfAbsent(rendition,
                k -> new ConcurrentHashMap<>()).computeIfAbsent(sourceMimetype,
                k -> new AtomicInteger()).incrementAndGet();
    }

    @Override
    public List<HBData> collectData()
    {
        List<HBData> collectedData = new LinkedList<>();

        String systemId = this.currentRepoDescriptorDAO.getDescriptor().getId();
        String collectorId = this.getCollectorId();
        String collectorVersion = this.getCollectorVersion();
        Date timestamp = new Date();

        // We don't mind if new renditions are added while we iterate, as we will pick them up next time.
        for (ThumbnailDefinition rendition : renditionRequests.keySet())
        {
            String renditionName = rendition.getName();
            String targetMimetype = rendition.getMimetype();
            for (Map.Entry<String, AtomicInteger> entry: renditionRequests.remove(rendition).entrySet())
            {
                String sourceMimetype = entry.getKey();
                AtomicInteger count = entry.getValue();

                Map<String, Object> values = new HashMap<>();
                values.put("rendition", renditionName);
                values.put("count", count.intValue());
                values.put("sourceMimetype", sourceMimetype);
                values.put("targetMimetype", targetMimetype);

                // Decided it would be simpler to be able to combine results in Kibana from different nodes
                // and days if the data was flattened (denormalized) out at this point. It is very likely
                // that different nodes would have different sets of sourceMimetypes which would make summing
                // the counts harder to do, if there was a single entry for each rendition with a nested
                // structure for each sourceMimetype.
                collectedData.add(new HBData(systemId, collectorId, collectorVersion, timestamp, values));

                if (logger.isDebugEnabled())
                {
                    logger.debug(renditionName+" "+count+" "+sourceMimetype+" "+targetMimetype);
                }
            }
        }

        return collectedData;
    }
}

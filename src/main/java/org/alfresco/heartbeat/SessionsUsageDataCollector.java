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
import org.alfresco.repo.admin.RepoServerMgmtMBean;
import org.alfresco.repo.descriptor.DescriptorDAO;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Date;



/**
 * <ul>
 *  <li>Collector ID: <b>acs.repository.usage.sessions</b></li>
 *  <li>Data:
 *      <ul>
 *          <li><b>activeTickets:</b> int - The number of non-expired tickets. {@link RepoServerMgmtMBean#getTicketCountNonExpired()}</li>
 *      </ul>
 *  </li>
 * </ul>
 *
 * @author eknizat
 */
public class SessionsUsageDataCollector extends HBBaseDataCollector
{
    /** The logger. */
    private static final Log logger = LogFactory.getLog(SessionsUsageDataCollector.class);

    /** DAO for current repository descriptor. */
    private DescriptorDAO currentRepoDescriptorDAO;

    private RepoServerMgmtMBean repoServerMgmt;

    public SessionsUsageDataCollector(String collectorId, String collectorVersion, String cronExpression, HeartBeatJobScheduler hbJobScheduler)
    {
        super(collectorId, collectorVersion, cronExpression, hbJobScheduler);
    }

    public void setCurrentRepoDescriptorDAO(DescriptorDAO currentRepoDescriptorDAO)
    {
        this.currentRepoDescriptorDAO = currentRepoDescriptorDAO;
    }

    public void setRepoServerMgmt(RepoServerMgmtMBean repoServerMgmt)
    {
        this.repoServerMgmt = repoServerMgmt;
    }

    @Override
    public List<HBData> collectData()
    {
        Map<String, Object> sessionValues = new HashMap<>();

        sessionValues.put("activeTickets", repoServerMgmt.getTicketCountNonExpired());

        HBData sessionsData = new HBData(
                this.currentRepoDescriptorDAO.getDescriptor().getId(),
                this.getCollectorId(),
                this.getCollectorVersion(),
                new Date(),
                sessionValues);

        return Arrays.asList(sessionsData);
    }
}

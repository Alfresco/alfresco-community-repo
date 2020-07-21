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

import org.alfresco.heartbeat.datasender.HBDataSenderServiceBuilder;
import org.alfresco.heartbeat.datasender.HBDataSenderService;

/**
 * Creates and configures a {@link HBDataSenderService}
 *
 * @author Ancuta Morarasu
 *
 */
public class HBDataSenderServiceFactory
{
    private String target;
    private boolean heartbeatEnabled;
    private String sendingSchedule;

    /**
     * Sets the ingestion url for the heartbeat data
     *
     * @param target valid url
     */
    public void setTarget(String target)
    {
        this.target = target;
    }

    /**
     * Enables or stops gathering and sending heartbeat data
     *
     * @param heartbeatEnabled
     */
    public void setHeartbeatEnabled(boolean heartbeatEnabled)
    {
        this.heartbeatEnabled = heartbeatEnabled;
    }

    /**
     * Sets the cron trigger expression for scheduling the heartbeat job.
     *
     * @param sendingSchedule
     */
    public void setSendingSchedule(String sendingSchedule)
    {
        this.sendingSchedule = sendingSchedule;
    }

    /**
     * Creates the HBDataSenderService
     *
     * @return
     */
    public HBDataSenderService createInstance() 
    {
        return HBDataSenderServiceBuilder.builder()
                  .withHeartbeatURL(target)
                  .withSendingSchedule(sendingSchedule)
                  .enable(heartbeatEnabled)
                  .build();
    }
}

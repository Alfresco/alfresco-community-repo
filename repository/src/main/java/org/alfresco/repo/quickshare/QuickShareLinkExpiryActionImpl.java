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

package org.alfresco.repo.quickshare;

import org.alfresco.repo.action.ActionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.scheduled.ScheduledPersistedAction;
import org.alfresco.service.cmr.quickshare.QuickShareLinkExpiryAction;
import org.alfresco.service.namespace.QName;

import java.io.Serializable;
import java.util.Date;

/**
 * Quick share link expiry action implementation class.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class QuickShareLinkExpiryActionImpl extends ActionImpl implements QuickShareLinkExpiryAction
{

    public static final String EXECUTOR_NAME = "quickShareLinkExpiryActionExecutor";
    public static final String QUICK_SHARE_LINK_EXPIRY_ACTION_NAME = "quickShareLinkExpiryActionName";

    private static final long serialVersionUID = 2497810872555230797L;

    private ScheduledPersistedAction schedule;

    /**
     * @param id          the action id
     * @param sharedId    a unique name for the quick share link expiry action.
     * @param description the action description
     */
    public QuickShareLinkExpiryActionImpl(String id, String sharedId, String description)
    {
        super(null, id, EXECUTOR_NAME);
        setActionQName(createQName(sharedId));
        setDescription(description);
    }

    public QuickShareLinkExpiryActionImpl(Action action)
    {
        super(action);
    }

    protected void setActionQName(QName actionQName)
    {
        setParameterValue(QUICK_SHARE_LINK_EXPIRY_ACTION_NAME, actionQName);
    }

    @Override
    public QName getActionQName()
    {
        Serializable parameterValue = getParameterValue(QUICK_SHARE_LINK_EXPIRY_ACTION_NAME);
        return (QName) parameterValue;
    }

    @Override
    public ScheduledPersistedAction getSchedule()
    {
        return this.schedule;
    }

    @Override
    public void setSchedule(ScheduledPersistedAction schedule)
    {
        this.schedule = schedule;
    }

    @Override
    public String getSharedId()
    {
        // As we use the sharedId to generate the action's QName.
        QName qName = getActionQName();
        if (qName != null)
        {
            return qName.getLocalName();
        }
        return null;
    }

    @Override
    public Date getScheduleStart()
    {
        if (schedule == null)
            return null;
        return schedule.getScheduleStart();
    }

    @Override
    public void setScheduleStart(Date startDate)
    {
        if (schedule == null)
        {
            throw new IllegalStateException("Scheduling is not enabled.");
        }
        schedule.setScheduleStart(startDate);
    }

    @Override
    public Integer getScheduleIntervalCount()
    {
        if (schedule == null)
        {
            return null;
        }
        return schedule.getScheduleIntervalCount();
    }

    @Override
    public void setScheduleIntervalCount(Integer count)
    {
        if (schedule == null)
        {
            throw new IllegalStateException("Scheduling is not enabled.");
        }
        schedule.setScheduleIntervalCount(count);
    }

    @Override
    public IntervalPeriod getScheduleIntervalPeriod()
    {
        if (schedule == null)
        {
            return null;
        }
        return schedule.getScheduleIntervalPeriod();
    }

    @Override
    public void setScheduleIntervalPeriod(IntervalPeriod period)
    {
        if (schedule == null)
            throw new IllegalStateException("Scheduling is not enabled.");
        schedule.setScheduleIntervalPeriod(period);
    }

    public static QName createQName(String sharedId)
    {
        return QName.createQName(null, sharedId);
    }
}

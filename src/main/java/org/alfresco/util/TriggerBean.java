/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.util;

import java.util.Date;

import org.quartz.Scheduler;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;

public class TriggerBean extends AbstractTriggerBean implements TriggerBeanSPI
{
    public long startDelay = 0;

    public long repeatInterval = 0;

    public int repeatCount = SimpleTrigger.REPEAT_INDEFINITELY;

    public TriggerBean()
    {
        super();
    }

    @Override
    public int getRepeatCount()
    {
        return repeatCount;
    }

    @Override
    public void setRepeatCount(int repeatCount)
    {
        this.repeatCount = repeatCount;
    }

    @Override
    public long getRepeatInterval()
    {
        return repeatInterval;
    }

    @Override
    public void setRepeatInterval(long repeatInterval)
    {
        this.repeatInterval = repeatInterval;
    }

    @Override
    public void setRepeatIntervalMinutes(long repeatIntervalMinutes)
    {
        this.repeatInterval = repeatIntervalMinutes * 60L * 1000L;
    }

    @Override
    public long getStartDelay()
    {
        return startDelay;
    }

    @Override
    public void setStartDelay(long startDelay)
    {
        this.startDelay = startDelay;
    }

    @Override
    public void setStartDelayMinutes(long startDelayMinutes)
    {
        this.startDelay = startDelayMinutes * 60L * 1000L;
    }

    @Override
    public Trigger getTrigger() throws Exception
    {
        if ((repeatInterval <= 0) && (repeatCount != 0))
        {
           logger.error("Job "+getBeanName()+" - repeatInterval/repeatIntervalMinutes cannot be 0 (or -ve) unless repeatCount is also 0");
           return null;
        }
        
        SimpleTrigger trigger = new SimpleTrigger(getBeanName(), Scheduler.DEFAULT_GROUP);
        trigger.setStartTime(new Date(System.currentTimeMillis() + this.startDelay));
        trigger.setRepeatCount(repeatCount);
        trigger.setRepeatInterval(repeatInterval);
        return trigger;
    }
}

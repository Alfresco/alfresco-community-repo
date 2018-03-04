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
package org.alfresco.schedule;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.quartz.SchedulerAccessorBean;

/**
 * The class is designed to add <code>enabled</code> check to switch on/off the triggers scheduling.
 * The default is <code>true</code>.
 *
 * @since 6.0
 *
 * @author amukha
 */
public class AlfrescoSchedulerAccessorBean extends SchedulerAccessorBean implements DisposableBean
{
    @Nullable
    private List<TriggerKey> triggerKeys;
    private boolean enabled = true;

    public boolean isEnabled()
    {
        return enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    @Override
    public void setTriggers(Trigger... triggers)
    {
        super.setTriggers(triggers);
        this.triggerKeys = Arrays.stream(triggers).map(Trigger::getKey).collect(Collectors.toList());
    }

    @Override
    public void afterPropertiesSet() throws SchedulerException
    {
        if (isEnabled())
        {
            super.afterPropertiesSet();
        }
    }

    @Override
    public void destroy() throws Exception
    {
        getScheduler().unscheduleJobs(triggerKeys);
    }
}

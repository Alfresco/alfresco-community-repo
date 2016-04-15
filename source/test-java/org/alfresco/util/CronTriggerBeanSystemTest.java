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

package org.alfresco.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * This class tests the CronTriggerBean within the full Alfresco context.
 * This test runs in about 45 seconds.
 * 
 * @author Ahmed Owian
 */
public class CronTriggerBeanSystemTest
{
    private ClassPathXmlApplicationContext context;

    @Before
    public void setUp() throws Exception
    {
        this.context = (ClassPathXmlApplicationContext) ApplicationContextHelper.getApplicationContext();
    }

    /**
     * All CronTriggerBean classes should be configured with a delay 
     * to allow the server to start before the jobs run.
     */
    @Test
    public void testAllCronTriggerBeansHaveDelay()
    {
        Map<String, CronTriggerBean> beans = this.context
                    .getBeansOfType(org.alfresco.util.CronTriggerBean.class);
        assertFalse(beans.isEmpty());
        List<String> undelayedJobs = new ArrayList<>();

        for (Map.Entry<String, CronTriggerBean> entry : beans.entrySet())
        {
            CronTriggerBean bean = entry.getValue();
            if (bean.getStartDelay() == 0)
            {
                undelayedJobs.add(entry.getKey());
            }
        }

        assertTrue("Undelayed CronTriggerBeans: " + undelayedJobs, undelayedJobs.isEmpty());
    }
}

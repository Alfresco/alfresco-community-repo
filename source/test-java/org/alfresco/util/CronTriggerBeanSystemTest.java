
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

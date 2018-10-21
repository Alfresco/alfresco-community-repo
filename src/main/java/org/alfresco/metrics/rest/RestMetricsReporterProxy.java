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
package org.alfresco.metrics.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class RestMetricsReporterProxy implements RestMetricsReporter, ApplicationContextAware, InitializingBean
{
    private Log logger = LogFactory.getLog(getClass());
    private ApplicationContext applicationContext;
    private RestMetricsReporter restMetricsReporterImpl;

    @Override
    public void reportRestRequestExecutionTime(long milliseconds, String queryTpe, String statementID)
    {
        if (restMetricsReporterImpl != null)
        {
            restMetricsReporterImpl.reportRestRequestExecutionTime(milliseconds, queryTpe, statementID);
        }
    }

    @Override
    public boolean isEnabled()
    {
        if (restMetricsReporterImpl != null)
        {
            return restMetricsReporterImpl.isEnabled();
        }
        return false;
    }

    @Override
    public boolean isRestServicePathMetricsEnabled()
    {
        if (restMetricsReporterImpl != null)
        {
            return restMetricsReporterImpl.isRestServicePathMetricsEnabled();
        }
        return false;
    }

    @Override
    public void afterPropertiesSet() throws Exception
    {
        init();
    }

    private void init()
    {
        try
        {
            restMetricsReporterImpl = (RestMetricsReporter) applicationContext.getBean("restMetricsReporterImpl");
        }
        catch (Exception e)
        {
            // we expect that we will not have this bean in the community runtime
            // so don't report this problem
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;
    }
}

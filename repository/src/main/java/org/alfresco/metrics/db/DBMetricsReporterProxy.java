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
package org.alfresco.metrics.db;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class DBMetricsReporterProxy implements DBMetricsReporter, ApplicationContextAware, InitializingBean
{
    private Log logger = LogFactory.getLog(getClass());
    private ApplicationContext applicationContext;
    private DBMetricsReporter dbMetricsReporterImpl;

    @Override
    public void reportQueryExecutionTime(long milliseconds, String queryTpe, String statementID)
    {
        if (dbMetricsReporterImpl != null)
        {
            dbMetricsReporterImpl.reportQueryExecutionTime(milliseconds, queryTpe, statementID);
        }
    }

    @Override
    public boolean isEnabled()
    {
        if (dbMetricsReporterImpl != null)
        {
            return dbMetricsReporterImpl.isEnabled();
        }
        return false;
    }

    @Override
    public boolean isQueryMetricsEnabled()
    {
        if (dbMetricsReporterImpl != null)
        {
            return dbMetricsReporterImpl.isQueryMetricsEnabled();
        }
        return false;
    }

    @Override
    public boolean isQueryStatementsMetricsEnabled()
    {
        if (dbMetricsReporterImpl != null)
        {
            return dbMetricsReporterImpl.isQueryStatementsMetricsEnabled();
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
            dbMetricsReporterImpl = (DBMetricsReporter) applicationContext.getBean("dbMetricsReporterImpl");
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

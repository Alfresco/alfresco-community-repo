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
package org.alfresco.repo.metrics.db;

import io.micrometer.core.instrument.Tag;
import org.alfresco.micrometer.MetricsController;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DBMetricsReporterImpl implements DBMetricsReporter, InitializingBean
{
    public static final String QUERIES_EXECUTION_TIME = "queries.execution.time";
    public static final int MAX_TAG_LENGTH = 1024;

    private Log logger = LogFactory.getLog(getClass());

    private boolean enabled;
    private boolean queryMetricsEnabled;
    private boolean queryStatementsMetricsEnabled;

    private DataSource dataSource;

    MetricsController metricsController;

    @Override
    public void afterPropertiesSet() throws Exception
    {
        // metricsController should never be null
        // metricsController.getRegistry() can be null, and probably should be null if metricsController.isEnabled() is false
        PropertyCheck.mandatory(this, "metricsController", metricsController);
        try
        {
            init();
        }
        catch (Exception e)
        {
            if (logger.isWarnEnabled())
            {
                logger.warn("Could not initialize DB metrics reporter: " + e.getMessage(), e);
            }
        }
    }

    private void init()
    {
        if (!isEnabled())
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("DB Metrics reporting is not enabled");
            }
            return;
        }

        if (isEnabled() && metricsController.getRegistry() == null)
        {
            logger.error(
                "There is no meterRegistry object defined in the metricsController. That is essential for reporting DB metrics.");
            return;
        }

        initConnectionMetrics();
    }

    private void initConnectionMetrics()
    {
        if (metricsController.getRegistry() != null)
        {
            metricsController.getRegistry()
                .gauge("num.connections.active", Collections.emptyList(), dataSource, ConnectionGaugeDataProvider::getNumActive);
            metricsController.getRegistry()
                .gauge("num.connections.idle", Collections.emptyList(), dataSource, ConnectionGaugeDataProvider::getNumIdle);
        }
    }

    @Override
    public void reportQueryExecutionTime(final long milliseconds, final String queryTpe, final String statementID)
    {
        try
        {
            if (isQueryMetricsEnabled() && metricsController.getRegistry() != null && !isEmpty(queryTpe) && milliseconds >= 0)
            {
                List<Tag> tags = buildTagsForQueryExecution(queryTpe, statementID);
                metricsController.getRegistry().timer(QUERIES_EXECUTION_TIME, tags).record(milliseconds, TimeUnit.MILLISECONDS);
            }
        }
        catch (Exception e)
        {
            logMetricReportingProblem(e);
        }
    }

    private List<Tag> buildTagsForQueryExecution(final String queryType, final String statementID)
    {
        // we know that queryType is not empty at this point, but just for safe measure, sanitize it
        String sanitizedQueryType = sanitizeTagValue(queryType);
        List<Tag> tags = new ArrayList<>();
        tags.add(Tag.of("queryType", sanitizedQueryType));

        if (isQueryStatementsMetricsEnabled() && !isEmpty(statementID))
        {
            //just to be sure, sanitize the string
            String sanitizedStatementID = sanitizeTagValue(statementID);
            tags.add(Tag.of("statementID", sanitizedStatementID));
        }
        return tags;
    }

    private String sanitizeTagValue(final String tagValue)
    {
        //we always assume parameter is not null
        String str = tagValue.trim();
        if (str.length() > MAX_TAG_LENGTH)
        {
            return str.substring(0, MAX_TAG_LENGTH - 1);
        }
        return str;
    }

    @Override
    public boolean isEnabled()
    {
        return metricsController.isEnabled() && enabled;
    }

    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    @Override
    public boolean isQueryMetricsEnabled()
    {
        return isEnabled() && queryMetricsEnabled;
    }

    public void setQueryMetricsEnabled(boolean queryMetricsEnabled)
    {
        this.queryMetricsEnabled = queryMetricsEnabled;
    }

    @Override
    public boolean isQueryStatementsMetricsEnabled()
    {
        return isQueryMetricsEnabled() && queryStatementsMetricsEnabled;
    }

    public void setQueryStatementsMetricsEnabled(boolean queryStatementsMetricsEnabled)
    {
        this.queryStatementsMetricsEnabled = queryStatementsMetricsEnabled;
    }

    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public void setMetricsController(MetricsController metricsController)
    {
        this.metricsController = metricsController;
    }

    private void logMetricReportingProblem(Exception e)
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Could not report metric: " + e.getMessage(), e);
        }
    }

    private boolean isEmpty(String tag)
    {
        return tag == null || tag.isEmpty();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString());

        sb.append(" DB Metrics Reporting is enabled:");
        sb.append(isEnabled());

        sb.append(". Query metrics enabled: ");
        sb.append(isQueryMetricsEnabled());

        sb.append(". Query statement metrics enabled: ");
        sb.append(isQueryStatementsMetricsEnabled());
        return sb.toString();
    }
}

class ConnectionGaugeDataProvider
{
    private static Log logger = LogFactory.getLog(ConnectionGaugeDataProvider.class);

    public static double getNumActive(DataSource dataSource)
    {
        try
        {
            return ((BasicDataSource) dataSource).getNumActive();
        }
        catch (Exception e)
        {
            reportException(e);
        }
        return 0;
    }

    public static double getNumIdle(DataSource dataSource)
    {
        try
        {
            return ((BasicDataSource) dataSource).getNumIdle();
        }
        catch (Exception e)
        {
            reportException(e);
        }
        return 0;
    }

    private static void reportException(Exception e)
    {
        if (logger.isWarnEnabled())
        {
            logger.warn("Exception in getting the DB connection pool data: " + e.getMessage(), e);
        }
    }

}

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

import org.alfresco.metrics.MetricsReporter;

public interface DBMetricsReporter extends MetricsReporter
{
    /**
     * Report the time it took to execute a query.
     * queryType and statementID will be used as tags for the recorded metric
     *
     * @param milliseconds the delta time to record in milliseconds  - must be positive
     * @param queryTpe     mandatory, the type of query that we report for: e.g: "select", "insert", "update", "delete"
     * @param statementID  optional. if this parameter is not provided, a metric without it will be recorded;
     *                     this parameter is used only if "isQueryStatementsMetricsEnabled()" is true
     */
    void reportQueryExecutionTime(final long milliseconds, final String queryTpe, final String statementID);

    boolean isEnabled();

    boolean isQueryMetricsEnabled();

    boolean isQueryStatementsMetricsEnabled();
}

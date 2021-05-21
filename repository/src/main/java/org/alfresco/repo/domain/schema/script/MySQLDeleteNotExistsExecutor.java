/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.repo.domain.schema.script;

import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Extends <code>{@link DeleteNotExistsExecutor}</code> to cope with MySQL
 * specific fetch size limitation and restrictions.
 */
public class MySQLDeleteNotExistsExecutor extends DeleteNotExistsExecutor
{
    private static final Log logger = LogFactory.getLog(MySQLDeleteNotExistsExecutor.class);
    
    private final DataSource dataSource;

    public MySQLDeleteNotExistsExecutor(Connection connection, String sql, int line, File scriptFile, Properties globalProperties, DataSource dataSource)
    {
        super(connection, sql, line, scriptFile, globalProperties);
        this.dataSource = dataSource;
    }

    @Override
    protected void process(Pair<String, String>[] tableColumn, Long[] tableUpperLimits, String[] optionalWhereClauses) throws SQLException
    {
        // The approach is to fetch ordered row ids from all referencer/secondary (e.g.
        // alf_audit_app, alf_audit_entry, alf_prop_unique_ctx) tables and
        // referenced/primary table (e.g. alf_prop_root) concurrently, so that it is
        // possible skip over id gaps efficiently while at the same time being able to
        // work out which ids are obsolete and delete them in batches.

        // The algorithm can be further improved by iterating over the rows in descending order.
        // This is due to the fact that older data should be more stable in time.

        String primaryTableName = tableColumn[0].getFirst();
        String primaryColumnName = tableColumn[0].getSecond();
        String primaryWhereClause = optionalWhereClauses[0];

        Long primaryId = 0L;

        // There are some caveats with MySQL specific fetch size limitation. You must
        // read all of the rows in the result set (or close it) before you can issue any
        // other queries on the connection, or an exception will be thrown.
        Connection primaryConnection = null;
        Connection[] secondaryConnections = null;
        PreparedStatement primaryPrepStmt = null;
        PreparedStatement[] secondaryPrepStmts = null;
        PreparedStatement deletePrepStmt = null;
        Set<Long> deleteIds = new HashSet<>();

        deletedCount = 0L;
        startTime = new Date();
        try
        {
            connection.setAutoCommit(false);

            primaryConnection = dataSource.getConnection();
            
            primaryPrepStmt = primaryConnection.prepareStatement(createPreparedSelectStatement(primaryTableName, primaryColumnName, primaryWhereClause));
            // Note the MySQL specific fetch size limitation (Integer.MIN_VALUE). fetchSize
            // activates result set streaming.
            primaryPrepStmt.setFetchSize(Integer.MIN_VALUE);
            primaryPrepStmt.setLong(1, primaryId);
            primaryPrepStmt.setLong(2, tableUpperLimits[0]);

            boolean hasResults = primaryPrepStmt.execute();

            if (hasResults)
            {

                secondaryPrepStmts = new PreparedStatement[tableColumn.length];
                secondaryConnections = new Connection[tableColumn.length];
                for (int i = 1; i < tableColumn.length; i++)
                {
                    secondaryConnections[i] = dataSource.getConnection();
                    PreparedStatement secStmt = secondaryConnections[i].prepareStatement(createPreparedSelectStatement(tableColumn[i].getFirst(), tableColumn[i].getSecond(), optionalWhereClauses[i]));
                    // Note the MySQL specific fetch size limitation (Integer.MIN_VALUE). fetchSize
                    // activates result set streaming.
                    secStmt.setFetchSize(Integer.MIN_VALUE);
                    secStmt.setLong(1, primaryId);
                    secStmt.setLong(2, tableUpperLimits[i]);

                    secondaryPrepStmts[i] = secStmt;
                }

                deletePrepStmt = connection.prepareStatement(createPreparedDeleteStatement(primaryTableName, primaryColumnName, deleteBatchSize, primaryWhereClause));

                // Timeout is only checked at each bach start.
                // It can be further refined by being verified at each primary row processing.
                while (hasResults && !isTimeoutExceeded())
                {
                    // Process batch
                    primaryId = processPrimaryTableResultSet(primaryPrepStmt, secondaryPrepStmts, deletePrepStmt, deleteIds, primaryTableName, primaryColumnName, tableColumn);

                    if (primaryId == null)
                    {
                        break;
                    }

                    // Prepare for next batch
                    primaryPrepStmt.setLong(1, primaryId);
                    primaryPrepStmt.setLong(2, tableUpperLimits[0]);

                    for (int i = 1; i < tableColumn.length; i++)
                    {
                        PreparedStatement secStmt = secondaryPrepStmts[i];
                        secStmt.setLong(1, primaryId);
                        secStmt.setLong(2, tableUpperLimits[i]);
                    }

                    hasResults = primaryPrepStmt.execute();
                }
            }

            // Check if we have any more ids to delete
            if (!deleteIds.isEmpty())
            {
                deleteFromPrimaryTable(deletePrepStmt, deleteIds, primaryTableName);
                connection.commit();
            }

            if (logger.isDebugEnabled())
            {
                String msg = ((readOnly) ? "Script would have" : "Script") + " deleted a total of " + deletedCount + " items from table " + primaryTableName + ".";
                logger.debug(msg);
            }
        }
        finally
        {
            closeQuietly(deletePrepStmt);
            closeQuietly(secondaryPrepStmts);
            closeQuietly(primaryPrepStmt);
            closeQuietly(secondaryConnections);
            closeQuietly(primaryConnection);

            connection.setAutoCommit(true);
        }
    }

    protected void closeQuietly(Connection connection)
    {
        if (connection != null)
        {
            try
            {
                connection.close();
            }
            catch (Exception e)
            {
                logger.warn("Error closing DB connection: " + e.getMessage());
            }
        }
    }

    protected void closeQuietly(Connection[] connections)
    {
        if (connections != null)
        {
            for (Connection connection : connections)
            {
                closeQuietly(connection);
            }
        }
    }
}

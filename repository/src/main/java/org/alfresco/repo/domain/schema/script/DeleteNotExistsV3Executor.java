/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
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

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import org.alfresco.repo.domain.dialect.Dialect;
import org.alfresco.repo.domain.dialect.MySQLInnoDBDialect;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Same logic as DeleteNotExistsExecutor with the following changes:
 * <p/>
 * - filters the queries by unique values
 * <p/>
 * - eager close of result sets
 * <p/>
 * - we store all the ids in memory and process them from there - the secondary ids are stored in a unique list without
 * duplicate values.
 * <p/>
 * - we only cross 2 sets (the potential ids to delete from the primary table with the set of all secondary ids in that
 * range) removing all elements from the second set from the first set
 * <p/>
 * - every {pauseAndRecoverBatchSize} rows deleted we close all prepared statements and close the connection and sleep
 * for {pauseAndRecoverTime} milliseconds. This is necessary to allow the DBMS to perform the background tasks without
 * load from ACS. When we do not do this and if we are performing millions of deletes, the connection eventually gets
 * aborted.
 * 
 * @author Eva Vasques
 */
public class DeleteNotExistsV3Executor extends DeleteNotExistsExecutor
{
    private static Log logger = LogFactory.getLog(DeleteNotExistsV3Executor.class);

    public static final String PROPERTY_PAUSE_AND_RECOVER_BATCHSIZE = "system.delete_not_exists.pauseAndRecoverBatchSize";
    public static final String PROPERTY_PAUSE_AND_RECOVER_TIME = "system.delete_not_exists.pauseAndRecoverTime";
    public static final long DEFAULT_PAUSE_AND_RECOVER_BATCHSIZE = 500000;
    public static final long DEFAULT_PAUSE_AND_RECOVER_TIME = 300000;

    private Dialect dialect;
    private final DataSource dataSource;
    private long pauseAndRecoverTime;
    private long pauseAndRecoverBatchSize;
    private boolean pauseAndRecover = false;
    private int processedCounter = 0;

    public DeleteNotExistsV3Executor(Dialect dialect, Connection connection, String sql, int line, File scriptFile,
            Properties globalProperties, DataSource dataSource)
    {
        super(connection, sql, line, scriptFile, globalProperties);
        this.dialect = dialect;
        this.dataSource = dataSource;
    }

    @Override
    public void execute() throws Exception
    {
        checkProperties();

        String pauseAndRecoverBatchSizeString = globalProperties.getProperty(PROPERTY_PAUSE_AND_RECOVER_BATCHSIZE);
        pauseAndRecoverBatchSize = pauseAndRecoverBatchSizeString == null ? DEFAULT_PAUSE_AND_RECOVER_BATCHSIZE
                : Long.parseLong(pauseAndRecoverBatchSizeString);

        String pauseAndRecoverTimeString = globalProperties.getProperty(PROPERTY_PAUSE_AND_RECOVER_TIME);
        pauseAndRecoverTime = pauseAndRecoverTimeString == null ? DEFAULT_PAUSE_AND_RECOVER_TIME
                : Long.parseLong(pauseAndRecoverTimeString);

        super.execute();
    }

    @Override
    protected void process(Pair<String, String>[] tableColumn, Long[] tableUpperLimits, String[] optionalWhereClauses)
            throws SQLException
    {
        process(tableColumn, tableUpperLimits, optionalWhereClauses, 0L);
    }

    @Override
    protected void process(Pair<String, String>[] tableColumn, Long[] tableUpperLimits, String[] optionalWhereClauses, Long skipToId)
            throws SQLException
    {
        String primaryTableName = tableColumn[0].getFirst();
        String primaryColumnName = tableColumn[0].getSecond();
        String primaryWhereClause = optionalWhereClauses[0];

        Long primaryId = skipToId;

        deletedCount = 0L;
        startTime = new Date();

        processBatch(primaryTableName, primaryColumnName, primaryWhereClause, primaryId, tableColumn, tableUpperLimits,
                optionalWhereClauses);

        if (logger.isDebugEnabled())
        {
            String msg = ((readOnly) ? "Script would have" : "Script") + " deleted a total of " + deletedCount
                    + " items from table " + primaryTableName + ".";
            logger.debug(msg);
        }
    }

    private void processBatch(String primaryTableName, String primaryColumnName, String primaryWhereClause, Long primaryId,
            Pair<String, String>[] tableColumn, Long[] tableUpperLimits, String[] optionalWhereClauses) throws SQLException
    {
        PreparedStatement primaryPrepStmt = null;
        PreparedStatement[] secondaryPrepStmts = null;
        PreparedStatement deletePrepStmt = null;
        Set<Long> deleteIds = new HashSet<>();
        pauseAndRecover = false;

        try
        {

            connection.setAutoCommit(false);

            primaryPrepStmt = connection
                    .prepareStatement(createPreparedSelectStatement(primaryTableName, primaryColumnName, primaryWhereClause));
            primaryPrepStmt.setFetchSize(batchSize);
            primaryPrepStmt.setLong(1, primaryId);
            primaryPrepStmt.setLong(2, tableUpperLimits[0]);

            boolean hasResults = primaryPrepStmt.execute();

            if (hasResults)
            {

                // Prepared statements for secondary tables for the next batch
                secondaryPrepStmts = new PreparedStatement[tableColumn.length];
                for (int i = 1; i < tableColumn.length; i++)
                {
                    PreparedStatement secStmt = connection.prepareStatement(createPreparedSelectStatement(
                            tableColumn[i].getFirst(), tableColumn[i].getSecond(), optionalWhereClauses[i]));
                    secStmt.setFetchSize(batchSize);
                    secondaryPrepStmts[i] = secStmt;
                }

                deletePrepStmt = connection.prepareStatement(
                        createPreparedDeleteStatement(primaryTableName, primaryColumnName, deleteBatchSize, primaryWhereClause));

                // Timeout is only checked at each batch start.
                // It can be further refined by being verified at each primary row processing.
                while (hasResults && !isTimeoutExceeded())
                {
                    // Process batch
                    primaryId = processPrimaryTableResultSet(primaryPrepStmt, secondaryPrepStmts, deletePrepStmt, deleteIds,
                            primaryTableName, primaryColumnName, tableColumn);
                    connection.commit();

                    // If we have no more results (next primaryId is null) or job is marked for pause and recover, do
                    // not start the next batch
                    if (primaryId == null || pauseAndRecover)
                    {
                        break;
                    }

                    // Prepare for next batch
                    primaryPrepStmt.setLong(1, primaryId + 1);
                    primaryPrepStmt.setLong(2, tableUpperLimits[0]);

                    // Query the primary table for the next batch
                    hasResults = primaryPrepStmt.execute();
                }

                // Check if we have any more ids to delete
                if (!deleteIds.isEmpty())
                {
                    deleteFromPrimaryTable(deletePrepStmt, deleteIds, primaryTableName);
                    connection.commit();
                }
            }
        }
        finally
        {
            closeQuietly(deletePrepStmt);
            closeQuietly(secondaryPrepStmts);
            closeQuietly(primaryPrepStmt);

            closeQuietly(connection);
        }

        if (pauseAndRecover)
        {
            pauseAndRecoverJob(dataSource);
            logger.info("Resuming the job on primary table " + primaryTableName + " picking up after id " + primaryId);
            processBatch(primaryTableName, primaryColumnName, primaryWhereClause, primaryId, tableColumn, tableUpperLimits,
                    optionalWhereClauses);
        }
    }

    @Override
    protected String createPreparedSelectStatement(String tableName, String columnName, String whereClause)
    {
        StringBuilder sqlBuilder = new StringBuilder("SELECT " + columnName + " FROM " + tableName + " WHERE ");

        if (whereClause != null && !whereClause.isEmpty())
        {
            sqlBuilder.append(whereClause + " AND ");
        }

        sqlBuilder.append(
                columnName + " >= ? AND " + columnName + " <= ? GROUP BY " + columnName + " ORDER BY " + columnName + " ASC ");

        if (dialect instanceof MySQLInnoDBDialect)
        {
            sqlBuilder.append(" LIMIT " + batchSize);
        }
        else
        {
            sqlBuilder.append(" OFFSET 0 ROWS FETCH FIRST " + batchSize + " ROWS ONLY");
        }

        return sqlBuilder.toString();
    }

    @Override
    protected Long processPrimaryTableResultSet(PreparedStatement primaryPrepStmt, PreparedStatement[] secondaryPrepStmts,
            PreparedStatement deletePrepStmt, Set<Long> deleteIds, String primaryTableName, String primaryColumnName,
            Pair<String, String>[] tableColumn) throws SQLException
    {
        Long primaryId = null;
        Long minSecId = 0L;
        Long maxSecId = 0L;

        // Set all rows retrieved from the primary table as our potential ids to delete
        Set<Long> potentialIdsToDelete = new HashSet<Long>();
        Long minPotentialId = 0L;
        Long maxPotentialId = 0L;
        try (ResultSet resultSet = primaryPrepStmt.getResultSet())
        {
            while (resultSet.next())
            {
                primaryId = resultSet.getLong(primaryColumnName);
                potentialIdsToDelete.add(primaryId);

                minPotentialId = (minPotentialId == 0L || primaryId < minPotentialId) ? primaryId : minPotentialId;
                maxPotentialId = primaryId > maxPotentialId ? primaryId : maxPotentialId;
            }
        }

        if (potentialIdsToDelete.size() == 0)
        {
            // Nothing more to do
            return primaryId;
        }

        int rowsInBatch = potentialIdsToDelete.size();
        processedCounter = processedCounter + rowsInBatch;

        // Get a combined list of the ids present in the secondary tables
        SecondaryResultsInfo secondaryResultsInfo = getSecondaryResults(secondaryPrepStmts, tableColumn, minPotentialId,
                maxPotentialId);

        Set<Long> secondaryResults = secondaryResultsInfo.getValues();

        if (secondaryResultsInfo.getSize() > 0)
        {
            minSecId = secondaryResultsInfo.getMinValue();
            maxSecId = secondaryResultsInfo.getMaxValue();

            // From our potentialIdsToDelete list, remove all non-eligible ids: any id that is in a secondary table or
            // any ID past the last ID we were able to access in the secondary tables (maxSecId)
            Iterator<Long> it = potentialIdsToDelete.iterator();
            while (it.hasNext())
            {
                Long id = it.next();
                if (id > maxSecId || secondaryResults.contains(id))
                {
                    it.remove();
                }
            }

            // The next starting primary ID for the next batch will either be the next last id evaluated from the
            // primary table or, in case the secondary queries did not get that far, the last secondary table id
            // evaluated (maxSecId)
            primaryId = primaryId < maxSecId ? primaryId : maxSecId;
        }

        // Delete the ids that are eligble from the primary table
        if (potentialIdsToDelete.size() > 0)
        {
            deleteInBatches(potentialIdsToDelete, deleteIds, primaryTableName, deletePrepStmt);
        }

        if (logger.isTraceEnabled())
        {
            logger.trace("Rows processed " + rowsInBatch + " from primary table " + primaryTableName + ". Primary: ["
                    + minPotentialId + "," + maxPotentialId + "] Secondary rows processed: " + secondaryResultsInfo.getSize()
                    + " [" + minSecId + "," + maxSecId + "] Total Deleted: " + deletedCount);
        }

        // If the total rows processed from all batches so far is greater that the defined pauseAndRecoverBatchSize,
        // mark the job to pause and recover after completing this batch
        if (processedCounter >= pauseAndRecoverBatchSize)
        {
            pauseAndRecover = true;
        }

        // Return the last primary id processed for the next batch
        return primaryId;
    }

    private void deleteInBatches(Set<Long> potentialIdsToDelete, Set<Long> deleteIds, String primaryTableName,
            PreparedStatement deletePrepStmt) throws SQLException
    {
        Iterator<Long> potentialIdsIt = potentialIdsToDelete.iterator();
        while (potentialIdsIt.hasNext())
        {
            Long idToDelete = (Long) potentialIdsIt.next();
            deleteIds.add(idToDelete);

            if (deleteIds.size() == deleteBatchSize)
            {
                deleteFromPrimaryTable(deletePrepStmt, deleteIds, primaryTableName);
            }
        }
    }

    /*
     * Get a combined list of the ids present in all the secondary tables
     */
    private SecondaryResultsInfo getSecondaryResults(PreparedStatement[] preparedStatements, Pair<String, String>[] tableColumn,
            Long minPotentialId, Long maxPotentialId) throws SQLException
    {
        Set<Long> secondaryResultValues = new HashSet<Long>();
        Long lowestUpperValue = 0L;
        for (int i = 1; i < preparedStatements.length; i++)
        {
            String columnId = tableColumn[i].getSecond();
            PreparedStatement secStmt = preparedStatements[i];
            secStmt.setLong(1, minPotentialId);
            secStmt.setLong(2, maxPotentialId);

            // Execute the query on each secondary table
            boolean secHasResults = secStmt.execute();
            if (secHasResults)
            {
                try (ResultSet secResultSet = secStmt.getResultSet())
                {
                    Long thisId = 0L;
                    Long resultSize = 0L;
                    Long upperValue = 0L;
                    while (secResultSet.next())
                    {
                        resultSize++;
                        thisId = secResultSet.getLong(columnId);

                        // Add to the list if it's not there yet
                        if (!secondaryResultValues.contains(thisId))
                        {
                            secondaryResultValues.add(thisId);
                        }

                        upperValue = thisId > upperValue ? thisId : upperValue;
                    }

                    // Set the upper min value. We need to gather the last ID processed, so on the next batch on the
                    // primary table we can resume from there. We only need to do this if the number of results of the
                    // secondary table matches the batch size (if not, it means that there aren't more results up to
                    // maxPotentialId). Example on why this is needed: Primary table batch has 100k results from id's 1
                    // to 250000. Secondary table on that interval returns 100k results from id 3 to 210000. Next batch
                    // needs to start on id 210001
                    if (upperValue > 0 && resultSize == batchSize)
                    {
                        lowestUpperValue = (lowestUpperValue == 0L || upperValue < lowestUpperValue) ? upperValue
                                : lowestUpperValue;
                    }
                }
            }
        }

        // If lowestUpperValue is still 0 (because a secondary table never had more or the same number of results as the
        // primary table), the next id should be the last max id evaluated from the primary table (maxPotentialId)
        lowestUpperValue = lowestUpperValue == 0 ? maxPotentialId : lowestUpperValue;

        // Remove all values after the lower upper value of a secondary table
        long minSecId = 0L;
        Iterator<Long> it = secondaryResultValues.iterator();
        while (it.hasNext())
        {
            long secondaryId = it.next();
            if (secondaryId > lowestUpperValue)
            {
                it.remove();
            }
            else
            {
                minSecId = (minSecId == 0L || secondaryId < minSecId) ? secondaryId : minSecId;
            }
        }

        // Return a combined list of the ids present in all the secondary tables
        return new SecondaryResultsInfo(secondaryResultValues, minSecId, lowestUpperValue);
    }

    private class SecondaryResultsInfo
    {
        Set<Long> values;
        long minValue;
        long maxValue;
        long size;

        public SecondaryResultsInfo(Set<Long> values, long minValue, long maxValue)
        {
            super();
            this.values = values;
            this.minValue = minValue;
            this.maxValue = maxValue;
            this.size = values.size();
        }

        public Set<Long> getValues()
        {
            return values;
        }

        public long getMinValue()
        {
            return minValue;
        }

        public long getMaxValue()
        {
            return maxValue;
        }

        public long getSize()
        {
            return size;
        }
    }

    /*
     * Sleep for {pauseAndRecoverTime} before opening a new connection and continue to process new batches
     */
    private void pauseAndRecoverJob(DataSource dataSource) throws SQLException
    {
        if (logger.isDebugEnabled())
        {
            logger.debug("Reached batch size for pause and recovery. Job will resume in " + pauseAndRecoverTime + " ms");
        }
        // Wait
        try
        {
            Thread.sleep(pauseAndRecoverTime);
        }
        catch (InterruptedException e)
        {
            // Do nothing
        }
        // Start another connection and continue where we left off
        connection = dataSource.getConnection();
        pauseAndRecover = false;
        processedCounter = 0;
    }

    protected void closeQuietly(Connection connection)
    {
        try
        {
            connection.close();
        }
        catch (SQLException e)
        {
            // Do nothing
        }
        finally
        {
            connection = null;
        }
    }
}
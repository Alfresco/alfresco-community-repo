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

import org.alfresco.util.LogUtil;
import org.alfresco.util.Pair;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * The
 * <code>--DELETE_NOT_EXISTS primaryTable.columnName,secondaryTable1.columnName1,...,secondaryTableN.columnNameN batch.size.property</code>
 * statement is used to delete all the items that don't have any corresponding
 * key in any of the secondary tables (e.g. secondaryTable1.columnName1,...,secondaryTableN.columnNameN).
 * <p/>
 * The processing of the tables and the actual deletes are done in batches to support a high volume of data. It can be influenced using: <br>
 * <code>system.delete_not_exists.batchsize</code> and/or <code>system.delete_not_exists.delete_batchsize</code>
 * <p/>
 * The statement can be executed in read only mode using: <code>system.delete_not_exists.read_only</code>.
 * <p/>
 * In case of high volume of data we can limit the processing time using: <code>system.delete_not_exists.timeout_seconds</code>.
 * 
 * @author Cristian Turlica
 */
public class DeleteNotExistsExecutor implements StatementExecutor
{
    private static Log logger = LogFactory.getLog(DeleteNotExistsExecutor.class);

    private static final String ERR_STATEMENT_FAILED = "schema.update.err.statement_failed";
    private static final String MSG_OPTIONAL_STATEMENT_FAILED = "schema.update.msg.optional_statement_failed";

    public static final String PROPERTY_BATCH_SIZE = "system.delete_not_exists.batchsize";
    public static final String PROPERTY_DELETE_BATCH_SIZE = "system.delete_not_exists.delete_batchsize";
    public static final String PROPERTY_READ_ONLY = "system.delete_not_exists.read_only";
    public static final String PROPERTY_TIMEOUT_SECONDS = "system.delete_not_exists.timeout_seconds";

    private Connection connection;
    private String sql;
    private int line;
    private File scriptFile;
    private Properties globalProperties;

    private boolean readOnly;
    private int deleteBatchSize;
    private int batchSize;
    private long timeoutSec;

    private long deletedCount;
    private Date startTime;

    public DeleteNotExistsExecutor(Connection connection, String sql, int line, File scriptFile, Properties globalProperties)
    {
        this.connection = connection;
        this.sql = sql;
        this.line = line;
        this.scriptFile = scriptFile;
        this.globalProperties = globalProperties;
    }

    public void checkProperties()
    {
         PropertyCheck.mandatory(this, "globalProperties", globalProperties);
    }

    public void execute() throws Exception
    {
        checkProperties();

        if (logger.isTraceEnabled())
        {
            logger.trace("Execute statement: " + sql);
        }

        // --DELETE_NOT_EXISTS primaryTable.key,secondaryTable1.key1,... batch.size.property
        String[] args = sql.split("[ \\t]+(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
        if (args.length == 3 && (args[1].indexOf('.')) != -1)
        {
            String[] tableColumnArgs = args[1].split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            if (tableColumnArgs.length >= 2)
            {
                // Read the batch size from the named property
                String batchSizeString = globalProperties.getProperty(args[2]);
                // Fall back to the default property
                if (batchSizeString == null)
                {
                    batchSizeString = globalProperties.getProperty(PROPERTY_BATCH_SIZE);
                }

                batchSize = batchSizeString == null ? 100000 : Integer.parseInt(batchSizeString);

                // Read the batch size from the named property
                String deleteBatchSizeString = globalProperties.getProperty(PROPERTY_DELETE_BATCH_SIZE);
                deleteBatchSize = deleteBatchSizeString == null ? 1000 : Integer.parseInt(deleteBatchSizeString);

                String readOnlyString = globalProperties.getProperty(PROPERTY_READ_ONLY);
                readOnly = readOnlyString != null && Boolean.parseBoolean(readOnlyString);

                String timeoutSecString = globalProperties.getProperty(PROPERTY_TIMEOUT_SECONDS);
                timeoutSec = timeoutSecString == null ? -1 : Long.parseLong(timeoutSecString);

                // Compute upper limits
                Long[] tableUpperLimits = new Long[tableColumnArgs.length];
                Pair<String, String>[] tableColumn = new Pair[tableColumnArgs.length];
                String[] optionalWhereClauses = new String[tableColumnArgs.length];
                String[] tableDetails;
                for (int i = 0; i < tableColumnArgs.length; i++)
                {
                    tableDetails = tableColumnArgs[i].split("\\.");

                    String tableName = tableDetails[0];
                    String columnName = tableDetails[1];

                    if (tableDetails.length == 3)
                    {
                        optionalWhereClauses[i] = removeDoubleQuotes(tableDetails[2]);
                    }

                    tableColumn[i] = new Pair<>(tableName, columnName);
                    tableUpperLimits[i] = getBatchUpperLimit(connection, tableName, columnName, line, scriptFile);

                    if (logger.isTraceEnabled())
                    {
                        logger.trace("BatchUpperLimit " + tableUpperLimits[i] + " for " + tableName + "." + columnName);
                    }
                }

                process(tableColumn, tableUpperLimits, optionalWhereClauses);
            }
        }
    }

    private void process(Pair<String, String>[] tableColumn, Long[] tableUpperLimits, String[] optionalWhereClauses) throws SQLException
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
        PreparedStatement primaryPrepStmt = null;
        PreparedStatement[] secondaryPrepStmts = null;
        PreparedStatement deletePrepStmt = null;
        Set<Long> deleteIds = new HashSet<>();

        deletedCount = 0L;
        startTime = new Date();
        try
        {
            connection.setAutoCommit(false);
            primaryPrepStmt = connection.prepareStatement(createPreparedSelectStatement(primaryTableName, primaryColumnName, primaryWhereClause));
            primaryPrepStmt.setFetchSize(batchSize);
            primaryPrepStmt.setLong(1, primaryId);
            primaryPrepStmt.setLong(2, tableUpperLimits[0]);

            boolean hasResults = primaryPrepStmt.execute();

            if (hasResults)
            {

                secondaryPrepStmts = new PreparedStatement[tableColumn.length];
                for (int i = 1; i < tableColumn.length; i++)
                {
                    PreparedStatement secStmt = connection.prepareStatement(createPreparedSelectStatement(tableColumn[i].getFirst(), tableColumn[i].getSecond(), optionalWhereClauses[i]));
                    secStmt.setFetchSize(batchSize);
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
                    connection.commit();

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

            connection.setAutoCommit(true);
        }
    }

    private boolean isTimeoutExceeded()
    {
        if (timeoutSec <= 0)
        {
            return false;
        }

        Date now = new Date();
        return (now.getTime() > startTime.getTime() + (timeoutSec * 1000));
    }

    private Long processPrimaryTableResultSet(PreparedStatement primaryPrepStmt, PreparedStatement[] secondaryPrepStmts, PreparedStatement deletePrepStmt, Set<Long> deleteIds, String primaryTableName,
            String primaryColumnName, Pair<String, String>[] tableColumn) throws SQLException
    {
        int rowsProcessed = 0;
        Long primaryId = null;
        ResultSet[] secondaryResultSets = null;
        try (ResultSet resultSet = primaryPrepStmt.getResultSet())
        {
            secondaryResultSets = getSecondaryResultSets(secondaryPrepStmts);
            Long[] secondaryIds = getSecondaryIds(secondaryResultSets, tableColumn);

            while (resultSet.next())
            {
                ++rowsProcessed;
                primaryId = resultSet.getLong(primaryColumnName);

                while (isLess(primaryId, secondaryIds))
                {
                    deleteIds.add(primaryId);

                    if (deleteIds.size() == deleteBatchSize)
                    {
                        deleteFromPrimaryTable(deletePrepStmt, deleteIds, primaryTableName);
                    }

                    if (!resultSet.next())
                    {
                        break;
                    }

                    ++rowsProcessed;
                    primaryId = resultSet.getLong(primaryColumnName);

                    // Try to limit processing to a reasonable size.
                    if (rowsProcessed == batchSize)
                    {
                        break;
                    }
                }

                // Try to limit processing to a reasonable size.
                if (rowsProcessed == batchSize)
                {
                    if (logger.isTraceEnabled())
                    {
                        logger.trace("RowsProcessed " + rowsProcessed + " from primary table " + primaryTableName);
                    }
                    break;
                }

                updateSecondaryIds(primaryId, secondaryIds, secondaryResultSets, tableColumn);
            }
        }
        finally
        {
            closeQuietly(secondaryResultSets);
        }

        return primaryId;
    }

    private void deleteFromPrimaryTable(PreparedStatement deletePrepStmt, Set<Long> deleteIds, String primaryTableName) throws SQLException
    {
        int deletedBatchCount = deleteIds.size();
        if (!readOnly && !deleteIds.isEmpty())
        {
            if (logger.isTraceEnabled())
            {
                logger.trace("Prepare to delete " + deleteIds.size() + " items from table " + primaryTableName + ".");
            }

            deletedBatchCount = executeDeleteStatement(deletePrepStmt, deleteIds, deleteBatchSize, line, scriptFile);
        }

        deletedCount += deletedBatchCount;

        if (logger.isTraceEnabled())
        {
            String msg = ((readOnly) ? "Script would have" : "Script") + " deleted a batch of " + deletedBatchCount + " items from table " + primaryTableName + ".";
            logger.trace(msg);
        }

        deleteIds.clear();
    }

    /**
     * Execute the given SQL statement, absorbing exceptions that we expect.
     *
     * @param fetchColumnName
     *            the name of the column value to return
     */
    private Object executeStatement(Connection connection, String sql, String fetchColumnName, boolean optional, int line, File file) throws SQLException
    {
        Statement stmt = null;
        Object ret = null;
        try
        {
            stmt = connection.createStatement();
            if (logger.isTraceEnabled())
            {
                logger.trace("Executing statement: " + sql);
            }
            boolean haveResults = stmt.execute(sql);
            if (haveResults && fetchColumnName != null)
            {
                try (ResultSet rs = stmt.getResultSet())
                {
                    if (rs.next())
                    {
                        // Get the result value
                        ret = rs.getObject(fetchColumnName);
                    }
                }
            }
        }
        catch (SQLException e)
        {
            if (optional)
            {
                // it was marked as optional, so we just ignore it
                LogUtil.debug(logger, MSG_OPTIONAL_STATEMENT_FAILED, sql, e.getMessage(), file.getAbsolutePath(), line);
            }
            else
            {
                LogUtil.error(logger, ERR_STATEMENT_FAILED, sql, e.getMessage(), file.getAbsolutePath(), line);
                throw e;
            }
        }
        finally
        {
            closeQuietly(stmt);
        }
        return ret;
    }

    private Long getBatchUpperLimit(Connection connection, String tableName, String columnName, int line, File scriptFile) throws SQLException
    {
        Long batchUpperLimit = 0L;

        String stmt = "SELECT MAX(" + columnName + ") AS upper_limit FROM " + tableName;
        Object fetchedVal = executeStatement(connection, stmt, "upper_limit", false, line, scriptFile);

        if (fetchedVal instanceof Number)
        {
            batchUpperLimit = ((Number) fetchedVal).longValue();
        }

        return batchUpperLimit;
    }

    private boolean isLess(Long primaryId, Long[] secondaryIds)
    {
        for (Long secondaryId : secondaryIds)
        {
            if (secondaryId != null && primaryId >= secondaryId)
            {
                return false;
            }
        }

        return true;
    }

    private String removeDoubleQuotes(String quotedString)
    {
        if (quotedString == null || quotedString.isEmpty())
        {
            return quotedString;
        }

        return quotedString.replace("\"", "");
    }
    
    private String createPreparedSelectStatement(String tableName, String columnName, String whereClause)
    {
        StringBuilder sqlBuilder = new StringBuilder("SELECT " + columnName + " FROM " + tableName + " WHERE ");

        if (whereClause != null && !whereClause.isEmpty())
        {
            sqlBuilder.append(whereClause + " AND ");
        }

        sqlBuilder.append(columnName + " > ? AND " + columnName + " <= ? ORDER BY " + columnName + " ASC");
        return sqlBuilder.toString();
    }

    private String createPreparedDeleteStatement(String tableName, String idColumnName, int deleteBatchSize, String whereClause)
    {
        StringBuilder stmtBuilder = new StringBuilder("DELETE FROM " + tableName + " WHERE ");

        if (whereClause != null && !whereClause.isEmpty())
        {
            stmtBuilder.append(whereClause + " AND ");
        }
        stmtBuilder.append(idColumnName + " IN ");
        stmtBuilder.append("(");

        for (int i = 1; i <= deleteBatchSize; i++)
        {
            if (i < deleteBatchSize)
            {
                stmtBuilder.append("?,");
            }
            else
            {
                stmtBuilder.append("?");
            }
        }
        stmtBuilder.append(")");

        return stmtBuilder.toString();
    }

    private int executeDeleteStatement(PreparedStatement stmt, Set<Long> deleteIds, int deleteBatchSize, int line, File scriptFile)
            throws SQLException
    {
        try
        {
            int i = 1;
            for (Long deleteId : deleteIds)
            {
                stmt.setObject(i, deleteId);
                i++;
            }

            for (int j = i; j <= deleteBatchSize; j++)
            {
                stmt.setObject(j, 0);
            }

            int deletedItems = stmt.executeUpdate();
            return deletedItems;
        }
        catch (SQLException e)
        {
            LogUtil.error(logger, ERR_STATEMENT_FAILED, sql, e.getMessage(), scriptFile.getAbsolutePath(), line);
            throw e;
        }
    }

    private Long getColumnValueById(ResultSet resultSet, String columnId) throws SQLException
    {
        Long columnValue = null;
        if (resultSet != null && resultSet.next())
        {
            columnValue = resultSet.getLong(columnId);
        }

        return columnValue;
    }

    private ResultSet[] getSecondaryResultSets(PreparedStatement[] preparedStatements) throws SQLException
    {
        ResultSet[] secondaryResultSets = new ResultSet[preparedStatements.length];
        for (int i = 1; i < preparedStatements.length; i++)
        {
            PreparedStatement secStmt = preparedStatements[i];

            boolean secHasResults = secStmt.execute();
            secondaryResultSets[i] = secHasResults ? secStmt.getResultSet() : null;
        }

        return secondaryResultSets;
    }

    private Long[] getSecondaryIds(ResultSet[] secondaryResultSets, Pair<String, String>[] tableColumn) throws SQLException
    {
        Long[] secondaryIds = new Long[tableColumn.length];

        for (int i = 1; i < tableColumn.length; i++)
        {
            ResultSet resultSet = secondaryResultSets[i];
            String columnId = tableColumn[i].getSecond();

            secondaryIds[i] = getColumnValueById(resultSet, columnId);
        }

        return secondaryIds;
    }

    private void updateSecondaryIds(Long primaryId, Long[] secondaryIds, ResultSet[] secondaryResultSets, Pair<String, String>[] tableColumn) throws SQLException
    {
        for (int i = 1; i < tableColumn.length; i++)
        {
            Long secondaryId = secondaryIds[i];
            while (secondaryId != null && primaryId >= secondaryId)
            {
                ResultSet resultSet = secondaryResultSets[i];
                String columnId = tableColumn[i].getSecond();

                secondaryId = getColumnValueById(resultSet, columnId);
                secondaryIds[i] = secondaryId;
            }
        }
    }

    private void closeQuietly(Statement statement)
    {
        if (statement != null)
        {
            try
            {
                statement.close();
            }
            catch (Exception e)
            {
                // Little can be done at this stage.
            }
        }
    }

    private void closeQuietly(Statement[] statements)
    {
        if (statements != null)
        {
            for (Statement statement : statements)
            {
                closeQuietly(statement);
            }
        }
    }

    private void closeQuietly(ResultSet resultSet)
    {
        if (resultSet != null)
        {
            try
            {
                resultSet.close();
            }
            catch (Exception e)
            {
                // Little can be done at this stage.
            }
        }
    }

    private void closeQuietly(ResultSet[] resultSets)
    {
        if (resultSets != null)
        {
            for (ResultSet resultSet : resultSets)
            {
                closeQuietly(resultSet);
            }
        }
    }
}

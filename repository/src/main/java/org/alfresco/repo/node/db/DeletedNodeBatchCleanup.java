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

package org.alfresco.repo.node.db;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.dialect.Dialect;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Cleans up deleted nodes{@link #purgeOldDeletedNodes()} and dangling transactions{@link #purgeOldEmptyTransactions()}
 * that are old enough{@link #minPurgeAgeMs} in fixed size batches.
 * The Algorithm fetches the deleted nodes in batches{@link #batchSize} and uses the node id to delete the entries
 * in alf_node and alf_node_properties table. The batch size to fetch and delete{@link #deleteBatchSize} the entries are configurable.
 * The alf_transactions entries which doesn't have an entry in alf_node table are selected for deletion in batches.
 */
public class DeletedNodeBatchCleanup
{
    private final static String SELECT_NODE_STATEMENT =
                "select node.id as id from alf_node node join alf_transaction txn on (node.transaction_id = txn.id) "
                            + "where node.id > ? and txn.commit_time_ms < ?  and node.type_qname_id = ? order by node.id asc";
    private final static String SELECT_TXN_STATEMENT =
                "select id from alf_transaction  where not exists (  select 1 from alf_node node where"
                            + " node.transaction_id = alf_transaction.id) and id > ? and commit_time_ms <= ? order by id asc";
    private final static String DELETE_NODE_PROP_STATEMENT = "DELETE FROM ALF_NODE_PROPERTIES WHERE NODE_ID IN (";
    private final static String DELETE_NODE_STATEMENT = "DELETE FROM ALF_NODE WHERE ID IN (";
    private final static String DELETE_TXN_STATEMENT = "DELETE FROM ALF_TRANSACTION WHERE ID IN (";
    private final static Log logger = LogFactory.getLog(DeletedNodeBatchCleanup.class);
    private int deleteBatchSize;
    private int batchSize;
    private QNameDAO qnameDAO;
    private DataSource dataSource;
    //TODO - use the dialect to have different algorithm for MySQL as it needs an offset based implementation
    //see org.alfresco.repo.domain.schema.script.DeleteNotExistsExecutor
    private Dialect dialect;
    private long minPurgeAgeMs;
    private long timeoutSec;
    private final AtomicLong nodeDeletionCount = new AtomicLong(0);
    private final AtomicLong txnDeletionCount = new AtomicLong(0);

    public void setDataSource(DataSource dataSource)
    {
        this.dataSource = dataSource;
    }

    public void setDialect(Dialect dialect)
    {
        this.dialect = dialect;
    }

    public void setQnameDAO(QNameDAO qnameDAO)
    {
        this.qnameDAO = qnameDAO;
    }

    public void setMinPurgeAgeMs(long minPurgeAgeMs)
    {
        this.minPurgeAgeMs = minPurgeAgeMs;
        ;
    }

    public void setDeleteBatchSize(int deleteBatchSize)
    {
        this.deleteBatchSize = deleteBatchSize;
    }

    public void setBatchSize(int batchSize)
    {
        this.batchSize = batchSize;
    }

    public void setTimeoutSec(long timeoutSec)
    {
        this.timeoutSec = timeoutSec;
    }

    public List<String> purgeOldDeletedNodes() throws SQLException
    {

        nodeDeletionCount.getAndSet(0);
        List<String> deleteResult = purge(DeletionType.NODE);
        if(logger.isDebugEnabled())
        {
            logger.debug("Total nodes deleted:"+ nodeDeletionCount.get());
        }
        return deleteResult;
    }

    public List<String> purgeOldEmptyTransactions() throws SQLException
    {

        txnDeletionCount.getAndSet(0);
        List<String> deleteResult =  purge(DeletionType.TRANSACTION);
        if(logger.isDebugEnabled())
        {
            logger.debug("Total transactions deleted:"+ txnDeletionCount.get());
            logger.debug(deleteResult);
        }
        return deleteResult;


    }

    private List<String> purge(DeletionType deletionType) throws SQLException
    {
        if(batchSize < deleteBatchSize)
        {
            return Collections.singletonList("The batchSize should be equal or greater than deleteBatchSize");
        }
        final List<String> deletedList = new ArrayList<>();
        PreparedStatement primaryPrepStmt = null;
        PreparedStatement deleteEntityPrepStmt[] = new PreparedStatement[2];
        //select the delete

        final Pair<Long, QName> deletedTypePair = qnameDAO.getQName(ContentModel.TYPE_DELETED);

        final Date startTime = new Date();
        final long maxCommitTime = System.currentTimeMillis() - minPurgeAgeMs;
        Long primaryId = 0L;
        String primaryPrepStatementSQL = SELECT_NODE_STATEMENT;
        String deletionPrepSatatementSQL = DELETE_NODE_STATEMENT;

        final Long selectPrepStatementFirstParam = primaryId;
        final Long selectPrepStatemenSecondParam = maxCommitTime;
        final Long selectPrepStatemenThirdParam = deletedTypePair.getFirst();

        if (deletionType == DeletionType.TRANSACTION)
        {
            primaryPrepStatementSQL = SELECT_TXN_STATEMENT;
            deletionPrepSatatementSQL = DELETE_TXN_STATEMENT;
        }
        try(Connection connection = dataSource.getConnection())
        {
            connection.setAutoCommit(false);
            primaryPrepStmt = connection.prepareStatement(primaryPrepStatementSQL);
            primaryPrepStmt.setFetchSize(batchSize);
            primaryPrepStmt.setLong(1, selectPrepStatementFirstParam);
            primaryPrepStmt.setLong(2, selectPrepStatemenSecondParam);
            if (deletionType == DeletionType.NODE)
            {
                primaryPrepStmt.setLong(3, selectPrepStatemenThirdParam);
            }
            boolean hasResults = primaryPrepStmt.execute();
            Set<Long> deleteIds = new HashSet<>();
            while (hasResults && !isTimeoutExceeded(startTime))
            {

                deleteEntityPrepStmt[0] = connection
                            .prepareStatement(createDeleteStatement(deleteBatchSize, deletionPrepSatatementSQL));
                if (deletionType == DeletionType.NODE)
                {
                    deleteEntityPrepStmt[1] = connection
                                .prepareStatement(createDeleteStatement(deleteBatchSize, DELETE_NODE_PROP_STATEMENT));
                }

                primaryId = processQueryResults(primaryPrepStmt, deleteEntityPrepStmt, deleteIds, connection,
                            deletedList, deletionType);
                if (primaryId == null)
                {
                    break;
                }
                primaryPrepStmt.setLong(1, primaryId);
                hasResults = primaryPrepStmt.execute();

            }

        }
        finally
        {
            closeStatement(primaryPrepStmt);
            closeStatement(deleteEntityPrepStmt[0]);
            closeStatement(deleteEntityPrepStmt[1]);

        }

        if (deletionType == DeletionType.NODE)
        {
            logger.debug("DeletedNodeBatchCleanup: purgeOldDeletedNodes finished ");
        }
        else
        {
            logger.debug("DeletedNodeBatchCleanup: purgeOldTransactions finished ");
        }

        return deletedList;
    }

    private Long processQueryResults(PreparedStatement primaryPrepStmt, PreparedStatement[] deletePrepStmts,
                Set<Long> deleteIds, Connection connection, List<String> deleteResult, DeletionType deletionType)
                throws SQLException
    {
        Long primaryId = null;
        int rowsProcessed = 0;
        try (ResultSet resultSet = primaryPrepStmt.getResultSet())
        {
            while (resultSet.next())
            {

                ++rowsProcessed;
                primaryId = resultSet.getLong("ID");
                deleteIds.add(primaryId);
                if (deleteIds.size() == deleteBatchSize)
                {
                    if (deletionType == DeletionType.NODE)
                        processNodeDeletion(deletePrepStmts, deleteIds, connection, deleteResult);
                    else
                        processTxnDeletion(deletePrepStmts[0], deleteIds, connection, deleteResult);

                }
                if (rowsProcessed == batchSize)
                {
                    if (logger.isDebugEnabled())
                    {
                        if (deletionType == DeletionType.NODE)
                        {
                            logger.debug("RowsProcessed " + rowsProcessed + " from primary table alf_node ");
                        }
                        else
                        {
                            logger.debug("RowsProcessed " + rowsProcessed + " from primary table alf_transaction");
                        }
                    }
                    break;
                }
            }
            if (!deleteIds.isEmpty())
            {
                if (deletionType == DeletionType.NODE)
                    processNodeDeletion(deletePrepStmts, deleteIds, connection, deleteResult);
                else
                    processTxnDeletion(deletePrepStmts[0], deleteIds, connection, deleteResult);

            }

        }

        return primaryId;

    }

    private void processNodeDeletion(PreparedStatement[] preparedStatements, Set<Long> deleteIds, Connection connection,
                List<String> deleteResult) throws SQLException
    {

        int deletedNodePropItems = deleteItems(preparedStatements[1], deleteIds);

        int deletedNodeItems = deleteItems(preparedStatements[0], deleteIds);
        nodeDeletionCount.getAndAdd(deletedNodeItems);
        connection.commit();
        deleteIds.clear();
        if (logger.isDebugEnabled())
        {
            logger.debug("alf_node entries deleted " + deletedNodeItems);
            logger.debug("alf_node_properties entries deleted " + deletedNodePropItems);
        }
        deleteResult.add("Purged old nodes: " + deletedNodeItems);

    }

    private void processTxnDeletion(PreparedStatement txnPrepStmt, Set<Long> deleteIds, Connection connection,
                List<String> deleteResult) throws SQLException
    {

        int deletedTxnItems = deleteItems(txnPrepStmt, deleteIds);
        txnDeletionCount.getAndAdd(deletedTxnItems);
        connection.commit();
        deleteIds.clear();
        if (logger.isDebugEnabled())
        {
            logger.debug("alf_transaction entries deleted " + deletedTxnItems);

        }
        deleteResult.add("Purged old transactions:" + deletedTxnItems);

    }

    private int deleteItems(PreparedStatement preparedStatement, Set<Long> deleteIds) throws SQLException
    {
        int i = 1;
        for (Long deleteId : deleteIds)
        {
            preparedStatement.setObject(i, deleteId);
            i++;
        }

        for (int j = i; j <= deleteBatchSize; j++)
        {
            preparedStatement.setObject(j, 0);
        }

        return preparedStatement.executeUpdate();

    }

    private String createDeleteStatement(int deleteBatchSize, String deleteSQL)
    {
        StringBuilder stmtBuilder = new StringBuilder(deleteSQL);
        for (int i = 1; i <= deleteBatchSize; i++)
        {
            if (i < deleteBatchSize)
            {
                stmtBuilder.append("?,");
            }
            else
            {
                stmtBuilder.append('?');
            }
        }
        stmtBuilder.append(')');
        return stmtBuilder.toString();
    }

    private boolean isTimeoutExceeded(Date startTime)
    {
        if (timeoutSec <= 0)
        {
            return false;
        }

        final Date now = new Date();
        return (now.getTime() > startTime.getTime() + (timeoutSec * 1000));
    }

    protected void closeStatement(Statement statement)
    {
        if (statement != null)
        {
            try
            {
                statement.close();
            }
            catch (SQLException e)
            {
               logger.error("Error while closing statement:",e);
            }
        }
    }

    private enum DeletionType
    {

        NODE, TRANSACTION

    }

}

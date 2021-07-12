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
import org.alfresco.repo.domain.schema.script.DeleteNotExistsExecutor;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Cleans up deleted nodes and dangling transactions in fixed size batches.
 */
public class DeletedNodeBatchCleanup
{

    private DataSource dataSource;
    private Dialect dialect;
    private long minPurgeAgeMs;
    protected int deleteBatchSize;
    protected int batchSize;
    private long timeoutSec;
    protected QNameDAO qnameDAO;

    private static Log logger = LogFactory.getLog(DeletedNodeBatchCleanup.class);

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

    public void setMinPurgeAgeMs(long minPurgeAgeDays)
    {
        this.minPurgeAgeMs = ((long) minPurgeAgeDays) * 24L * 3600L * 1000L;;
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

    public List<String> execute() throws Exception
    {
        List<String> list = new ArrayList<>();
        PreparedStatement primaryPrepStmt = null;
        //select the delete
        String selectNodes = "select node.id from alf_node node join alf_transaction txn on (node.transaction_id = txn.id) where node.type_qname_id = ? and txn.commit_time_ms < ?";
        Pair<Long, QName> deletedTypePair = qnameDAO.getQName(ContentModel.TYPE_DELETED);

        Date startTime = new Date();
        final long maxCommitTime = System.currentTimeMillis() - minPurgeAgeMs;
        try
        {
            Connection connection = dataSource.getConnection();
            connection.setAutoCommit(true);
            primaryPrepStmt = connection.prepareStatement(selectNodes);
            primaryPrepStmt.setFetchSize(batchSize);
            primaryPrepStmt.setLong(1, deletedTypePair.getFirst());
            primaryPrepStmt.setLong(2, maxCommitTime);
            boolean hasResults = primaryPrepStmt.execute();
            while(hasResults && !isTimeoutExceeded(startTime))
            {
                //delete alf_node_properties
                //delete alf_node
            }


        }
        finally
        {

        }

        return list;
    }

    private boolean isTimeoutExceeded(Date startTime)
    {
        if (timeoutSec <= 0)
        {
            return false;
        }

        Date now = new Date();
        return (now.getTime() > startTime.getTime() + (timeoutSec * 1000));
    }
}

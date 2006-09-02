/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.domain.hibernate;

import java.io.Serializable;

import org.alfresco.repo.domain.Server;
import org.alfresco.repo.domain.Transaction;

/**
 * Bean containing all the persistence data representing a <b>Transaction</b>.
 * <p>
 * This implementation of the {@link org.alfresco.repo.domain.Transaction Transaction} interface is
 * Hibernate specific.
 * 
 * @author Derek Hulley
 */
public class TransactionImpl extends LifecycleAdapter implements Transaction, Serializable
{
    private static final long serialVersionUID = -8264339795578077552L;

    private Long id;
    private String changeTxnId;
    private Server server;
    
    public TransactionImpl()
    {
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(50);
        sb.append("Transaction")
          .append("[id=").append(id)
          .append(", changeTxnId=").append(changeTxnId)
          .append("]");
        return sb.toString();
    }
    
    public Long getId()
    {
        return id;
    }

    /**
     * For Hibernate use
     */
    @SuppressWarnings("unused")
    private void setId(Long id)
    {
        this.id = id;
    }

    public String getChangeTxnId()
    {
        return changeTxnId;
    }

    public void setChangeTxnId(String changeTransactionId)
    {
        this.changeTxnId = changeTransactionId;
    }

    public Server getServer()
    {
        return server;
    }

    public void setServer(Server server)
    {
        this.server = server;
    }
}

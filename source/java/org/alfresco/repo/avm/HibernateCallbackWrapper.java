/*
 * Copyright (C) 2006 Alfresco, Inc.
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
package org.alfresco.repo.avm;

import java.sql.SQLException;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * This is a wrapper around HibernateTxnCallback implementation.
 * @author britt
 */
class HibernateCallbackWrapper implements HibernateCallback
{
    /**
     * The HibernateTxnCallback to execute.
     */
    private RetryingTransactionCallback fCallback;
    
    /**
     * Make one up.
     * @param callback
     */
    public HibernateCallbackWrapper(RetryingTransactionCallback callback)
    {
        fCallback = callback;
    }
    
    /**
     * Call the HibernateTxnCallback internally.
     * @param session The Hibernate Session.
     */
    public Object doInHibernate(Session session) throws HibernateException,
            SQLException
    {
        fCallback.perform();
        return null;
    }
}

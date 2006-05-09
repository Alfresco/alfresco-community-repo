package org.alfresco.repo.avm.hibernate;

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

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

/**
 * Helper for DAOs.
 * @author britt
 */
public class HibernateTxn
{
    /**
     * The SessionFactory.
     */
    private SessionFactory fSessionFactory;
    
    /**
     * Make one up.
     * @param sessionFactory The SessionFactory.
     */
    public HibernateTxn(SessionFactory sessionFactory)
    {
        fSessionFactory = sessionFactory;
    }
    
    /**
     * Perform a set of operations under a single Hibernate transaction.
     * @param callback The worker.
     * @return Whether the operation finished with a commit.
     */
    public boolean perform(HibernateTxnCallback callback)
    {
        Session sess = null;
        Transaction txn = null;
        try
        {
            sess = fSessionFactory.openSession();
            txn = sess.beginTransaction();
            callback.perform(sess);
            txn.commit();
            return true;
        }
        catch (Throwable t)
        {
            t.printStackTrace(System.err);
            if (txn != null)
            {
                try
                {
                    txn.rollback();
                }
                catch (HibernateException he)
                {
                    // Do nothing.
                }
            }
            return false;
        }
        finally
        {
            if (sess != null)
            {
                try
                {
                    sess.close();
                }
                catch (HibernateException he)
                {
                    // Do nothing.
                }
            }
        }
    }
}

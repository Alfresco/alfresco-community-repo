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

package org.alfresco.repo.avm.hibernate;

import java.util.List;

import org.alfresco.repo.avm.AVMNode;
import org.alfresco.repo.avm.AVMStore;
import org.alfresco.repo.avm.NewInAVMStore;
import org.alfresco.repo.avm.NewInAVMStoreDAO;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Hibernate implementation of NewInAVMStore DAO.
 * @author britt
 */
class NewInAVMStoreDAOHibernate extends HibernateDaoSupport implements
        NewInAVMStoreDAO
{
    /**
     * Save one.
     * @param newEntry The item to save.
     */
    public void save(NewInAVMStore newEntry)
    {
        getSession().save(newEntry);
    }
    
    /**
     * Get one by Node.
     * @param node The node to lookup with.
     * @return The Entry or null if not found.
     */
    public NewInAVMStore getByNode(AVMNode node)
    {
        Query query = getSession().createQuery("from NewInAVMStoreImpl nie where nie.node = :node");
        query.setEntity("node", node);
        return (NewInAVMStore)query.uniqueResult();
    }

    /**
     * Get all that are in the given store.
     * @param store The AVMStore.
     * @return A List of NewInAVMStores.
     */
    @SuppressWarnings("unchecked")
    public List<NewInAVMStore> getByAVMStore(AVMStore store)
    {
        Query query = getSession().createQuery("from NewInAVMStoreImpl nie where nie.avmStore = :store");
        query.setEntity("store", store);
        return (List<NewInAVMStore>)query.list();
    }
    
    /**
     * Delete the given entry.
     * @param newEntry The entry to delete.
     */
    public void delete(NewInAVMStore newEntry)
    {
        getSession().delete(newEntry);
    }
}

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
import org.alfresco.repo.avm.NewInRepository;
import org.alfresco.repo.avm.NewInRepositoryDAO;
import org.alfresco.repo.avm.Repository;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Hibernate implementation of NewInRepository DAO.
 * @author britt
 */
public class NewInRepositoryDAOHibernate extends HibernateDaoSupport implements
        NewInRepositoryDAO
{
    /**
     * Save one.
     * @param newEntry The item to save.
     */
    public void save(NewInRepository newEntry)
    {
        getSession().save(newEntry);
    }
    
    /**
     * Get one by Node.
     * @param node The node to lookup with.
     * @return The Entry or null if not found.
     */
    public NewInRepository getByNode(AVMNode node)
    {
        Query query = getSession().createQuery("from NewInRepositoryImpl nie where nie.node = :node");
        query.setEntity("node", node);
        return (NewInRepository)query.uniqueResult();
    }

    /**
     * Get all that are in the given repository.
     * @param repository The Repository.
     * @return A List of NewInRepositorys.
     */
    @SuppressWarnings("unchecked")
    public List<NewInRepository> getByRepository(Repository repository)
    {
        Query query = getSession().createQuery("from NewInRepositoryImpl nie where nie.repository = :rep");
        query.setEntity("rep", repository);
        return (List<NewInRepository>)query.list();
    }
    
    /**
     * Delete the given entry.
     * @param newEntry The entry to delete.
     */
    public void delete(NewInRepository newEntry)
    {
        getSession().delete(newEntry);
    }
}

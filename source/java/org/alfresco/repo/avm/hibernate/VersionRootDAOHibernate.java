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

import java.util.Date;
import java.util.List;

import org.alfresco.repo.avm.Repository;
import org.alfresco.repo.avm.VersionRoot;
import org.alfresco.repo.avm.VersionRootDAO;
import org.hibernate.Query;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * This is the Hibernate version of the DAO for version roots.
 * @author britt
 */
public class VersionRootDAOHibernate extends HibernateDaoSupport implements
        VersionRootDAO
{
    /**
     * Do nothing constructor.
     */
    public VersionRootDAOHibernate()
    {
        super();
    }
    
    /**
     * Save an unsaved VersionRoot.
     * @param vr The VersionRoot to save.
     */
    public void save(VersionRoot vr)
    {
        getSession().save(vr);
    }
    
    /**
     * Delete a VersionRoot.
     * @param vr The VersionRoot to delete.
     */
    public void delete(VersionRoot vr)
    {
        getSession().delete(vr);
        getSession().flush();
    }
    
    /**
     * Get all the version roots in a given repository.
     * @param rep The repository.
     * @return A List of VersionRoots.  In id order.
     */
    @SuppressWarnings("unchecked")
    public List<VersionRoot> getAllInRepository(Repository rep)
    {
        Query query = getSession().createQuery("from VersionRootImpl v where v.repository = :rep order by v.versionID");
        query.setEntity("rep", rep);
        return (List<VersionRoot>)query.list();
    }
    
    /**
     * Get the version of a repository by dates.
     * @param rep The repository.
     * @param from The starting date.  May be null but not with to null also.
     * @param to The ending date.  May be null but not with from null also.
     * @return A List of VersionRoots.
     */
    @SuppressWarnings("unchecked")
    public List<VersionRoot> getByDates(Repository rep, Date from, Date to)
    {
        Query query;
        if (from == null)
        {
            query = 
                getSession().createQuery("from VersionRootImpl vr where vr.createDate <= :to " +
                                         "and vr.repository = :rep " +
                                         "order by vr.versionID");
            query.setLong("to", to.getTime());
            query.setEntity("rep", rep);
        }
        else if (to == null)
        {
            query =
                getSession().createQuery("from VersionRootImpl vr " +
                                         "where vr.createDate >= :from " +
                                         "and vr.repository = :rep " +
                                         "order by vr.versionID");
            query.setLong("from", from.getTime());
            query.setEntity("rep", rep);
        }
        else
        {
            query =
                getSession().createQuery("from VersionRootImpl vr "+ 
                                         "where vr.createDate between :from and :to " +
                                         "and vr.repository = :rep " +
                                         "order by vr.versionID");
            query.setLong("from", from.getTime());
            query.setLong("to", to.getTime());
            query.setEntity("rep", rep);
        }
        query.setEntity("rep", rep);
        return (List<VersionRoot>)query.list();
    }
    
    /**
     * Get the VersionRoot corresponding to the given id.
     * @param rep The repository
     * @param id The version id.
     * @return The VersionRoot or null if not found.
     */
    public VersionRoot getByVersionID(Repository rep, int id)
    {
        Query query = getSession().getNamedQuery("VersionRoot.VersionByID");
        query.setEntity("rep", rep);
        query.setInteger("version", id);
        return (VersionRoot)query.uniqueResult();
    }
    
    /**
     * Get the highest numbered version in a repository.
     * @param rep The repository.
     * @return The highest numbered version.
     */
    public VersionRoot getMaxVersion(Repository rep)
    {
        Query query = getSession().createQuery("from VersionRootImpl vr " +
                                               "where vr.versionID = " +
                                               "(select max(v.versionID) from VersionRootImpl v)");
        return (VersionRoot)query.uniqueResult();
    }
}

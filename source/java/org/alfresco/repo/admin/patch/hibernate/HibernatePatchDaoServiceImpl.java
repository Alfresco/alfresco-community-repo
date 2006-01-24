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
package org.alfresco.repo.admin.patch.hibernate;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.repo.admin.patch.PatchDaoService;
import org.alfresco.repo.domain.AppliedPatch;
import org.alfresco.repo.domain.hibernate.AppliedPatchImpl;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * Hibernate-specific implementation for managing patch persistence.
 * 
 * @since 1.2
 * @author Derek Hulley
 */
public class HibernatePatchDaoServiceImpl extends HibernateDaoSupport implements PatchDaoService
{
    public static final String QUERY_GET_ALL_APPLIED_PATCHES = "patch.GetAllAppliedPatches";
    public static final String QUERY_GET_APPLIED_PATCHES_BY_DATE = "patch.GetAppliedPatchesByDate";

    public AppliedPatch newAppliedPatch(String id)
    {
        // check for existence
        AppliedPatch existing = getAppliedPatch(id);
        if (existing != null)
        {
            throw new AlfrescoRuntimeException("An applied patch already exists: \n" +
                    "   id: " + id);
        }
        // construct a new one
        AppliedPatchImpl patch = new AppliedPatchImpl();
        patch.setId(id);
        // save this in hibernate
        getHibernateTemplate().save(patch);
        // done
        return patch;
    }

    public AppliedPatch getAppliedPatch(String id)
    {
        AppliedPatch patch = (AppliedPatch) getHibernateTemplate().get(AppliedPatchImpl.class, id);
        // done
        return patch;
    }

    public void detach(AppliedPatch appliedPatch)
    {
        getSession().evict(appliedPatch);
    }

    /**
     * @see #QUERY_GET_ALL_APPLIED_PATCHES
     */
    @SuppressWarnings("unchecked")
    public List<AppliedPatch> getAppliedPatches()
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(HibernatePatchDaoServiceImpl.QUERY_GET_ALL_APPLIED_PATCHES);
                return query.list();
            }
        };
        List<AppliedPatch> queryResults = (List) getHibernateTemplate().execute(callback);
        // done
        return queryResults;
    }

    /**
     * @see #QUERY_GET_APPLIED_PATCHES_BY_DATE
     */
    @SuppressWarnings("unchecked")
    public List<AppliedPatch> getAppliedPatches(final Date fromDate, final Date toDate)
    {
        HibernateCallback callback = new HibernateCallback()
        {
            public Object doInHibernate(Session session)
            {
                Query query = session.getNamedQuery(HibernatePatchDaoServiceImpl.QUERY_GET_ALL_APPLIED_PATCHES);
                return query.list();
            }
        };
        List<AppliedPatch> queryResults = (List) getHibernateTemplate().execute(callback);
        // eliminate results that are out of range
        Iterator<AppliedPatch> iterator = queryResults.iterator();
        while (iterator.hasNext())
        {
            AppliedPatch appliedPatch = iterator.next();
            Date appliedOnDate = appliedPatch.getAppliedOnDate();
            if (fromDate.compareTo(appliedOnDate) >= 0 || toDate.compareTo(appliedOnDate) <= 0)
            {
                // it is out of range
                iterator.remove();
            }
        }
        // done
        return queryResults;
    }
}

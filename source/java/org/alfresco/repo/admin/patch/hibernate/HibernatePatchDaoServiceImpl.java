/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
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
            if (appliedOnDate != null && fromDate.compareTo(appliedOnDate) >= 0 || toDate.compareTo(appliedOnDate) <= 0)
            {
                // it is out of range
                iterator.remove();
            }
        }
        // done
        return queryResults;
    }
}

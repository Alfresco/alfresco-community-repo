/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
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
 */
package org.alfresco.repo.domain.patch;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.alfresco.repo.admin.patch.AppliedPatch;

/**
 * Abstract implementation for DAO <b>alf_applied_patch</b>.
 * 
 * @author Derek Hulley
 * @since 3.4
 */
public abstract class AbstractAppliedPatchDAOImpl implements AppliedPatchDAO
{
    public void createAppliedPatch(AppliedPatch appliedPatch)
    {
        AppliedPatchEntity entity = new AppliedPatchEntity(appliedPatch);
        createAppliedPatchEntity(entity);
    }

    public void updateAppliedPatch(AppliedPatch appliedPatch)
    {
        AppliedPatchEntity entity = new AppliedPatchEntity(appliedPatch);
        updateAppliedPatchEntity(entity);
    }

    public AppliedPatch getAppliedPatch(String id)
    {
        return getAppliedPatchEntity(id);
    }

    public List<AppliedPatch> getAppliedPatches()
    {
        List<AppliedPatchEntity> entities = getAppliedPatchEntities();
        List<AppliedPatch> results = new ArrayList<AppliedPatch>();
        results.addAll(entities);
        return results;
    }

    public List<AppliedPatch> getAppliedPatches(Date from, Date to)
    {
        // Manual filter (no performance required)
        List<AppliedPatch> results = getAppliedPatches();
        Iterator<AppliedPatch> iterator = results.iterator();
        while (iterator.hasNext())
        {
            AppliedPatch next = iterator.next();
            Date appliedOn = next.getAppliedOnDate();
            if (from != null && appliedOn != null && from.compareTo(appliedOn) >= 0)
            {
                iterator.remove();
                continue;
            }
            if (to != null && appliedOn != null && to.compareTo(appliedOn) <= 0)
            {
                iterator.remove();
                continue;
            }
        }
        return results;
    }

    public void setAppliedOnDate(String id, Date appliedOnDate)
    {
        throw new UnsupportedOperationException();
    }
    
    protected abstract void createAppliedPatchEntity(AppliedPatchEntity entity);
    protected abstract void updateAppliedPatchEntity(AppliedPatchEntity appliedPatch);
    protected abstract AppliedPatchEntity getAppliedPatchEntity(String id);
    protected abstract List<AppliedPatchEntity> getAppliedPatchEntities();
}

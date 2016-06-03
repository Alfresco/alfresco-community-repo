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

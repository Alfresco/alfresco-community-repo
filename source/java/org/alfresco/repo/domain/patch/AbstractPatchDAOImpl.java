package org.alfresco.repo.domain.patch;

import org.alfresco.ibatis.BatchingDAO;

/**
 * Abstract implementation for Patch DAO.
 * <p>
 * This provides additional queries used by patches.
 * 
 * @author Derek Hulley
 * @author janv
 * @since 3.2
 */
public abstract class AbstractPatchDAOImpl implements PatchDAO, BatchingDAO
{
    protected AbstractPatchDAOImpl()
    {
    }
}

package org.alfresco.util.schemacomp;

import org.springframework.extensions.surf.util.I18NUtil;



/**
 * Result of a comparison between two database objects.
 * 
 * @author Matt Ward
 */
public final class Difference extends Result
{
    /** Specifies the type of differences */
    public enum Where { ONLY_IN_REFERENCE, ONLY_IN_TARGET, IN_BOTH_NO_DIFFERENCE, IN_BOTH_BUT_DIFFERENCE };
    private final Where where;
    private final DbProperty left;
    private final DbProperty right;
    
    
    public Difference(Where where, DbProperty left, DbProperty right)
    {
        // Sanity check parameters
        if (left == null && right == null)
        {
            throw new IllegalArgumentException("DbProperty parameters cannot BOTH be null.");
        }
        
        this.where = where;
        this.left = left;
        this.right = right;
    }

    /**
     * @return the where
     */
    public Where getWhere()
    {
        return this.where;
    }

    /**
     * @return the left
     */
    public DbProperty getLeft()
    {
        return this.left;
    }

    /**
     * @return the right
     */
    public DbProperty getRight()
    {
        return this.right;
    }

    
    @Override
    public String describe()
    {
        if (getLeft() == null)
        {
            return I18NUtil.getMessage(
                        "system.schema_comp.diff.target_only",
                        getRight().getDbObject().getTypeName(),
                        getRight().getPath(),
                        getRight().getPropertyValue());
        }
        if (getRight() == null)
        {
            return I18NUtil.getMessage(
                        "system.schema_comp.diff.ref_only",
                        getLeft().getDbObject().getTypeName(),
                        getLeft().getPath(),
                        getLeft().getPropertyValue());
        }
        
        return I18NUtil.getMessage(
                    "system.schema_comp.diff",
                    getLeft().getDbObject().getTypeName(),
                    getLeft().getPath(),
                    getLeft().getPropertyValue(),
                    getRight().getPath(),
                    getRight().getPropertyValue());
    }

    @Override
    public String toString()
    {
        return "Difference [where=" + this.where + ", left=" + this.left + ", right=" + this.right + "]";
    }
}

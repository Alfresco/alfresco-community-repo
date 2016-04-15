package org.alfresco.repo.security.permissions;

/**
 * The ACE Type
 * @author andyh
 *
 */
public enum ACEType
{
    /**
     * ACE applies to the object and its children
     */
    ALL
    {
        public int getId()
        {
            return 0;
        }
    },
    /**
     * ACE applies to the object only
     */
    OBJECT
    {
        public int getId()
        {
            return 1;
        }
    },
    /**
     * ACE only applies to children
     */
    CHILDREN
    {
        public int getId()
        {
            return 2;
        }
    };
    
    /**
     * Get the id for the ACEType stored in the DB.
     * @return int
     */
    public abstract int getId();
    
    
    /**
     * Get the ACEType from the value stored in the DB.
     * @param id int
     * @return ACEType
     */
    public static ACEType getACETypeFromId(int id)
    {
        switch(id)
        {
        case 0:
            return ACEType.ALL;
        case 1:
            return ACEType.OBJECT;
        case 2:
            return ACEType.CHILDREN;
        default:
            throw new IllegalArgumentException("Unknown ace type "+id);
        }
    }
}

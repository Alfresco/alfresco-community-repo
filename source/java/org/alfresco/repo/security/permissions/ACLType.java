package org.alfresco.repo.security.permissions;

/**
 * The ACL Type
 * 
 * @author andyh
 *
 */
public enum ACLType
{
    /**
     * Old style permissions that require a parent wlak to resolve
     */
    OLD
    {
        public int getId()
        {
            return 0;
        }
    },
    
    /**
     * Defining permission - not reused anywhere
     */
    DEFINING
    {
        public int getId()
        {
            return 1;
        }
    },
    
    /**
     * Shared permission, reused for inhertiance from defining permission
     */
    SHARED
    {
        public int getId()
        {
            return 2;
        }
    },
    
    /**
     * An ACL defined in its own right - there is no inheriance context
     * 
     */
    FIXED
    {
        public int getId()
        {
            return 3;
        }
    },
    
    /**
     * A single instance for global permissions
     */
    GLOBAL
    {
        public int getId()
        {
            return 4;
        }
    },
    
    /**
     * Layered types
     */
    LAYERED
    {
        public int getId()
        {
            return 5;
        }
    };
    
    
    /**
     * Get the id for the ACLType stored in the DB
     * 
     * @return int
     */
    public abstract int getId();
    
    /**
     * Get the ACLType from the value stored in the DB
     * @param id int
     * @return ACLType
     */
    public static ACLType getACLTypeFromId(int id)
    {
        switch(id)
        {
        case 0:
            return ACLType.OLD;
        case 1:
            return ACLType.DEFINING;
        case 2:
            return ACLType.SHARED;
        case 3:
            return ACLType.FIXED;
        case 4:
            return ACLType.GLOBAL;
        case 5:
            return ACLType.LAYERED;
        default:
            throw new IllegalArgumentException("Unknown acl type "+id);
        }
    }
}

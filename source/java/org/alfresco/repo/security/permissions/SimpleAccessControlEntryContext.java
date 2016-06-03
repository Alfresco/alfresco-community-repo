
package org.alfresco.repo.security.permissions;

public class SimpleAccessControlEntryContext implements AccessControlEntryContext
{
    /**
     * 
     */
    private static final long serialVersionUID = -5679179194140822827L;

    private String classContext;
    
    private String KVPContext;

    private String propertyContext;
    
    public String getClassContext()
    {
        return classContext;
    }

    public String getKVPContext()
    {
        return KVPContext;
    }

    public String getPropertyContext()
    {
        return propertyContext;
    }

    public void setClassContext(String classContext)
    {
        this.classContext = classContext;
    }

    public void setKVPContext(String context)
    {
        KVPContext = context;
    }

    public void setPropertyContext(String propertyContext)
    {
        this.propertyContext = propertyContext;
    }
    

}

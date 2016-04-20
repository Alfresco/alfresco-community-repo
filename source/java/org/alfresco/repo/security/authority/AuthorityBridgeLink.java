package org.alfresco.repo.security.authority;

/**
 * @author Andy
 *
 */
public class AuthorityBridgeLink
{
    private String childName;
    
    private String parentName;

    /**
     * @return the childName
     */
    public String getChildName()
    {
        return childName;
    }

    /**
     * @param childName the childName to set
     */
    public void setChildName(String childName)
    {
        this.childName = childName;
    }

    /**
     * @return the parentName
     */
    public String getParentName()
    {
        return parentName;
    }

    /**
     * @param parentName the parentName to set
     */
    public void setParentName(String parentName)
    {
        this.parentName = parentName;
    }
    
    
    
}

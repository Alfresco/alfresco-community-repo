package org.alfresco.module.org_alfresco_module_rm.caveat;

import java.io.Serializable;


public class ScriptAuthority implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private String authorityTitle;
    private String authorityName;
    
    public void setAuthorityName(String authorityName)
    {
        this.authorityName = authorityName;
    }
    public String getAuthorityName()
    {
        return authorityName;
    }
    public void setAuthorityTitle(String authorityName)
    {
        this.authorityTitle = authorityName;
    }
    public String getAuthorityTitle()
    {
        return authorityTitle;
    }

                                     
}

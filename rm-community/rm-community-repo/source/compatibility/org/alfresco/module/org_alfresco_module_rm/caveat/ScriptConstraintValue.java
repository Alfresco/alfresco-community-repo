 
package org.alfresco.module.org_alfresco_module_rm.caveat;

import java.util.List;
import java.io.Serializable;

public class ScriptConstraintValue implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = -4659454215122271811L;
    private String value;
    private List<ScriptAuthority>authorities;
    
    public void setAuthorities(List<ScriptAuthority> values)
    {
        this.authorities = values;
    }
    public List<ScriptAuthority> getAuthorities()
    {
        return authorities;
    }
    public void setValueName(String authorityName)
    {
        this.value = authorityName;
    }
    public String getValueName()
    {
        return value;
    }
    public void setValueTitle(String authorityName)
    {
        this.value = authorityName;
    }
    public String getValueTitle()
    {
        return value;
    }
}

package org.alfresco.module.org_alfresco_module_rm.caveat;

import java.io.Serializable;
import java.util.List;

public class ScriptConstraintAuthority implements Serializable
{
    /**
     * 
     */
    private static final long serialVersionUID = -4659454215122271811L;
    private String authorityName;
    private List<String>values;
    
    public void setValues(List<String> values)
    {
        this.values = values;
    }
    public List<String> getValues()
    {
        return values;
    }
    public void setAuthorityName(String authorityName)
    {
        this.authorityName = authorityName;
    }
    public String getAuthorityName()
    {
        return authorityName;
    }
}

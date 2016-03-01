 
package org.alfresco.module.org_alfresco_module_rm.caveat;

import java.util.Arrays;

public class RMConstraintInfo
{
    private String name;
    private String title;
    private boolean caseSensitive;
    private String[] allowedValues;

    public void setName(String name)
    {
        this.name = name;
    }
    public String getName()
    {
        return name;
    }
    public void setTitle(String title)
    {
        this.title = title;
    }
    public String getTitle()
    {
        return title;
    }
    public void setCaseSensitive(boolean caseSensitive)
    {
        this.caseSensitive = caseSensitive;
    }
    public boolean isCaseSensitive()
    {
        return caseSensitive;
    }
    public void setAllowedValues(String[] values)
    {
        this.allowedValues = values.clone();
    }
    public String[] getAllowedValues()
    {
        return allowedValues;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("RMConstraintInfo [name=");
        builder.append(this.name);
        builder.append(", title=");
        builder.append(this.title);
        builder.append(", caseSensitive=");
        builder.append(this.caseSensitive);
        builder.append(", allowedValues=");
        builder.append(Arrays.toString(this.allowedValues));
        builder.append("]");
        return builder.toString();
    }

}

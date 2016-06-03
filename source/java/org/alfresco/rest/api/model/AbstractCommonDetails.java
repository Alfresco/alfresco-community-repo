
package org.alfresco.rest.api.model;

/**
 * @author Jamal Kaabi-Mofrad
 */
public abstract class AbstractCommonDetails implements Comparable<AbstractCommonDetails>
{
    /* package */String name;
    /* package */String prefixedName;
    /* package */String title;
    /* package */String description;

    public String getName()
    {
        return this.name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getPrefixedName()
    {
        return this.prefixedName;
    }

    public String getTitle()
    {
        return this.title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    public String getDescription()
    {
        return this.description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (!(obj instanceof CustomModelConstraint))
        {
            return false;
        }
        CustomModelConstraint other = (CustomModelConstraint) obj;
        if (this.name == null)
        {
            if (other.name != null)
            {
                return false;
            }
        }
        else if (!this.name.equals(other.name))
        {
            return false;
        }
        return true;
    }

    @Override
    public int compareTo(AbstractCommonDetails other)
    {
        return this.name.compareTo(other.getName());
    }
}

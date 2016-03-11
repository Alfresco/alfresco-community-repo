package org.alfresco.module.org_alfresco_module_rm.email;

/**
 * Custom EMail Mapping
 */
public class CustomMapping
{
    private String from;
    private String to;

    /**
     * Default constructor.
     */
    public CustomMapping()
    {
    }

    /**
     * Default constructor.
     * @param from
     * @param to
     */
    public CustomMapping(String from, String to)
    {
        this.from = from;
        this.to = to;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }

    public String getFrom()
    {
        return from;
    }

    public void setTo(String to)
    {
        this.to = to;
    }

    public String getTo()
    {
        return to;
    }

    public int hashCode()
    {
        if(from != null && to != null)
        {
            return (from + to).hashCode();
        }
        else
        {
            return 1;
        }
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
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
        if (getClass() != obj.getClass())
        {
            return false;
        }

        final CustomMapping other = (CustomMapping) obj;

        if (!from.equals(other.getFrom()))
        {
            return false;
        }
        if (!to.equals(other.getTo()))
        {
            return false;
        }
        return true;
    }
}

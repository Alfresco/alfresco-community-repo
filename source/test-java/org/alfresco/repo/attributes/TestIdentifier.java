package org.alfresco.repo.attributes;

import java.io.Serializable;

public class TestIdentifier implements Serializable
{
    public enum TestEnum
    {
        ONE, TWO
    }
    
    private static final long serialVersionUID = 1L;
    private String id;
    
    public TestIdentifier(String id)
    {
        this.id = id;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == null) return false;
        if (!(o instanceof TestIdentifier)) return false;
        TestIdentifier other = (TestIdentifier) o;
        if (!this.id.equals(other.id)) return false;
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 31 * hash + (null == id ? 0 : id.hashCode());
        return hash;
    }

}

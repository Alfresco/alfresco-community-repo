
package org.alfresco.repo.virtual.ref;

/**
 * Generic value of type <code>V</code> holder parameter.
 * 
 * @param <V>
 */
public abstract class ValueParameter<V> implements Parameter
{
    private V value;

    public ValueParameter(V value)
    {
        super();
        this.value = value;
    }

    public V getValue()
    {
        return value;
    }

    @Override
    public int hashCode()
    {
        return this.value != null ? this.value.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        else if (obj == null)
        {
            return false;
        }
        else if (!(getClass().equals(obj.getClass())))
        {
            return false;
        }

        if (obj instanceof ValueParameter<?>)
        {
            ValueParameter<?> other = (ValueParameter<?>) obj;

            if (value == null)
            {
                return other.value == null;
            }
            else
            {
                return this.value.equals(other.value);
            }
        }
        else
        {
            return false;
        }
    }
}

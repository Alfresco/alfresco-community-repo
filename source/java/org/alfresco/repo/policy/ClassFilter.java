package org.alfresco.repo.policy;

import org.alfresco.service.namespace.QName;

/**
 * ClassFilter object used to describe the BehaviourFilter for a class
 *
 * @author alex.mukha
 */
public class ClassFilter
{
    private QName className;
    private boolean disableSubClasses;

    public ClassFilter(QName className, boolean disableSubClasses)
    {
        this.className = className;
        this.disableSubClasses = disableSubClasses;
    }

    public QName getClassName()
    {
        return className;
    }

    public boolean isDisableSubClasses()
    {
        return disableSubClasses;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ClassFilter that = (ClassFilter) o;

        return !(className != null ? !className.equals(that.className) : that.className != null);

    }

    @Override
    public int hashCode()
    {
        return className != null ? className.hashCode() : 0;
    }

    @Override
    public String toString()
    {
        return "ClassFilter{" +
                "className=" + className +
                ", disableSubClasses=" + disableSubClasses +
                '}';
    }
}
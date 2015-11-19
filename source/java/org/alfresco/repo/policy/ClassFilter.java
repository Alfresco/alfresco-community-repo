/*
 * Copyright (C) 2005-2015 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
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
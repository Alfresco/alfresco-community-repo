/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.api.model;

import java.util.Objects;

public class Category
{
    private String id;
    private String name;
    private String parentId;
    private boolean hasChildren;
    private Integer count;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public void setCategoryId(String categoryId)
    {
        this.id = categoryId;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getParentId()
    {
        return parentId;
    }

    public void setParentId(String parentId)
    {
        this.parentId = parentId;
    }

    public boolean getHasChildren()
    {
        return hasChildren;
    }

    public void setHasChildren(boolean hasChildren)
    {
        this.hasChildren = hasChildren;
    }

    public Integer getCount()
    {
        return count;
    }

    public void setCount(Integer count)
    {
        this.count = count;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Category category = (Category) o;
        return hasChildren == category.hasChildren && Objects.equals(id, category.id) && Objects.equals(name, category.name) && Objects.equals(parentId, category.parentId)
            && Objects.equals(count, category.count);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, name, parentId, hasChildren, count);
    }

    @Override
    public String toString()
    {
        return "Category{" + "id='" + id + '\'' + ", name='" + name + '\'' + ", parentId='" + parentId + '\'' + ", hasChildren=" + hasChildren + ", count=" + count + '}';
    }

    public static Builder builder()
    {
        return new Builder();
    }

    public static class Builder
    {
        private String id;
        private String name;
        private String parentId;
        private boolean hasChildren;
        private Integer count;

        public Builder id(String id)
        {
            this.id = id;
            return this;
        }

        public Builder name(String name)
        {
            this.name = name;
            return this;
        }

        public Builder parentId(String parentId)
        {
            this.parentId = parentId;
            return this;
        }

        public Builder hasChildren(boolean hasChildren)
        {
            this.hasChildren = hasChildren;
            return this;
        }

        public Builder count(Integer count)
        {
            this.count = count;
            return this;
        }

        public Category create()
        {
            final Category category = new Category();
            category.setId(id);
            category.setName(name);
            category.setParentId(parentId);
            category.setHasChildren(hasChildren);
            category.setCount(count);
            return category;
        }
    }
}

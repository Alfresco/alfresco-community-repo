/*
 * #%L
 * Alfresco Remote API
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.rest.api.model;

import org.alfresco.service.cmr.dictionary.ModelDefinition;
import org.alfresco.service.cmr.dictionary.NamespaceDefinition;
import org.alfresco.service.cmr.i18n.MessageLookup;
import org.alfresco.service.namespace.QName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractClass extends ClassDefinition implements Comparable<AbstractClass>
{
    String id;
    String title;
    String description;
    String parentId;
    Model model;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
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

    public String getParentId()
    {
        return parentId;
    }

    public void setParentId(String parentId)
    {
        this.parentId = parentId;
    }

    public Model getModel()
    {
        return model;
    }

    public void setModel(Model model)
    {
        this.model = model;
    }

    <T> List<T> setList(List<T> sourceList)
    {
        if (sourceList == null)
        {
            return Collections.<T> emptyList();
        }
        return new ArrayList<>(sourceList);
    }

    String getParentNameAsString(QName parentQName)
    {
        if (parentQName != null)
        {
            return parentQName.toPrefixString();
        }
        return null;
    }

    Model getModelInfo(org.alfresco.service.cmr.dictionary.ClassDefinition classDefinition, MessageLookup messageLookup)
    {
        final ModelDefinition modelDefinition  = classDefinition.getModel();
        final String prefix = classDefinition.getName().toPrefixString().split(":")[0];

        final NamespaceDefinition namespaceDefinition = modelDefinition.getNamespaces().stream()
                .filter(definition -> definition.getPrefix().equals(prefix))
                .findFirst()
                .get();

        final String modelId = modelDefinition.getName().toPrefixString();
        final String author = modelDefinition.getAuthor();
        final String description = modelDefinition.getDescription(messageLookup);

        return new Model(modelId, author, description, namespaceDefinition.getUri(), namespaceDefinition.getPrefix());
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        return super.equals(obj);
    }

    @Override
    public int compareTo(AbstractClass other)
    {
        return this.id.compareTo(other.getId());
    }
}

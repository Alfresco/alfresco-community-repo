/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.opencmis.dictionary;

import java.io.Serializable;

import org.alfresco.cmis.CMISPropertyAccessor;
import org.alfresco.cmis.CMISPropertyLuceneBuilder;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;

public class BasePropertyDefintionWrapper implements PropertyDefintionWrapper, Serializable
{
    private static final long serialVersionUID = 1L;

    private PropertyDefinition<?> propDef;
    private QName alfrescoName;
    private TypeDefinitionWrapper owningType;
    private CMISPropertyAccessor accessor;
    private CMISPropertyLuceneBuilder luceneBuilder;

    public BasePropertyDefintionWrapper(PropertyDefinition<?> propDef, QName alfrescoName,
            TypeDefinitionWrapper owningType, CMISPropertyAccessor accessor, CMISPropertyLuceneBuilder luceneBuilder)
    {
        this.propDef = propDef;
        this.alfrescoName = alfrescoName;
        this.owningType = owningType;
        this.accessor = accessor;
        this.luceneBuilder = luceneBuilder;
    }

    @Override
    public PropertyDefinition<?> getPropertyDefinition()
    {
        return propDef;
    }

    @Override
    public String getPropertyId()
    {
        return propDef.getId();
    }

    @Override
    public QName getAlfrescoName()
    {
        return alfrescoName;
    }

    @Override
    public TypeDefinitionWrapper getOwningType()
    {
        return owningType;
    }

    @Override
    public CMISPropertyAccessor getPropertyAccessor()
    {
        return accessor;
    }

    @Override
    public CMISPropertyLuceneBuilder getPropertyLuceneBuilder()
    {
        return luceneBuilder;
    }

}

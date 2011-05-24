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
package org.alfresco.opencmis.search;

import org.alfresco.opencmis.dictionary.TypeDefinitionWrapper;
import org.alfresco.service.cmr.search.ResultSetSelector;
import org.alfresco.service.namespace.QName;

/**
 * @author andyh
 * 
 */
public class CMISResultSetSelector implements ResultSetSelector
{
    private String name;

    private TypeDefinitionWrapper typeDefinition;

    public CMISResultSetSelector(String name, TypeDefinitionWrapper typeDefinition)
    {
        this.name = name;
        this.typeDefinition = typeDefinition;
    }

    public String getName()
    {
        return name;
    }

    public TypeDefinitionWrapper getTypeDefinition()
    {
        return typeDefinition;
    }

    public QName getType()
    {
        return typeDefinition.getAlfrescoName();
    }

}

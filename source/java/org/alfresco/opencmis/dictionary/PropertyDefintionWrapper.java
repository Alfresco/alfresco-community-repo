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

import org.alfresco.cmis.CMISPropertyAccessor;
import org.alfresco.cmis.CMISPropertyLuceneBuilder;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.definitions.PropertyDefinition;

public interface PropertyDefintionWrapper
{
    PropertyDefinition<?> getPropertyDefinition();

    String getPropertyId();

    QName getAlfrescoName();

    TypeDefinitionWrapper getOwningType();

    CMISPropertyAccessor getPropertyAccessor();

    CMISPropertyLuceneBuilder getPropertyLuceneBuilder();
}

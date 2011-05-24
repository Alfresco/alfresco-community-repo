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

import org.alfresco.opencmis.CMISUtils;
import org.alfresco.opencmis.mapping.CMISMapping;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.opencmis.commons.enums.BaseTypeId;
import org.apache.chemistry.opencmis.commons.enums.ContentStreamAllowed;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.DocumentTypeDefinitionImpl;

public class DocumentTypeDefinitionWrapper extends ShadowTypeDefinitionWrapper
{
    private static final long serialVersionUID = 1L;

    private DocumentTypeDefinitionImpl typeDef;
    private DocumentTypeDefinitionImpl typeDefInclProperties;

    public DocumentTypeDefinitionWrapper(CMISMapping cmisMapping, ServiceRegistry serviceRegistry, String typeId,
            ClassDefinition cmisClassDef)
    {
        alfrescoName = cmisClassDef.getName();
        alfrescoClass = cmisMapping.getAlfrescoClass(alfrescoName);

        typeDef = new DocumentTypeDefinitionImpl();

        typeDef.setBaseTypeId(BaseTypeId.CMIS_DOCUMENT);
        typeDef.setId(typeId);
        typeDef.setLocalName(alfrescoName.getLocalName());
        typeDef.setLocalNamespace(alfrescoName.getNamespaceURI());

        if (BaseTypeId.CMIS_DOCUMENT.value().equals(typeId))
        {
            typeDef.setQueryName(typeId);
            typeDef.setParentTypeId(null);
        } else
        {
            typeDef.setQueryName(cmisMapping.buildPrefixEncodedString(alfrescoName));
            QName parentQName = cmisMapping.getCmisType(cmisClassDef.getParentName());
            if (cmisMapping.isValidCmisDocument(parentQName))
            {
                typeDef.setParentTypeId(cmisMapping.getCmisTypeId(BaseTypeId.CMIS_DOCUMENT, parentQName));
            }
        }

        typeDef.setDisplayName((cmisClassDef.getTitle() != null) ? cmisClassDef.getTitle() : typeId);
        typeDef.setDescription(cmisClassDef.getDescription() != null ? cmisClassDef.getDescription() : typeDef
                .getDisplayName());

        typeDef.setIsCreatable(true);
        typeDef.setIsQueryable(true);
        typeDef.setIsFulltextIndexed(true);
        typeDef.setIsControllablePolicy(false);
        typeDef.setIsControllableAcl(true);
        typeDef.setIsIncludedInSupertypeQuery(cmisClassDef.getIncludedInSuperTypeQuery());
        typeDef.setIsFileable(true);
        typeDef.setContentStreamAllowed(ContentStreamAllowed.ALLOWED);
        typeDef.setIsVersionable(true);

        typeDefInclProperties = CMISUtils.copy(typeDef);
        setTypeDefinition(typeDef, typeDefInclProperties);

        createOwningPropertyDefinitions(cmisMapping, serviceRegistry, cmisClassDef);

        actionEvaluators = cmisMapping.getActionEvaluators(BaseTypeId.CMIS_DOCUMENT);
    }
}

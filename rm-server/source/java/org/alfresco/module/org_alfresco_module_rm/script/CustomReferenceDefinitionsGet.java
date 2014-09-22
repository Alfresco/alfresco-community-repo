/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.script;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Implementation for Java backed webscript to get RM custom reference definitions.
 *
 * @author Neil McErlean
 * @author Tuna Aksoy
 */
public class CustomReferenceDefinitionsGet extends CustomReferenceDefinitionBase
{
    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.Status, org.springframework.extensions.webscripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        String referenceId = getRequestParameterValue(req, REF_ID);
        Map<QName, AssociationDefinition> customReferenceDefinitions = getCustomReferenceDefinitions(referenceId);
        List<Map<String, String>> customReferenceData = getCustomReferenceData(customReferenceDefinitions);

        Map<String, Object> model = new HashMap<String, Object>();
        model.put(CUSTOM_REFS, customReferenceData);

        return model;
    }

    /**
     * Gets the custom reference definition(s) for the given reference id
     *
     * @param referenceId The reference id
     * @return If the reference id is not blank the custom definition for the given reference id will be returned,
     * otherwise all custom definitions will be returned.
     */
    private Map<QName, AssociationDefinition> getCustomReferenceDefinitions(String referenceId)
    {
        Map<QName, AssociationDefinition> customReferenceDefinitions = new HashMap<QName, AssociationDefinition>();

        if (StringUtils.isNotBlank(referenceId))
        {
            QName referenceQName = getCustomReferenceQName(referenceId);
            AssociationDefinition associationDefinition = getAssosiationDefinitionForCustomReference(referenceQName);
            customReferenceDefinitions.put(referenceQName, associationDefinition);
        }
        else
        {
            customReferenceDefinitions.putAll(getRmAdminService().getCustomReferenceDefinitions());
        }

        return customReferenceDefinitions;
    }

    /**
     * Gets the association definition for the given reference QName
     *
     * @param referenceQName The reference QName
     * @return The association definition for the given reference QName
     */
    private AssociationDefinition getAssosiationDefinitionForCustomReference(QName referenceQName)
    {
        AssociationDefinition associationDefinition = getRmAdminService().getCustomReferenceDefinitions().get(referenceQName);
        if (associationDefinition == null)
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Unable to find association definition for the reference: '");
            msg.append(referenceQName.getLocalName());
            msg.append("'.");
            String errorMsg = msg.toString();

            throw new WebScriptException(Status.STATUS_NOT_FOUND, errorMsg);
        }
        return associationDefinition;
    }

    /**
     * Gets the custom reference type from the association definition
     *
     * @param associationDefinition The association definition
     * @return Returns the custom reference type which is either parent/child or bidirectional
     */
    private CustomReferenceType getCustomReferenceType(AssociationDefinition associationDefinition)
    {
        CustomReferenceType referenceType;

        if (associationDefinition instanceof ChildAssociationDefinition)
        {
            referenceType = CustomReferenceType.PARENT_CHILD;
        }
        else if (associationDefinition instanceof AssociationDefinition)
        {
            referenceType = CustomReferenceType.BIDIRECTIONAL;
        }
        else
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Unsupported association definition: '");
            msg.append(associationDefinition.getName().getLocalName());
            msg.append("'.");
            String errorMsg = msg.toString();

            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, errorMsg);
        }

        return referenceType;
    }

    /**
     * Gets the custom reference data
     *
     * @param customReferenceDefinitions The custom reference definitions
     * @return Custom reference data
     */
    private List<Map<String, String>> getCustomReferenceData(Map<QName, AssociationDefinition> customReferenceDefinitions)
    {
        List<Map<String, String>> customReferences = new ArrayList<Map<String, String>>();

        for (Entry<QName, AssociationDefinition> entry : customReferenceDefinitions.entrySet())
        {
            Map<String, String> customReference = new HashMap<String, String>();
            AssociationDefinition associationDefinition = entry.getValue();
            CustomReferenceType referenceType = getCustomReferenceType(associationDefinition);
            String title = getAssociationDefinitionTitle(associationDefinition);

            if (CustomReferenceType.PARENT_CHILD.equals(referenceType))
            {
                String[] sourceAndTarget = getRmAdminService().splitSourceTargetId(title);
                customReference.put(SOURCE, sourceAndTarget[0]);
                customReference.put(TARGET, sourceAndTarget[1]);
            }
            else if (CustomReferenceType.BIDIRECTIONAL.equals(referenceType))
            {
                customReference.put(LABEL, title);
            }

            String referenceId = entry.getKey().getLocalName();
            customReference.put(REF_ID, referenceId);
            customReference.put(REFERENCE_TYPE, referenceType.toString());

            customReferences.add(customReference);
        }

        return customReferences;
    }

    /**
     * Gets the association definition title
     *
     * @param associationDefinition The association definition
     * @return The title of the association definition
     */
    private String getAssociationDefinitionTitle(AssociationDefinition associationDefinition)
    {
        String title = associationDefinition.getTitle(getDictionaryService());
        if (StringUtils.isBlank(title))
        {
            throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Association definition title is blank.");
        }
        return title;
    }
}
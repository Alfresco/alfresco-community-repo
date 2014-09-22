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

import org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Base class for custom reference definition classes
 *
 * @author Tuna Aksoy
 * @since 2.3
 */
public class CustomReferenceDefinitionBase extends AbstractRmWebScript
{
    /** Constants for the custom reference definition classes */
    protected static final String REFERENCE_TYPE = "referenceType";
    protected static final String REF_ID = "refId";
    protected static final String LABEL = "label";
    protected static final String SOURCE = "source";
    protected static final String TARGET = "target";
    protected static final String CUSTOM_REFS = "customRefs";
    protected static final String URL = "url";
    protected static final String SUCCESS = "success";

    /** Records Management Admin Service */
    private RecordsManagementAdminService rmAdminService;

    /** Dictionary Service */
    private DictionaryService dictionaryService;

    /**
     * Sets the records management admin service
     *
     * @param rmAdminService The records management admin service
     */
    public void setRecordsManagementAdminService(RecordsManagementAdminService rmAdminService)
    {
        this.rmAdminService = rmAdminService;
    }

    /**
     * Gets the records management admin service instance
     *
     * @return The records management admin service instance
     */
    protected RecordsManagementAdminService getRmAdminService()
    {
        return this.rmAdminService;
    }

    /**
     * Sets the dictionary service
     *
     * @param dictionaryService The dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Gets the dictionary service instance
     *
     * @return The dictionary service instance
     */
    protected DictionaryService getDictionaryService()
    {
        return this.dictionaryService;
    }

    /**
     * Gets the QName for the given custom reference id
     *
     * @param referenceId The reference id
     * @return The QName for the given custom reference id
     */
    protected QName getCustomReferenceQName(String referenceId)
    {
        QName customReferenceQName = getRmAdminService().getQNameForClientId(referenceId);
        if (customReferenceQName == null)
        {
            StringBuilder msg = new StringBuilder();
            msg.append("Unable to find QName for the reference: '");
            msg.append(referenceId);
            msg.append("'.");
            String errorMsg = msg.toString();

            throw new WebScriptException(Status.STATUS_NOT_FOUND, errorMsg);
        }
        return customReferenceQName;
    }

    /**
     * Gets the custom reference type from the json object
     *
     * @param requestContent The request content as json object
     * @return Returns the custom reference type which is either parent/child or bidirectional
     */
    protected CustomReferenceType getCustomReferenceType(JSONObject requestContent)
    {
        String referenceType = (String) getJSONObjectValue(requestContent, REFERENCE_TYPE);
        if (StringUtils.isBlank(referenceType))
        {
            throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Reference type is missing.");
        }
        return CustomReferenceType.getEnumFromString(referenceType);
    }

    /**
     * Gets the service path from the webscript request
     *
     * @param req The webscript request
     * @return The service path
     */
    protected String getServicePath(WebScriptRequest req)
    {
        return req.getServicePath();
    }
}

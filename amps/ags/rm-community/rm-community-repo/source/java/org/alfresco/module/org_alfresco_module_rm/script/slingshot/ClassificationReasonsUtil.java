/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.module.org_alfresco_module_rm.script.slingshot;

import static org.alfresco.model.ContentModel.PROP_NAME;
import static org.alfresco.service.namespace.QName.createQName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;

/**
 * Method to replace the plain text classification reason id with the correct nodeRef during record search
 * @author Ross Gale
 * @since 2.7
 */
public class ClassificationReasonsUtil extends SearchUtil
{

    public static final String CR_URI = "http://www.alfresco.org/model/securitymarks/1.0";
    public static final QName CLASSIFICATION_REASONS_CONTAINER = createQName(CR_URI,"classificationReasonsContainer");
    public static final QName PROP_CLASSIFICATION_REASON_CODE = createQName(CR_URI, "classificationReasonCode");
    public static final String REASONS_KEY = "clf:classificationReasons:";

    /**
     * Replace plain text reason id with nodeRef
     * @param searchQuery String e.g. clf:classificationReasons:1.4(a)
     * @return String e.g. clf:classificationReasons:5cc6d344-fa94-4370-9c81-d947b7e8f2ac
     */
    public String replaceReasonWithNodeRef(String searchQuery)
    {
        List<String> queries = new ArrayList<>(Arrays.asList(searchQuery.split(" ")));
        StringBuilder stringBuilder = new StringBuilder();
        for (String queryToEdit : queries)
        {
            if(queryToEdit.contains(REASONS_KEY))
            {
                for (String reasonId : retrieveAllNodeIds(getRootContainer(CLASSIFICATION_REASONS_CONTAINER)))
                {
                    NodeRef reasonNodeRef = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, reasonId);
                    Map<QName, Serializable> properties = nodeService.getProperties(reasonNodeRef);
                    if (queryToEdit.equals(REASONS_KEY + properties.get(PROP_CLASSIFICATION_REASON_CODE).toString()) ||
                            queryToEdit.equals(REASONS_KEY +"\""+ properties.get(PROP_CLASSIFICATION_REASON_CODE).toString() + "\""))
                    {
                        queryToEdit =  REASONS_KEY + properties.get(PROP_NAME).toString();
                        break;
                    }
                }
            }
            stringBuilder.append(queryToEdit).append(" ");
        }
        return stringBuilder.toString();
    }


}

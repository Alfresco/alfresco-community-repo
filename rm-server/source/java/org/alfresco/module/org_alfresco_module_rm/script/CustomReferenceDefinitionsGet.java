/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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

import javax.servlet.http.HttpServletResponse;

import org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ChildAssociationDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class provides the implementation for the customrefdefinitions.get webscript.
 *
 * @author Neil McErlean
 */
public class CustomReferenceDefinitionsGet extends DeclarativeWebScript
{
	private static final String REFERENCE_TYPE = "referenceType";
    private static final String REF_ID = "refId";
    private static final String LABEL = "label";
    private static final String SOURCE = "source";
    private static final String TARGET = "target";
    private static final String CUSTOM_REFS = "customRefs";
    private static Log logger = LogFactory.getLog(CustomReferenceDefinitionsGet.class);

    private RecordsManagementAdminService rmAdminService;
    private DictionaryService dictionaryService;

    public void setRecordsManagementAdminService(RecordsManagementAdminService rmAdminService)
    {
        this.rmAdminService = rmAdminService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    @Override
    public Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>();

        Map<String, String> templateVars = req.getServiceMatch().getTemplateVars();
        String refId = templateVars.get(REF_ID);


    	if (logger.isDebugEnabled())
    	{
    		logger.debug("Getting custom reference definitions with refId: " + String.valueOf(refId));
    	}

    	Map<QName, AssociationDefinition> currentCustomRefs = rmAdminService.getCustomReferenceDefinitions();

    	// If refId has been provided then this is a request for a single custom-ref-defn.
        // else it is a request for them all.
        if (refId != null)
        {
            QName qn = rmAdminService.getQNameForClientId(refId);

        	AssociationDefinition assDef = currentCustomRefs.get(qn);
        	if (assDef == null)
        	{
        		StringBuilder msg = new StringBuilder();
        		msg.append("Unable to find reference: ").append(refId);
        		if (logger.isDebugEnabled())
        		{
        		    logger.debug(msg.toString());
        		}
				throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND,
                		msg.toString());
        	}

        	currentCustomRefs = new HashMap<QName, AssociationDefinition>(1);
        	currentCustomRefs.put(qn, assDef);
        }

        List<Map<String, String>> listOfReferenceData = new ArrayList<Map<String, String>>();

        for (Entry<QName, AssociationDefinition> entry : currentCustomRefs.entrySet())
        {
    		Map<String, String> data = new HashMap<String, String>();

    		AssociationDefinition nextValue = entry.getValue();

    		CustomReferenceType referenceType = nextValue instanceof ChildAssociationDefinition ?
    				CustomReferenceType.PARENT_CHILD : CustomReferenceType.BIDIRECTIONAL;

			data.put(REFERENCE_TYPE, referenceType.toString());

			// It is the title which stores either the label, or the source and target.
			String nextTitle = nextValue.getTitle(dictionaryService);
            if (CustomReferenceType.PARENT_CHILD.equals(referenceType))
            {
                if (nextTitle != null)
                {
                    String[] sourceAndTarget = rmAdminService.splitSourceTargetId(nextTitle);
                    data.put(SOURCE, sourceAndTarget[0]);
                    data.put(TARGET, sourceAndTarget[1]);
                    data.put(REF_ID, entry.getKey().getLocalName());
                }
            }
            else if (CustomReferenceType.BIDIRECTIONAL.equals(referenceType))
            {
                if (nextTitle != null)
                {
                    data.put(LABEL, nextTitle);
                    data.put(REF_ID, entry.getKey().getLocalName());
                }
            }
            else
            {
                throw new WebScriptException("Unsupported custom reference type: " + referenceType);
            }

    		listOfReferenceData.add(data);
        }

    	if (logger.isDebugEnabled())
    	{
    		logger.debug("Retrieved custom reference definitions: " + listOfReferenceData.size());
    	}

    	model.put(CUSTOM_REFS, listOfReferenceData);

        return model;
    }
}
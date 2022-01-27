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

package org.alfresco.module.org_alfresco_module_rm.script;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.dod5015.DOD5015Model;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class provides the implementation for the dodcustomtypes.get webscript.
 *
 * @author Neil McErlean
 */
public class DodCustomTypesGet extends DeclarativeWebScript
{
    // TODO Investigate a way of not hard-coding the 4 custom types here.
    private static final List<QName> CUSTOM_TYPE_ASPECTS = Arrays.asList(new QName[]{DOD5015Model.ASPECT_SCANNED_RECORD,
            DOD5015Model.ASPECT_PDF_RECORD, DOD5015Model.ASPECT_DIGITAL_PHOTOGRAPH_RECORD, DOD5015Model.ASPECT_WEB_RECORD});

    private DictionaryService dictionaryService;

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    @Override
    public Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<>();

        List<AspectDefinition> customTypeAspectDefinitions = new ArrayList<>(4);
        for (QName aspectQName : CUSTOM_TYPE_ASPECTS)
        {
            AspectDefinition nextAspectDef = dictionaryService.getAspect(aspectQName);
            customTypeAspectDefinitions.add(nextAspectDef);
        }
    	model.put("dodCustomTypes", customTypeAspectDefinitions);

        return model;
    }
}

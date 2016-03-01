 
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
        Map<String, Object> model = new HashMap<String, Object>();

        List<AspectDefinition> customTypeAspectDefinitions = new ArrayList<AspectDefinition>(4);
        for (QName aspectQName : CUSTOM_TYPE_ASPECTS)
        {
            AspectDefinition nextAspectDef = dictionaryService.getAspect(aspectQName);
            customTypeAspectDefinitions.add(nextAspectDef);
        }
    	model.put("dodCustomTypes", customTypeAspectDefinitions);

        return model;
    }
}
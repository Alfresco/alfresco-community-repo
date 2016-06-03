package org.alfresco.repo.web.scripts.dictionary.prefixed;

import org.alfresco.repo.web.scripts.dictionary.AbstractPropertiesGet;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript to get the Propertydefinitions for a given classname eg. =>cm_person
 * 
 * @author Saravanan Sellathurai, Viachaslau Tsikhanovich
 */

public class PropertiesGet extends AbstractPropertiesGet
{
    private static final String DICTIONARY_PREFIX = "prefix";
    private static final String DICTIONARY_SHORT_CLASS_NAME = "shortClassName";
    
    @Override
    protected QName getClassQName(WebScriptRequest req)
    {
        QName classQName = null;
        String prefix = req.getServiceMatch().getTemplateVars().get(DICTIONARY_PREFIX);
        String shortName = req.getServiceMatch().getTemplateVars().get(DICTIONARY_SHORT_CLASS_NAME);
        if (prefix != null && prefix.length() != 0 && shortName != null && shortName.length()!= 0)
        {            
            classQName = createClassQName(prefix, shortName);
            if (classQName == null)
            {
                // Error 
                throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the className - " + prefix + ":" + shortName + " - parameter in the URL");
            }
        }
        return classQName;
    }

}
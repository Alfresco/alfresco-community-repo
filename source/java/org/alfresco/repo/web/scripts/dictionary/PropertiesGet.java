package org.alfresco.repo.web.scripts.dictionary;

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
    private static final String DICTIONARY_CLASS_NAME = "classname";
	
    @Override
    protected QName getClassQName(WebScriptRequest req)
    {
        QName classQName = null;
        String className = req.getServiceMatch().getTemplateVars().get(DICTIONARY_CLASS_NAME);
        if (className != null && className.length() != 0)
        {            
            classQName = createClassQName(className);
            if (classQName == null)
            {
                // Error 
                throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the className - " + className + " - parameter in the URL");
            }
        }
        return classQName;
    }
            
}

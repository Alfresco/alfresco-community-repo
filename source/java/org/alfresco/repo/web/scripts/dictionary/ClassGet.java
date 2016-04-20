package org.alfresco.repo.web.scripts.dictionary;

import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript to get the Classdefinitions for a classname eg. =>cm_author
 * @author Saravanan Sellathurai, Viachaslau Tsikhanovich
 */

public class ClassGet extends AbstractClassGet
{	
    private static final String DICTIONARY_CLASS_NAME = "className";
    
    /**
     * Override method from AbstractClassGet
     */
    @Override
    protected QName getClassQname(WebScriptRequest req)
    {
        String className = req.getServiceMatch().getTemplateVars().get(DICTIONARY_CLASS_NAME);
        //validate the classname and throw appropriate error message
        if (isValidClassname(className) == false)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the classname - " + className + " - parameter in the URL");
        }
        return QName.createQName(getFullNamespaceURI(className));
    }
    
}
package org.alfresco.repo.web.scripts.dictionary.prefixed;

import org.alfresco.repo.web.scripts.dictionary.AbstractClassGet;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript to get the Classdefinitions for a classname eg. =>cm_author
 * 
 * @author Saravanan Sellathurai, Viachaslau Tsikhanovich
 */

public class ClassGet extends AbstractClassGet
{   
    private static final String DICTIONARY_PREFIX = "prefix";
    private static final String DICTIONARY_SHORT_CLASS_NAME = "shortClassName";
    
    /**
     * Override method from AbstractClassGet
     */
    @Override
    protected QName getClassQname(WebScriptRequest req)
    {
        String prefix = req.getServiceMatch().getTemplateVars().get(DICTIONARY_PREFIX);
        String shortName = req.getServiceMatch().getTemplateVars().get(DICTIONARY_SHORT_CLASS_NAME);
        
        //validate the classname and throw appropriate error message
        if (isValidClassname(prefix, shortName) == false)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the classname - " + prefix + ":" + shortName + " - parameter in the URL");
        }
        return QName.createQName(getFullNamespaceURI(prefix, shortName));
    }
    
}
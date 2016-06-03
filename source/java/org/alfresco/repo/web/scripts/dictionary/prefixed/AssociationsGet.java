package org.alfresco.repo.web.scripts.dictionary.prefixed;

import org.alfresco.repo.web.scripts.dictionary.AbstractAssociationsGet;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript to get the Associationdefinitions for a given classname 
 * 
 * @author Saravanan Sellathurai, Viachaslau Tsikhanovich
 */

public class AssociationsGet extends AbstractAssociationsGet
{
    private static final String DICTIONARY_PREFIX = "prefix";
    private static final String DICTIONARY_SHORT_CLASS_NAME = "shortClassName";
    
    @Override
    protected QName getClassQname(WebScriptRequest req)
    {
        String prefix = req.getServiceMatch().getTemplateVars().get(DICTIONARY_PREFIX);
        String shortClassName = req.getServiceMatch().getTemplateVars().get(DICTIONARY_SHORT_CLASS_NAME);
        
        //validate classname
        if (isValidClassname(prefix, shortClassName) == false)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the classname - " + prefix + ":" + shortClassName + " - parameter in the URL");
        }
        return QName.createQName(getFullNamespaceURI(prefix, shortClassName));
    }
        
    @Override
    protected QName getAssociationQname(String namespacePrefix, String name)
    {
        return QName.createQName(getFullNamespaceURI(namespacePrefix, name));
    }
    
}
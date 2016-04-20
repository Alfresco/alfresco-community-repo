package org.alfresco.repo.web.scripts.dictionary;

import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript to get the Associationdefinitions for a given classname 
 * @author Saravanan Sellathurai, Viachaslau Tsikhanovich
 */

public class AssociationsGet extends AbstractAssociationsGet
{
    private static final String DICTIONARY_CLASS_NAME = "classname";
    
    @Override
    protected QName getClassQname(WebScriptRequest req)
    {
        String className = req.getServiceMatch().getTemplateVars().get(DICTIONARY_CLASS_NAME);
        //validate classname
        if (isValidClassname(className) == false)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the classname - " + className + " - parameter in the URL");
        }
        return QName.createQName(getFullNamespaceURI(className));
    }

    @Override
    protected QName getAssociationQname(String namespacePrefix, String name)
    {
        return QName.createQName(getFullNamespaceURI(namespacePrefix + "_" + name));
    }
    
}
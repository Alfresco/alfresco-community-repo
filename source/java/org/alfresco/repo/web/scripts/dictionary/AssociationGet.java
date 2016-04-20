package org.alfresco.repo.web.scripts.dictionary;

import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/*
 * Webscript to get the Associationdefinition for a given classname and association-name
 * @author Saravanan Sellathurai, Viachaslau Tsikhanovich
 */

public class AssociationGet extends AbstractAssociationGet
{
    private static final String DICTIONARY_CLASS_NAME = "classname";
    private static final String DICTIONARY_ASSOCIATION_NAME = "assocname";
    
    @Override
    protected QName getAssociationQname(WebScriptRequest req)
    {
        String associationName = req.getServiceMatch().getTemplateVars().get(DICTIONARY_ASSOCIATION_NAME);
        if(associationName == null)
        {
        	throw new WebScriptException(Status.STATUS_NOT_FOUND, "Missing parameter association name in the URL");
        }
        
        return QName.createQName(getFullNamespaceURI(associationName));
    }

    @Override
    protected QName getClassQname(WebScriptRequest req)
    {
        String className = req.getServiceMatch().getTemplateVars().get(DICTIONARY_CLASS_NAME);
        
        // validate the classname
        if (isValidClassname(className) == false)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the classname - " + className + " - parameter in the URL");
        }
		
        return QName.createQName(getFullNamespaceURI(className));
    }
    
}
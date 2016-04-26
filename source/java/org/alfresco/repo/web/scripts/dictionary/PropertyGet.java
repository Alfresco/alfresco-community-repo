package org.alfresco.repo.web.scripts.dictionary;

import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript to get the Propertydefinition for a given classname and propname
 * 
 * @author Saravanan Sellathurai, Viachaslau Tsikhanovich
 */

public class PropertyGet extends AbstractPropertyGet
{
    private static final String DICTIONARY_CLASS_NAME = "classname";
    private static final String DICTIONARY_PROPERTY_NAME = "propname";
    
    @Override
    protected QName getPropertyQname(WebScriptRequest req)
    {
        String propertyName = req.getServiceMatch().getTemplateVars().get(DICTIONARY_PROPERTY_NAME);
        //validate the presence of property name
        if(propertyName == null)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Missing parameter propertyname in the URL");
        }
        
        return QName.createQName(getFullNamespaceURI(propertyName));
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
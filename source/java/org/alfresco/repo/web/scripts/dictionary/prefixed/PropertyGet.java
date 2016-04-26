package org.alfresco.repo.web.scripts.dictionary.prefixed;

import org.alfresco.repo.web.scripts.dictionary.AbstractPropertyGet;
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
    private static final String DICTIONARY_PREFIX = "prefix";
    private static final String DICTIONARY_SHORT_CLASS_NAME = "shortClassName";
    private static final String DICTIONARY_SHORTPROPERTY_NAME = "propname";
    private static final String DICTIONARY_PROPERTY_FREFIX = "proppref";
    
    @Override
    protected QName getPropertyQname(WebScriptRequest req)
    {
        String propertyName = req.getServiceMatch().getTemplateVars().get(DICTIONARY_SHORTPROPERTY_NAME);
        String propertyPrefix = req.getServiceMatch().getTemplateVars().get(DICTIONARY_PROPERTY_FREFIX);
        
        //validate the presence of property name
        if(propertyName == null)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Missing parameter short propertyname in the URL");
        }
        //validate the presence of property prefix
        if(propertyPrefix == null)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Missing parameter propertyprefix in the URL");
        }
        
        return QName.createQName(getFullNamespaceURI(propertyPrefix, propertyName));
    }

    @Override
    protected QName getClassQname(WebScriptRequest req)
    {
        String prefix = req.getServiceMatch().getTemplateVars().get(DICTIONARY_PREFIX);
        String shortClassName = req.getServiceMatch().getTemplateVars().get(DICTIONARY_SHORT_CLASS_NAME);
        
        // validate the classname
        if (isValidClassname(prefix, shortClassName) == false)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the classname - " + prefix + ":" + shortClassName + " - parameter in the URL");
        }
        
        return QName.createQName(getFullNamespaceURI(prefix, shortClassName));
    }

}
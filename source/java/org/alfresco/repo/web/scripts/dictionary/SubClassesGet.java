package org.alfresco.repo.web.scripts.dictionary;

import java.util.Collection;

import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript to get the Sub-Classdefinitions using classfilter , namespacePrefix and name
 * 
 * @author Saravanan Sellathurai, Viachaslau Tsikhanovich
 */

public class SubClassesGet extends AbstractSubClassesGet
{
    private static final String DICTIONARY_CLASS_NAME = "classname";
    	
    @Override
    protected Collection<QName> getQNameCollection(WebScriptRequest req, boolean recursive)
    {
        QName classQName = null;
        boolean isAspect = false;
        String className = req.getServiceMatch().getTemplateVars().get(DICTIONARY_CLASS_NAME);
        	
        //validate the className
        if(isValidClassname(className) == true)
        {
            classQName = QName.createQName(getFullNamespaceURI(className));
            if(isValidTypeorAspect(className) == true) 
            {
                isAspect = true;
            }
        }
        else
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the className - " + className + " parameter in the URL");
        }
        
        // collect the subaspects or subtypes of the class
        if(isAspect == true) 
        {
            return this.dictionaryservice.getSubAspects(classQName, recursive);
    	}
    	else
        {
            return this.dictionaryservice.getSubTypes(classQName, recursive);
        }
    }
        
    @Override
    protected void validateClassname(String namespacePrefix, String name)
    {
        if(isValidClassname(namespacePrefix + "_" + name) == false)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the namespacePrefix - " + namespacePrefix + " and name - "+ name  + " - parameter in the URL");
        }
    }
   
}
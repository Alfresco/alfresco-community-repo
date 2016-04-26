package org.alfresco.repo.web.scripts.dictionary.prefixed;

import org.alfresco.repo.web.scripts.dictionary.AbstractClassesGet;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;

/**
 * Webscript to get the Classdefinitions using classfilter , namespaceprefix and name
 * 
 * @author Saravanan Sellathurai, Viachaslau Tsikhanovich
 */

public class ClassesGet extends AbstractClassesGet
{
    @Override
    protected QName getQNameForModel(String namespacePrefix, String name)
    {
        return QName.createQName(getFullNamespaceURI(namespacePrefix, name));
    }
        
    @Override
    protected QName getClassQname(String namespacePrefix, String name)
    {
        if(isValidClassname(namespacePrefix, name) == false)
        {
            throw new WebScriptException(Status.STATUS_NOT_FOUND, "Check the name - " + name + "parameter in the URL");
        }
        return QName.createQName(getFullNamespaceURI(namespacePrefix, name));
    }

}
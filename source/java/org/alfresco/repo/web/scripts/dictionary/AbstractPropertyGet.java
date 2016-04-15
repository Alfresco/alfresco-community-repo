package org.alfresco.repo.web.scripts.dictionary;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript to get the Propertydefinition for a given classname and propname
 * 
 * @author Saravanan Sellathurai, Viachaslau Tsikhanovich
 */

public abstract class AbstractPropertyGet extends DictionaryWebServiceBase
{
    private static final String MODEL_PROP_KEY_PROPERTY_DETAILS = "propertydefs";

    /**
     * Override method from DeclarativeWebScript
     */
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>(1);
        QName classQname = getClassQname(req);
        QName propertyQname = getPropertyQname(req);

        if (this.dictionaryservice.getClass(classQname).getProperties().get(propertyQname) != null)
        {
            model.put(MODEL_PROP_KEY_PROPERTY_DETAILS, this.dictionaryservice.getClass(classQname).getProperties().get(propertyQname));
            model.put(MODEL_PROP_KEY_MESSAGE_LOOKUP, this.dictionaryservice);
        }

        return model;
    }

    /**
     * @param req - webscript request
     * @return  qualified name for property
     */
    protected abstract QName getPropertyQname(WebScriptRequest req);

    /**
     * @param req - webscript request
     * @return  qualified name for class
     */
    protected abstract QName getClassQname(WebScriptRequest req);

}
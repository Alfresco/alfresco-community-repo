package org.alfresco.repo.web.scripts.dictionary;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/*
 * Webscript to get the Associationdefinition for a given classname and association-name
 * @author Viachaslau Tsikhanovich
 */

public abstract class AbstractAssociationGet extends DictionaryWebServiceBase
{
    private static final String MODEL_PROP_KEY_ASSOCIATION_DETAILS = "assocdefs";

    /**
     * Override method from DeclarativeWebScript
     */
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<String, Object>(1);
        QName classQname = getClassQname(req);
        QName associationQname = getAssociationQname(req);

        if (this.dictionaryservice.getClass(classQname).getAssociations().get(associationQname) != null)
        {
            model.put(MODEL_PROP_KEY_ASSOCIATION_DETAILS, this.dictionaryservice.getClass(classQname).getAssociations().get(associationQname));
            model.put(MODEL_PROP_KEY_MESSAGE_LOOKUP, this.dictionaryservice);
        }

        return model;
    }

    /**
     * @param req - webscript request
     * @return qualified name for association
     */
    protected abstract QName getAssociationQname(WebScriptRequest req);

    /**
     * @param req - webscript request
     * @return  qualified name for class
     */
    protected abstract QName getClassQname(WebScriptRequest req);

}
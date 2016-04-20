package org.alfresco.repo.web.scripts.dictionary;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Webscript to get the Classdefinitions for a classname eg. =>cm_author
 * 
 * @author Saravanan Sellathurai, Viachaslau Tsikhanovich
 */

public abstract class AbstractClassGet extends DictionaryWebServiceBase
{
    private static final String MODEL_PROP_KEY_CLASS_DETAILS = "classdefs";
    private static final String MODEL_PROP_KEY_PROPERTY_DETAILS = "propertydefs";
    private static final String MODEL_PROP_KEY_ASSOCIATION_DETAILS = "assocdefs";

    /**
     * Override method from DeclarativeWebScript
     */
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status)
    {
        Map<String, Object> model = new HashMap<String, Object>(3);
        Map<QName, ClassDefinition> classdef = new HashMap<QName, ClassDefinition>();
        Map<QName, Collection<PropertyDefinition>> propdef = new HashMap<QName, Collection<PropertyDefinition>>();
        Map<QName, Collection<AssociationDefinition>> assocdef = new HashMap<QName, Collection<AssociationDefinition>>();

        QName classQname = getClassQname(req);
        classdef.put(classQname, this.dictionaryservice.getClass(classQname));
        propdef.put(classQname, this.dictionaryservice.getClass(classQname).getProperties().values());
        assocdef.put(classQname, this.dictionaryservice.getClass(classQname).getAssociations().values());

        model.put(MODEL_PROP_KEY_CLASS_DETAILS, classdef.values());
        model.put(MODEL_PROP_KEY_PROPERTY_DETAILS, propdef.values());
        model.put(MODEL_PROP_KEY_ASSOCIATION_DETAILS, assocdef.values());
        model.put(MODEL_PROP_KEY_MESSAGE_LOOKUP, this.dictionaryservice);

        return model;
    }

    /**
     * @param req - webscript request
     * @return  qualified name for class
     */
    protected abstract QName getClassQname(WebScriptRequest req);

}
package org.alfresco.module.org_alfresco_module_rm.script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.disposition.property.DispositionProperty;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.util.StringUtils;

/**
 * @author Roy Wetherall
 */
public class DispositionPropertiesGet extends DeclarativeWebScript
{
    protected DispositionService dispositionService;
    protected NamespaceService namespaceService;
    protected DictionaryService dictionaryService;

    /**
     * Sets the disposition service
     *
     * @param dispositionService    the disposition service
     */
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }

    /**
     * Sets the NamespaceService instance
     *
     * @param namespaceService The NamespaceService instance
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    /**
     * Sets the DictionaryService instance
     *
     * @param dictionaryService The DictionaryService instance
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /*
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        boolean recordLevel = false;
        String recordLevelValue = req.getParameter("recordlevel");
        if (recordLevelValue != null)
        {
            recordLevel = Boolean.valueOf(recordLevelValue);
        }
        String dispositionAction = req.getParameter("dispositionaction");

        Collection<DispositionProperty> dispositionProperties = dispositionService.getDispositionProperties(recordLevel, dispositionAction);
        List<Map<String, String>> items = new ArrayList<Map<String, String>>(dispositionProperties.size());
        for (DispositionProperty dispositionProperty : dispositionProperties)
        {
            PropertyDefinition propDef = dispositionProperty.getPropertyDefinition();
            QName propName = dispositionProperty.getQName();

            if (propDef != null)
            {
                Map<String, String> item = new HashMap<String, String>(2);
                String propTitle = propDef.getTitle(dictionaryService);
                if (propTitle == null || propTitle.length() == 0)
                {
                    propTitle = StringUtils.capitalize(propName.getLocalName());
                }
                item.put("label", propTitle);
                item.put("value", propName.toPrefixString(this.namespaceService));
                items.add(item);
            }
        }

        // create model object with the lists model
        Map<String, Object> model = new HashMap<String, Object>(1);
        model.put("properties", items);
        return model;
    }
}

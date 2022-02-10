/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

package org.alfresco.module.org_alfresco_module_rm.script;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementActionService;
import org.alfresco.module.org_alfresco_module_rm.audit.RecordsManagementAuditService;
import org.alfresco.module.org_alfresco_module_rm.audit.event.AuditEvent;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.disposition.property.DispositionProperty;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEvent;
import org.alfresco.module.org_alfresco_module_rm.event.RecordsManagementEventService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.Period;
import org.alfresco.service.cmr.repository.PeriodProvider;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.util.StringUtils;

/**
 * Implementation for Java backed webscript to return lists
 * of values for various records management services.
 *
 * @author Gavin Cornwell
 */
public class ListOfValuesGet extends DeclarativeWebScript
{
    protected RecordsManagementActionService rmActionService;
    protected RecordsManagementAuditService rmAuditService;
    protected RecordsManagementEventService rmEventService;
    protected DispositionService dispositionService;
    protected DictionaryService ddService;
    protected NamespaceService namespaceService;

    /**
     * Sets the RecordsManagementActionService instance
     *
     * @param rmActionService The RecordsManagementActionService instance
     */
    public void setRecordsManagementActionService(RecordsManagementActionService rmActionService)
    {
        this.rmActionService = rmActionService;
    }

    /**
     * Sets the RecordsManagementAuditService instance
     *
     * @param rmAuditService The RecordsManagementAuditService instance
     */
    public void setRecordsManagementAuditService(RecordsManagementAuditService rmAuditService)
    {
        this.rmAuditService = rmAuditService;
    }

    /**
     * Sets the RecordsManagementEventService instance
     *
     * @param rmEventService The RecordsManagementEventService instance
     */
    public void setRecordsManagementEventService(RecordsManagementEventService rmEventService)
    {
        this.rmEventService = rmEventService;
    }

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
     * Sets the DictionaryService instance
     *
     * @param ddService The DictionaryService instance
     */
    public void setDictionaryService(DictionaryService ddService)
    {
        this.ddService = ddService;
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

    /*
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        // add all the lists data to a Map
        Map<String, Object> listsModel = new HashMap<>(4);
        String requestUrl = req.getURL();
        listsModel.put("dispositionActions", createDispositionActionsModel(requestUrl));
        listsModel.put("events", createEventsModel(requestUrl));
        listsModel.put("periodTypes", createPeriodTypesModel(requestUrl));
        listsModel.put("periodProperties", createPeriodPropertiesModel(requestUrl));
        listsModel.put("auditEvents", createAuditEventsModel(requestUrl));

        // create model object with the lists model
        Map<String, Object> model = new HashMap<>(1);
        model.put("lists", listsModel);
        return model;
    }

    /**
     * Creates the model for the list of disposition actions.
     *
     * @param baseUrl The base URL of the service
     * @return model of disposition actions list
     */
    protected Map<String, Object> createDispositionActionsModel(String baseUrl)
    {
        // iterate over the disposition actions
        List<RecordsManagementAction> dispositionActions = this.rmActionService.getDispositionActions();
        List<Map<String, String>> items = new ArrayList<>(dispositionActions.size());
        for (RecordsManagementAction dispositionAction : dispositionActions)
        {
            Map<String, String> item = new HashMap<>(2);
            item.put("label", dispositionAction.getLabel());
            item.put("value", dispositionAction.getName());
            items.add(item);
        }

        // create the model
        Map<String, Object> model = new HashMap<>(2);
        model.put("url", baseUrl + "/dispositionactions");
        model.put("items", items);

        return model;
    }

    /**
     * Creates the model for the list of events.
     *
     * @param baseUrl The base URL of the service
     * @return model of events list
     */
    protected Map<String, Object> createEventsModel(String baseUrl)
    {
        // get all the events including their display labels from the event service
        List<RecordsManagementEvent> events = this.rmEventService.getEvents();
        List<Map<String, Object>> items = new ArrayList<>(events.size());
        for (RecordsManagementEvent event : events)
        {
            Map<String, Object> item = new HashMap<>(3);
            item.put("label", event.getDisplayLabel());
            item.put("value", event.getName());
            item.put("automatic",
                        this.rmEventService.getEventType(event.getType()).isAutomaticEvent());
            items.add(item);
        }

        // create the model
        Map<String, Object> model = new HashMap<>(2);
        model.put("url", baseUrl + "/events");
        model.put("items", items);

        return model;
    }

    /**
     * Creates the model for the list of period types.
     *
     * @param baseUrl The base URL of the service
     * @return model of period types list
     */
    protected Map<String, Object> createPeriodTypesModel(String baseUrl)
    {
        // iterate over all period provides, but ignore 'cron'
        Set<String> providers = Period.getProviderNames();
        List<Map<String, String>> items = new ArrayList<>(providers.size());
        for (String provider : providers)
        {
            PeriodProvider pp = Period.getProvider(provider);
            if (!pp.getPeriodType().equals("cron"))
            {
                Map<String, String> item = new HashMap<>(2);
                item.put("label", pp.getDisplayLabel());
                item.put("value", pp.getPeriodType());
                items.add(item);
            }
        }

        // create the model
        Map<String, Object> model = new HashMap<>(2);
        model.put("url", baseUrl + "/periodtypes");
        model.put("items", items);

        return model;
    }

    /**
     * Creates the model for the list of period properties.
     *
     * @param baseUrl The base URL of the service
     * @return model of period properties list
     */
    protected Map<String, Object> createPeriodPropertiesModel(String baseUrl)
    {
        // iterate over all period properties and get the label from their type definition
        Collection<DispositionProperty> dispositionProperties = dispositionService.getDispositionProperties();
        List<Map<String, String>> items = new ArrayList<>(dispositionProperties.size());
        for (DispositionProperty dispositionProperty : dispositionProperties)
        {
            PropertyDefinition propDef = dispositionProperty.getPropertyDefinition();
            QName propName = dispositionProperty.getQName();

            if (propDef != null)
            {
                Map<String, String> item = new HashMap<>(2);
                String propTitle = propDef.getTitle(ddService);
                if (propTitle == null || propTitle.length() == 0)
                {
                    propTitle = StringUtils.capitalize(propName.getLocalName());
                }
                item.put("label", propTitle);
                item.put("value", propName.toPrefixString(this.namespaceService));
                items.add(item);
            }
        }

        // create the model
        Map<String, Object> model = new HashMap<>(2);
        model.put("url", baseUrl + "/periodproperties");
        model.put("items", items);

        return model;
    }

    /**
     * Creates the model for the list of audit events.
     *
     * @param baseUrl The base URL of the service
     * @return model of audit events list
     */
    protected Map<String, Object> createAuditEventsModel(String baseUrl)
    {
        // iterate over all audit events
        List<AuditEvent> auditEvents = this.rmAuditService.getAuditEvents();
        List<Map<String, String>> items = new ArrayList<>(auditEvents.size());
        for (AuditEvent event : auditEvents)
        {
            Map<String, String> item = new HashMap<>(2);
            item.put("label", event.getLabel());
            item.put("value", event.getName());
            items.add(item);
        }

        // create the model
        Map<String, Object> model = new HashMap<>(2);
        model.put("url", baseUrl + "/auditevents");
        model.put("items", items);

        return model;
    }
}

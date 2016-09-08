/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.module.org_alfresco_module_rm.script.slingshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.RecordsManagementAdminService;
import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.service.cmr.dictionary.AspectDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * RM serach properties GET web script
 * 
 * @author Roy Wetherall
 */
public class RMSearchPropertiesGet extends DeclarativeWebScript
{
    private RecordsManagementAdminService adminService;
    
    private RecordsManagementService recordsManagementService;
    
    private DictionaryService dictionaryService;
    
    private NamespaceService namespaceService;
    
    public void setAdminService(RecordsManagementAdminService adminService)
    {
        this.adminService = adminService;
    }
    
    public void setRecordsManagementService(RecordsManagementService recordsManagementService)
    {
        this.recordsManagementService = recordsManagementService;
    }
    
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
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
        Map<String, Object> model = new HashMap<String, Object>(13);
        
        List<Group> groups = new ArrayList<Group>(5);
        
        Set<QName> aspects = recordsManagementService.getRecordMetaDataAspects();
        for (QName aspect : aspects)
        {
            Map<QName, PropertyDefinition> properties = dictionaryService.getPropertyDefs(aspect);
            Property[] propObjs = new Property[properties.size()];
            int index = 0;
            for (PropertyDefinition propertyDefinition : properties.values())
            {
                Property propObj = new Property(propertyDefinition);
                propObjs[index] = propObj;
                index ++;
            }
            
            AspectDefinition aspectDefinition = dictionaryService.getAspect(aspect);
            Group group = new Group(aspect.getLocalName(), aspectDefinition.getTitle(dictionaryService), propObjs);    
            groups.add(group);
        }

        Map<QName, PropertyDefinition> customProps = adminService.getCustomPropertyDefinitions();
        Property[] propObjs = new Property[customProps.size()];
        int index = 0;
        for (PropertyDefinition propertyDefinition : customProps.values())
        {
            Property propObj = new Property(propertyDefinition);
            propObjs[index] = propObj;
            index ++;
        }
        
        Group group = new Group("rmcustom", "Custom", propObjs);    
        groups.add(group);
        
        model.put("groups", groups);
        return model;
    }
    
    public class Group
    {        
        private String id;
        private String label;
        private Property[] properties;
        
        public Group(String id, String label, Property[] properties)
        {
            this.id = id;
            this.label = label;
            this.properties = properties;
        }
        
        public String getId()
        {
            return id;
        }
        
        public String getLabel()
        {
            return label;
        }
        
        public Property[] getProperties()
        {
            return properties;
        }       
    }
    
    public class Property
    {
        private String prefix;
        private String shortName;
        private String label;
        private String type;
        
        public Property(PropertyDefinition propertyDefinition)
        {
            QName qName = propertyDefinition.getName().getPrefixedQName(namespaceService);
            this.prefix = QName.splitPrefixedQName(qName.toPrefixString())[0];
            this.shortName = qName.getLocalName();
            this.label = propertyDefinition.getTitle(dictionaryService);
            this.type = propertyDefinition.getDataType().getName().getLocalName();
        }
        
        public String getPrefix()
        {
            return prefix;
        }
        
        public String getShortName()
        {
            return shortName;
        }
        
        public String getLabel()
        {
            return label;
        }
        
        public String getType()
        {
            return type;
        }
    }
}
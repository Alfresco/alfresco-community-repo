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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService;
import org.alfresco.service.cmr.dictionary.ClassDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * This class provides the implementation for the customisable.get webscript.
 *
 * @author Roy Wetherall
 */
public class CustomisableGet extends DeclarativeWebScript
{
    /** Records management admin service */
    private RecordsManagementAdminService rmAdminService;

    /** Dictionary service */
    private DictionaryService dictionaryService;

    /** Namespace service */
    private NamespaceService namespaceService;

    /**
     * @param rmAdminService	records management admin service
     */
    public void setRecordsManagementAdminService(RecordsManagementAdminService rmAdminService)
    {
        this.rmAdminService = rmAdminService;
    }

    /**
     * @param namespaceService	namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
		this.namespaceService = namespaceService;
	}

    /**
     * @param dictionaryService dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
		this.dictionaryService = dictionaryService;
	}
    /**
     * @see org.springframework.extensions.webscripts.DeclarativeWebScript#executeImpl(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.Status, org.springframework.extensions.webscripts.Cache)
     */
    @Override
    public Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache)
    {
        Map<String, Object> model = new HashMap<>();

        Set<QName> qnames = rmAdminService.getCustomisable();
        ArrayList<Item> items = new ArrayList<>(qnames.size());
        for (QName qname : qnames)
        {
            ClassDefinition definition = dictionaryService.getClass(qname);
            if (definition != null)
            {
                String name = qname.toPrefixString(namespaceService);
                String title = definition.getTitle(dictionaryService);
                if (title == null || title.length() == 0)
                {
                    title = qname.getLocalName();
                }
                boolean isAspect = definition.isAspect();

                items.add(new Item(name, isAspect, title));
            }
        }

        // Sort the customisable types and aspects by title
        Collections.sort(items, new Comparator<Item>()
        {
            @Override
            public int compare(Item o1, Item o2)
            {
                return o1.title.compareToIgnoreCase(o2.title);
            }});

        model.put("items", items);
        return model;
    }

    /**
     * Model items
     */
    public class Item
    {
        private String name;
        private boolean isAspect;
        private String title;

        public Item(String name, boolean isAspect, String title)
        {
            this.name = name;
            this.isAspect = isAspect;
            this.title = title;
        }

        public String getName()
        {
            return name;
        }

        public boolean getIsAspect()
        {
            return isAspect;
        }

        public String getTitle()
        {
            return title;
        }

        @Override
        public int hashCode()
        {
            int varCode = (null == name ? 0 : name.hashCode());
            return 31 + varCode;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (obj == null || (obj.getClass() != this.getClass()))
            {
                return false;
            }
            else
            {
                return this.name.equals(((Item)obj).name);
            }
        }
    }
}

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

package org.alfresco.module.org_alfresco_module_rm.forms;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.RecordsManagementServiceRegistry;
import org.alfresco.module.org_alfresco_module_rm.admin.RecordsManagementAdminService;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.repo.forms.Field;
import org.alfresco.repo.forms.FieldGroup;
import org.alfresco.repo.forms.Form;
import org.alfresco.repo.forms.FormData;
import org.alfresco.repo.forms.processor.AbstractFilter;
import org.alfresco.repo.forms.processor.node.FieldUtils;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract base class for records management related form filter
 * implementations.
 *
 * @author Gavin Cornwell
 */
public abstract class RecordsManagementFormFilter<ItemType> extends AbstractFilter<ItemType, NodeRef>
{
    /** Logger */
    private static Log logger = LogFactory.getLog(RecordsManagementFormFilter.class);

    public static final String CUSTOM_RM_FIELD_GROUP_ID = "rm-custom";
    public static final String RM_METADATA_PREFIX = "rm-metadata-";

    protected NamespaceService namespaceService;
    protected NodeService nodeService;
    protected RecordsManagementServiceRegistry rmServiceRegistry;
    protected RecordsManagementAdminService rmAdminService;
    protected RecordService recordService;
    protected DictionaryService dictionaryService;

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
     * Sets the node service
     *
     * @param nodeService The NodeService instance
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Sets the RecordsManagementServiceRegistry instance
     *
     * @param rmServiceRegistry The RecordsManagementServiceRegistry instance
     */
    public void setRecordsManagementServiceRegistry(RecordsManagementServiceRegistry rmServiceRegistry)
    {
        this.rmServiceRegistry = rmServiceRegistry;
    }

    /**
     * Sets the RecordsManagementAdminService instance
     *
     * @param rmAdminService The RecordsManagementAdminService instance
     */
    public void setRecordsManagementAdminService(RecordsManagementAdminService rmAdminService)
    {
        this.rmAdminService = rmAdminService;
    }

    /**
     * @param recordService record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    /**
     * @param dictionaryService dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    /**
     * Add property fields to group
     *
     * @param form
     * @param props
     * @param setId
     */
    protected void addPropertyFieldsToGroup(Form form, Map<QName, PropertyDefinition> props, String setId, String setLabel)
    {
        if (props != null)
        {
            for (Map.Entry<QName, PropertyDefinition> entry : props.entrySet())
            {
                PropertyDefinition prop = entry.getValue();

                String id = form.getItem().getId();
                id = id.replaceFirst("/", "://");
                NodeRef nodeRef = new NodeRef(id);
                Serializable value = nodeService.getProperty(nodeRef, entry.getKey());

                FieldGroup group = new FieldGroup(setId, setLabel, false, false, null);
                Field field = FieldUtils.makePropertyField(prop, value, group, namespaceService, dictionaryService);

                form.addField(field);

                if (logger.isDebugEnabled())
                {
                    logger.debug("Adding custom property .. " + prop.getName().toString() + " .. with value " + value + ".. to group .. " + setId);
                }
            }
        }
    }

    /**
     * @see
     * org.alfresco.repo.forms.processor.Filter#beforePersist(java.lang.Object,
     * org.alfresco.repo.forms.FormData)
     */
    public void beforePersist(ItemType item, FormData data)
    {
        // ignored
    }

    /**
     * @see
     * org.alfresco.repo.forms.processor.Filter#beforeGenerate(java.lang.Object,
     * java.util.List, java.util.List, org.alfresco.repo.forms.Form,
     * java.util.Map)
     */
    public void beforeGenerate(ItemType item, List<String> fields, List<String> forcedFields, Form form,
                Map<String, Object> context)
    {
        // ignored
    }

    /**
     * @see
     * org.alfresco.repo.forms.processor.Filter#afterPersist(java.lang.Object,
     * org.alfresco.repo.forms.FormData, java.lang.Object)
     */
    public void afterPersist(ItemType item, FormData data, NodeRef persistedObject)
    {
        // ignored
    }
}

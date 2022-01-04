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

package org.alfresco.module.org_alfresco_module_rm.version;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.record.RecordService;
import org.alfresco.repo.version.Node2ServiceImpl;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.common.VersionUtil;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Extended version node service implementation that supports the retrieval of
 * recorded version state.
 *
 * @author Roy Wetherall
 * @since 2.3
 */
public class RecordableVersionNodeServiceImpl extends Node2ServiceImpl
                                              implements RecordableVersionModel
{
    /** record service */
    private RecordService recordService;

    /** record model URI's */
    private List<String> recordModelURIs;

	/**
     * @param recordModelURIs namespaces specific to records
     */
    public void setRecordModelURIs(List<String> recordModelURIs)
    {
        this.recordModelURIs = recordModelURIs;
    }

    /**
     * @param recordService record service
     */
    public void setRecordService(RecordService recordService)
    {
        this.recordService = recordService;
    }

    /**
     * @see org.alfresco.repo.version.Node2ServiceImpl#getProperties(org.alfresco.service.cmr.repository.NodeRef)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<QName, Serializable> getProperties(NodeRef nodeRef) throws InvalidNodeRefException
    {
        // TODO only supported for Version2

        NodeRef converted = VersionUtil.convertNodeRef(nodeRef);
        if (dbNodeService.hasAspect(converted, ASPECT_RECORDED_VERSION))
        {
            NodeRef record = (NodeRef)dbNodeService.getProperty(converted, PROP_RECORD_NODE_REF);
            if (record != null && dbNodeService.exists(record))
            {
                Map<QName, Serializable> properties =  dbNodeService.getProperties(record);
                return processProperties(converted, properties);
            }
            else
            {
                return (Map<QName, Serializable>)Collections.EMPTY_MAP;
            }
        }
        else
        {
            return super.getProperties(nodeRef);
        }
    }

    /**
     * Process properties map before returning as frozen state.
     *
     * @param properties                                        properties map
     * @return {@link Map}&lt;{@link QName}, {@link Serializable}&gt; processed property map
     */
    protected Map<QName, Serializable> processProperties(NodeRef version, Map<QName, Serializable> properties)
    {
        Map<QName, Serializable> cloneProperties = new HashMap<>(properties);

        // revert modified record name
        properties.put(ContentModel.PROP_NAME, properties.get(RecordsManagementModel.PROP_ORIGIONAL_NAME));

        // remove all rma, rmc, rmr and rmv properties
        for (QName property : cloneProperties.keySet())
        {
            if (!PROP_RECORDABLE_VERSION_POLICY.equals(property) &&
                    !PROP_FILE_PLAN.equals(property) &&
                    (recordService.isRecordMetadataProperty(property) ||
                            recordModelURIs.contains(property.getNamespaceURI())))
            {
                properties.remove(property);
            }
        }

        // do standard property processing
        processVersionProperties(version, properties);

        return properties;
    }

    /**
     * Process version properties.
     *
     * @param version                   version node reference
     * @param properties                properties map
     */
    protected void processVersionProperties(NodeRef version, Map<QName, Serializable> properties) throws InvalidNodeRefException
    {
        // get version properties
        Map<QName, Serializable> versionProperties = dbNodeService.getProperties(version);

        if (versionProperties != null)
        {
            String versionLabel = (String)versionProperties.get(Version2Model.PROP_QNAME_VERSION_LABEL);
            properties.put(ContentModel.PROP_VERSION_LABEL, versionLabel);

            // Convert frozen sys:referenceable properties
            NodeRef nodeRef = (NodeRef)versionProperties.get(Version2Model.PROP_QNAME_FROZEN_NODE_REF);
            if (nodeRef != null)
            {
                properties.put(ContentModel.PROP_STORE_PROTOCOL, nodeRef.getStoreRef().getProtocol());
                properties.put(ContentModel.PROP_STORE_IDENTIFIER, nodeRef.getStoreRef().getIdentifier());
                properties.put(ContentModel.PROP_NODE_UUID, nodeRef.getId());
            }

            Long dbid = (Long)versionProperties.get(Version2Model.PROP_QNAME_FROZEN_NODE_DBID);
            properties.put(ContentModel.PROP_NODE_DBID, dbid);

            // Convert frozen cm:auditable properties
            String creator = (String)versionProperties.get(Version2Model.PROP_QNAME_FROZEN_CREATOR);
            if (creator != null)
            {
                properties.put(ContentModel.PROP_CREATOR, creator);
            }

            Date created = (Date)versionProperties.get(Version2Model.PROP_QNAME_FROZEN_CREATED);
            if (created != null)
            {
                properties.put(ContentModel.PROP_CREATED, created);
            }

            // TODO - check use-cases for get version, revert, restore ....
            String modifier = (String)versionProperties.get(Version2Model.PROP_QNAME_FROZEN_MODIFIER);
            if (modifier != null)
            {
                properties.put(ContentModel.PROP_MODIFIER, modifier);
            }

            Date modified = (Date)versionProperties.get(Version2Model.PROP_QNAME_FROZEN_MODIFIED);
            if (modified != null)
            {
                properties.put(ContentModel.PROP_MODIFIED, modified);
            }

            Date accessed = (Date)versionProperties.get(Version2Model.PROP_QNAME_FROZEN_ACCESSED);
            if (accessed != null)
            {
                properties.put(ContentModel.PROP_ACCESSED, accessed);
            }

            String owner = (String)versionProperties.get(PROP_FROZEN_OWNER);
            if (owner != null)
            {
                properties.put(ContentModel.PROP_OWNER, owner);
            }
        }
    }

    /**
     * @see org.alfresco.repo.version.Node2ServiceImpl#getAspects(org.alfresco.service.cmr.repository.NodeRef)
     */
    @SuppressWarnings("unchecked")
    @Override
    public Set<QName> getAspects(NodeRef nodeRef) throws InvalidNodeRefException
    {
        // TODO only supported for Version2

        NodeRef converted = VersionUtil.convertNodeRef(nodeRef);
        if (dbNodeService.hasAspect(converted, ASPECT_RECORDED_VERSION))
        {

            NodeRef record = (NodeRef)dbNodeService.getProperty(converted, PROP_RECORD_NODE_REF);
            if (record != null && dbNodeService.exists(record))
            {
                Set<QName> aspects =  dbNodeService.getAspects(record);
                return processAspects(aspects);
            }
            else
            {
                return (Set<QName>)Collections.EMPTY_SET;
            }
        }
        else
        {
            return super.getAspects(nodeRef);
        }
    }

    /**
     * Process frozen aspects.
     *
     * @param aspects                       aspect set
     * @return {@link Set}&lt;{@link QName}&gt;   processed aspect set
     */
    protected Set<QName> processAspects(Set<QName> aspects)
    {
        Set<QName> result = new HashSet<>(aspects);

        // remove version aspects
        result.remove(ASPECT_VERSION);
        result.remove(ASPECT_RECORDED_VERSION);

        // remove rm aspects
        for (QName aspect : aspects)
        {
            if (!ASPECT_VERSIONABLE.equals(aspect) &&
                    (recordService.isRecordMetadataAspect(aspect) ||
                            recordModelURIs.contains(aspect.getNamespaceURI())))
            {
                result.remove(aspect);
            }
        }

        // remove custom record meta-data aspects

        return result;
    }
}

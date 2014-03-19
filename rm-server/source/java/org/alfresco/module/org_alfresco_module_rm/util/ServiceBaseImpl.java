/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.util;

import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.hold.HoldService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyMap;

/**
 * Helper base class for service implementations.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class ServiceBaseImpl implements RecordsManagementModel
{
    /** Node service */
    protected NodeService nodeService;

    /** Dictionary service */
    protected DictionaryService dictionaryService;

    /**
     * @param nodeService   node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param dictionaryService dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }
    
    /**
     * Indicates whether the given node is a record folder or not.
     * <p>
     * Exposed in the RecordFolder service.
     * 
     * @param   nodeRef node reference
     * @return  boolean true if record folder, false otherwise
     */
    public boolean isRecordFolder(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        return instanceOf(nodeRef, TYPE_RECORD_FOLDER);
    }

    /**
     * Indicates whether the given node reference is a record or not.
     *
     * @param nodeRef   node reference
     * @return boolean  true if node reference is a record, false otherwise
     */
    public boolean isRecord(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        return nodeService.hasAspect(nodeRef, ASPECT_RECORD);
    }
    
    /**
     * Indicates whether the given node reference is a hold or not.
     * <p>
     * Exposed publically in the {@link HoldService}
     * 
     * @param nodeRef   node reference
     * @return boolean  true if rma:hold or sub-type, false otherwise
     */
    public boolean isHold(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        if (nodeService.exists(nodeRef) && instanceOf(nodeRef, TYPE_HOLD))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Gets the file plan that a given file plan component resides within.
     *
     * @param nodeRef           node reference
     * @return {@link NodeRef}  file plan, null if none
     */
    public NodeRef getFilePlan(final NodeRef nodeRef)
    {
        NodeRef result = null;
        if (nodeRef != null)
        {
            RunAsWork<NodeRef> runAsWork = new RunAsWork<NodeRef>()
            {
                @Override
                public NodeRef doWork() throws Exception
                {
                    NodeRef result = (NodeRef)nodeService.getProperty(nodeRef, PROP_ROOT_NODEREF);
                    if (result == null)
                    {
                        if (instanceOf(nodeRef, TYPE_FILE_PLAN))
                        {
                            result = nodeRef;
                        }
                        else
                        {
                            ChildAssociationRef parentAssocRef = nodeService.getPrimaryParent(nodeRef);
                            if (parentAssocRef != null)
                            {
                                result = getFilePlan(parentAssocRef.getParentRef());
                            }
                        }
                    }

                    return result;
                }

            };

            result = AuthenticationUtil.runAsSystem(runAsWork);
        }

        return result;
    }

    /**
     * Utility method to safely and quickly determine if a node is a type (or sub-type) of the one specified.
     *
     * @param nodeRef       node reference
     * @param ofClassName   class name to check
     */
    protected boolean instanceOf(NodeRef nodeRef, QName ofClassName)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);
        ParameterCheck.mandatory("ofClassName", ofClassName);
        boolean result = false;
        if (nodeService.exists(nodeRef) &&
            (ofClassName.equals(nodeService.getType(nodeRef)) ||
             dictionaryService.isSubClass(nodeService.getType(nodeRef), ofClassName)))
        {
            result = true;
        }
        return result;
    }

    /**
     * Utility method to get the next counter for a node.
     * <p>
     * If the node is not already countable, then rma:countable is added and 0 returned.
     *
     * @param nodeRef   node reference
     * @return int      next counter value
     */
    protected int getNextCount(NodeRef nodeRef)
    {
        int counter = 0;
        if (!nodeService.hasAspect(nodeRef, ASPECT_COUNTABLE))
        {
            PropertyMap props = new PropertyMap(1);
            props.put(PROP_COUNT, 1);
            nodeService.addAspect(nodeRef, ASPECT_COUNTABLE, props);
            counter = 1;
        }
        else
        {
            Integer value = (Integer)this.nodeService.getProperty(nodeRef, PROP_COUNT);
            if (value != null)
            {
                counter = value.intValue() + 1;
            }
            else
            {
                counter = 1;
            }
            nodeService.setProperty(nodeRef, PROP_COUNT, counter);

        }
        return counter;
    }

    /**
     * Helper method to get a set containing the node's type and all it's aspects
     *
     * @param nodeRef       nodeRef
     * @return Set<QName>   set of qname's
     */
    protected Set<QName> getTypeAndApsects(NodeRef nodeRef)
    {
        Set<QName> result = nodeService.getAspects(nodeRef);
        result.add(nodeService.getType(nodeRef));
        return result;
    }
    
    /**
     * Helper method that executed work as system user.
     * <p>
     * Useful when testing using mocks.
     * 
     * @param runAsWork work to execute as system user
     * @return 
     */
    public <R> R runAsSystem(RunAsWork<R> runAsWork)
    {
        return AuthenticationUtil.runAsSystem(runAsWork);
    }
    
    /**
     * Helper method that executed work as given user.
     * <p>
     * Useful when testing using mocks.
     * 
     * @param runAsWork work to execute as given user
     * @return
     */
    public <R> R runAs(RunAsWork<R> runAsWork, String uid)
    {
        return AuthenticationUtil.runAs(runAsWork, uid);
    }
}

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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanComponentKind;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyMap;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Helper base class for service implementations.
 *
 * @author Roy Wetherall
 * @since 2.1
 */
public class ServiceBaseImpl implements RecordsManagementModel, ApplicationContextAware
{
    /** Node service */
    protected NodeService nodeService;

    /** Dictionary service */
    protected DictionaryService dictionaryService;

    /** Application context */
    protected ApplicationContext applicationContext;

    /** internal node service */
    private NodeService internalNodeService;

    /**
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
    {
        this.applicationContext = applicationContext;
    }

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
     * Helper to get internal node service.
     * <p>
     * Used for performance reasons.
     */
    protected NodeService getInternalNodeService()
    {
        if (internalNodeService == null)
        {
            internalNodeService = (NodeService)applicationContext.getBean("dbNodeService");
        }

        return internalNodeService;
    }

    /**
     * Gets the file plan component kind from the given node reference
     *
     * @see FilePlanService#getFilePlanComponentKind(org.alfresco.service.cmr.repository.NodeRef)
     */
    public FilePlanComponentKind getFilePlanComponentKind(NodeRef nodeRef)
    {
        FilePlanComponentKind result = null;

        Map<NodeRef, FilePlanComponentKind> map = TransactionalResourceHelper.getMap("rm.transaction.filePlanComponentByNodeRef");
        if (map.containsKey(nodeRef))
        {
            result = map.get(nodeRef);
        }
        else
        {
            if (isFilePlanComponent(nodeRef))
            {
                result = FilePlanComponentKind.FILE_PLAN_COMPONENT;

                if (isFilePlan(nodeRef))
                {
                    result = FilePlanComponentKind.FILE_PLAN;
                }
                else if (isRecordCategory(nodeRef))
                {
                    result = FilePlanComponentKind.RECORD_CATEGORY;
                }
                else if (isRecordFolder(nodeRef))
                {
                    result = FilePlanComponentKind.RECORD_FOLDER;
                }
                else if (isRecord(nodeRef))
                {
                    result = FilePlanComponentKind.RECORD;
                }
                else if (isHold(nodeRef))
                {
                    result = FilePlanComponentKind.HOLD;
                }
                else if (isTransfer(nodeRef))
                {
                    result = FilePlanComponentKind.TRANSFER;
                }
                else if (instanceOf(nodeRef, TYPE_DISPOSITION_SCHEDULE) || instanceOf(nodeRef, TYPE_DISPOSITION_ACTION_DEFINITION))
                {
                    result = FilePlanComponentKind.DISPOSITION_SCHEDULE;
                }
                else if (isUnfiledRecordsContainer(nodeRef))
                {
                    result = FilePlanComponentKind.UNFILED_RECORD_CONTAINER;
                }
            }

            if (result != null)
            {
                map.put(nodeRef, result);
            }
        }

        return result;
    }

    /**
     * Gets the file plan component kind from the given type.
     *
     * @see FilePlanService#getFilePlanComponentKindFromType(QName)
     */
    public FilePlanComponentKind getFilePlanComponentKindFromType(QName type)
    {
        FilePlanComponentKind result = null;

        if (ASPECT_FILE_PLAN_COMPONENT.equals(type))
        {
            result = FilePlanComponentKind.FILE_PLAN_COMPONENT;
        }
        else if (instanceOf(type, ASPECT_RECORD))
        {
            result = FilePlanComponentKind.RECORD;
        }
        else if (instanceOf(type, TYPE_FILE_PLAN))
        {
            result = FilePlanComponentKind.FILE_PLAN;
        }
        else if (instanceOf(type, TYPE_RECORD_CATEGORY))
        {
            result = FilePlanComponentKind.RECORD_CATEGORY;
        }
        else if (instanceOf(type, TYPE_RECORD_FOLDER))
        {
            result = FilePlanComponentKind.RECORD_FOLDER;
        }
        else if (instanceOf(type, TYPE_HOLD))
        {
            result = FilePlanComponentKind.HOLD;
        }
        else if (instanceOf(type, TYPE_TRANSFER))
        {
            result = FilePlanComponentKind.TRANSFER;
        }
        else if (instanceOf(type, TYPE_DISPOSITION_SCHEDULE) ||
                 instanceOf(type, TYPE_DISPOSITION_ACTION_DEFINITION))
        {
            result = FilePlanComponentKind.DISPOSITION_SCHEDULE;
        }

        return result;
    }

    /**
     * Indicates whether the given node is a file plan component or not.
     * <p>
     * Exposed in the FilePlan service.
     *
     * @see FilePlanService#isFilePlanComponent(org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean isFilePlanComponent(NodeRef nodeRef)
    {
        boolean result = false;
        if (getInternalNodeService().exists(nodeRef) &&
            getInternalNodeService().hasAspect(nodeRef, ASPECT_FILE_PLAN_COMPONENT))
        {
            result = true;
        }
        return result;
    }

    /**
     * Indicates whether the given node is a file plan or not.
     * <p>
     * Exposed in the FilePlan service.
     *
     * @see FilePlanService#isFilePlan(org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean isFilePlan(NodeRef nodeRef)
    {
        return instanceOf(nodeRef, TYPE_FILE_PLAN);
    }

    /**
     * Indicates whether the given node is a file plan container or not.
     *
     * @see FilePlanService#isFilePlanContainer(org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean isFilePlanContainer(NodeRef nodeRef)
    {
        return instanceOf(nodeRef, TYPE_RECORDS_MANAGEMENT_CONTAINER);
    }

    /**
     * Indicates whether the given node is a record category or not.
     *
     * @see FilePlanService#isRecordCategory(org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean isRecordCategory(NodeRef nodeRef)
    {
        return instanceOf(nodeRef, TYPE_RECORD_CATEGORY);
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

        return getInternalNodeService().hasAspect(nodeRef, ASPECT_RECORD);
    }

    /**
     * Indicates whether the given node reference is a hold or not.
     * <p>
     * Exposed publicly in the {@link HoldService}
     *
     * @param nodeRef   node reference
     * @return boolean  true if rma:hold or sub-type, false otherwise
     */
    public boolean isHold(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        boolean isHold = false;
        if (getInternalNodeService().exists(nodeRef) &&
            instanceOf(nodeRef, TYPE_HOLD))
        {
            isHold = true;
        }
        return isHold;
    }

    /**
     * Indicates whether the given node reference is a transfer or not.
     *
     * @see org.alfresco.module.org_alfresco_module_rm.transfer.TransferService#isTransfer(NodeRef)
     */
    public boolean isTransfer(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        return instanceOf(nodeRef, TYPE_TRANSFER);
    }

    /**
     * Indicates whether the given node reference is an unfiled records container or not.
     *
     * @param nodeRef node reference
     * @return boolean true if rma:unfiledRecordContainer or sub-type, false otherwise
     */
    public boolean isUnfiledRecordsContainer(NodeRef nodeRef)
    {
        ParameterCheck.mandatory("nodeRef", nodeRef);

        return instanceOf(nodeRef, TYPE_UNFILED_RECORD_CONTAINER);
    }

    /**
     * Indicates whether a record is complete or not.
     *
     * @see org.alfresco.module.org_alfresco_module_rm.record.RecordService#isDeclared(org.alfresco.service.cmr.repository.NodeRef)
     */
    public boolean isDeclared(NodeRef record)
    {
        ParameterCheck.mandatory("record", record);

        return getInternalNodeService().hasAspect(record, ASPECT_DECLARED_RECORD);
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
        	Map<NodeRef, NodeRef> transactionCache = TransactionalResourceHelper.getMap("rm.servicebase.getFilePlan");
        	if (transactionCache.containsKey(nodeRef))
        	{
        		result = transactionCache.get(nodeRef);
        	}
        	else
        	{
	            result = (NodeRef)getInternalNodeService().getProperty(nodeRef, PROP_ROOT_NODEREF);
	            if (result == null || !instanceOf(result, TYPE_FILE_PLAN))
	            {
	                if (instanceOf(nodeRef, TYPE_FILE_PLAN))
	                {
	                    result = nodeRef;
	                }
	                else
	                {
	                    ChildAssociationRef parentAssocRef = getInternalNodeService().getPrimaryParent(nodeRef);
	                    if (parentAssocRef != null)
	                    {
	                        result = getFilePlan(parentAssocRef.getParentRef());
	                    }
	                }
	            }

	            // cache result in transaction
	            transactionCache.put(nodeRef, result);
        	}
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
        QName className = getInternalNodeService().getType(nodeRef);
        return instanceOf(className, ofClassName);
    }

    private static Map<String, Boolean> instanceOfCache = new HashMap<String, Boolean>();

    /**
     * Utility method to quickly determine whether one class is equal to or sub of another.
     *
     * @param className     class name
     * @param ofClassName   class name to check against
     * @return boolean      true if equal to or sub, false otherwise
     */
    protected boolean instanceOf(QName className, QName ofClassName)
    {
        ParameterCheck.mandatory("className", className);
        ParameterCheck.mandatory("ofClassName", ofClassName);

        boolean result = false;

        String key = className.toString() + "|" + ofClassName.toString();
        if (instanceOfCache.containsKey(key))
        {
            result = instanceOfCache.get(key);
        }
        else
        {
            if (ofClassName.equals(className) ||
                dictionaryService.isSubClass(className, ofClassName))
            {
                result = true;
            }

            instanceOfCache.put(key, result);
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

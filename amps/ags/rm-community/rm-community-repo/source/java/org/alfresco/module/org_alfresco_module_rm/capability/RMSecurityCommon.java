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

package org.alfresco.module.org_alfresco_module_rm.capability;

import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.capability.impl.ViewRecordsCapability;
import org.alfresco.module.org_alfresco_module_rm.caveat.RMCaveatConfigComponent;
import org.alfresco.module.org_alfresco_module_rm.fileplan.FilePlanService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.util.Pair;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import net.sf.acegisecurity.vote.AccessDecisionVoter;


/**
 * Common security functions.
 *
 * TODO move methods to the appropriate services
 *
 * @author Roy Wetherall
 * @since 2.0
 */
public class RMSecurityCommon implements ApplicationContextAware
{
    /** No set value */
    protected static final int NOSET_VALUE = -100;

    /** Logger */
    private static Log logger = LogFactory.getLog(RMSecurityCommon.class);

    /** Services */
    //This is the internal NodeService -- no permission checks
    protected NodeService nodeService;
    protected PermissionService permissionService;
    protected RMCaveatConfigComponent caveatConfigComponent;
    private FilePlanService filePlanService;

    /** Application context */
    protected ApplicationContext applicationContext;

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
     * @param permissionService permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    /**
     * @param caveatConfigComponent caveat config service
     */
    public void setCaveatConfigComponent(RMCaveatConfigComponent caveatConfigComponent)
    {
        this.caveatConfigComponent = caveatConfigComponent;
    }

    /**
     * @return	FilePlanService	file plan service
     */
    protected FilePlanService getFilePlanService()
    {
    	if (filePlanService == null)
    	{
    		filePlanService = (FilePlanService)applicationContext.getBean("filePlanService");
    	}
		return filePlanService;
	}

    /**
     * Sets a value into the transaction cache
     *
     * @param prefix
     * @param nodeRef
     * @param value
     * @return
     */
    protected int setTransactionCache(String prefix, NodeRef nodeRef, int value)
    {
        String user = AuthenticationUtil.getRunAsUser();
        AlfrescoTransactionSupport.bindResource(prefix + nodeRef.toString() + user, Integer.valueOf(value));
        return value;
    }

    /**
     * Gets a value from the transaction cache
     *
     * @param prefix
     * @param nodeRef
     * @return
     */
    protected int getTransactionCache(String prefix, NodeRef nodeRef)
    {
        int result = NOSET_VALUE;
        StringBuffer key = new StringBuffer(prefix)
                .append(nodeRef)
                .append(AuthenticationUtil.getRunAsUser());
        
        Integer value = (Integer)AlfrescoTransactionSupport.getResource(key);
        if (value != null)
        {
            result = value.intValue();
        }
        return result;
    }

    /**
     * Check for RM read
     *
     * @param nodeRef
     * @return
     */
    public int checkRead(NodeRef nodeRef)
    {
        int result = AccessDecisionVoter.ACCESS_ABSTAIN;
        if (nodeRef != null)
        {
            // now we know the node - we can abstain for certain types and aspects (eg, rm)
            result = checkRead(nodeRef, false);
        }

        return result;
    }

    /**
     * Check for RM read
     *
     * @param nodeRef
     * @param allowDMRead
     * @return
     */
    public int checkRead(NodeRef nodeRef, boolean allowDMRead)
    {
        int result = AccessDecisionVoter.ACCESS_ABSTAIN;

        if (nodeService.hasAspect(nodeRef, RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT))
        {
            result = checkRmRead(nodeRef);
        }
        else if (allowDMRead)
        {
            // Check DM read for copy etc
            // DM does not grant - it can only deny
            if (permissionService.hasPermission(nodeRef, PermissionService.READ) == AccessStatus.DENIED)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("\t\tPermission is denied");
                    Thread.dumpStack();
                }
                result = AccessDecisionVoter.ACCESS_DENIED;
            }
            else
            {
                result =  AccessDecisionVoter.ACCESS_GRANTED;
            }
        }

        return result;
    }

    /**
     * Core RM read check
     *
     * @param nodeRef	node reference
     * @return int		see {@link AccessDecisionVoter}
     */
    public int checkRmRead(NodeRef nodeRef)
    {
    	int result = AccessDecisionVoter.ACCESS_ABSTAIN;

    	Map<Pair<String, NodeRef>, Integer> transactionCache = TransactionalResourceHelper.getMap("rm.security.checkRMRead");
    	Pair<String, NodeRef> key = new Pair<>(AuthenticationUtil.getRunAsUser(), nodeRef);

    	if (transactionCache.containsKey(key))
    	{
    		result = transactionCache.get(key);
    	}
    	else
    	{
	        if (permissionService.hasPermission(nodeRef, RMPermissionModel.READ_RECORDS) == AccessStatus.DENIED)
	        {
	            if (logger.isDebugEnabled())
	            {
	                logger.debug("\t\tUser does not have read record permission on node, access denied.  (nodeRef=" + nodeRef.toString() + ", user=" + AuthenticationUtil.getRunAsUser() + ")");
	            }
	            result = AccessDecisionVoter.ACCESS_DENIED;
	        }
	        else
	        {
		        // Get the file plan for the node
		        NodeRef filePlan = getFilePlanService().getFilePlan(nodeRef);
		        if (filePlan != null &&
		            hasViewCapability(filePlan) == AccessStatus.DENIED)
		        {
		            if (logger.isDebugEnabled())
		            {
		                logger.debug("\t\tUser does not have view records capability permission on node, access denied. (filePlan=" + filePlan.toString() + ", user=" + AuthenticationUtil.getRunAsUser() + ")");
		            }
		            result = AccessDecisionVoter.ACCESS_DENIED;
		        }
		        else if (!caveatConfigComponent.hasAccess(nodeRef))
		        {
		            result = AccessDecisionVoter.ACCESS_DENIED;
		        }
		        else
		        {
		            result = AccessDecisionVoter.ACCESS_GRANTED;
		        }
	        }

	        // cache result
	        transactionCache.put(key, result);
    	}

    	return result;
    }

    /**
     * Helper method to determine whether the current user has view capability on the file plan
     *
     * @param  filePlan	file plan
     * @return {@link AccessStatus}
     */
    private AccessStatus hasViewCapability(NodeRef filePlan)
    {
    	Map<Pair<String, NodeRef>, AccessStatus> transactionCache = TransactionalResourceHelper.getMap("rm.security.hasViewCapability");
    	Pair<String, NodeRef> key = new Pair<>(AuthenticationUtil.getRunAsUser(), filePlan);

    	if (transactionCache.containsKey(key))
    	{
    		return transactionCache.get(key);
    	}
    	else
    	{
    		AccessStatus result = permissionService.hasPermission(filePlan, ViewRecordsCapability.NAME);
    		transactionCache.put(key, result);
    		return result;
    	}
    }

    @SuppressWarnings("rawtypes")
    protected NodeRef getTestNode(MethodInvocation invocation, Class[] params, int position, boolean parent)
    {
        NodeRef testNodeRef = null;
        if (position < 0)
        {
            if (logger.isDebugEnabled())
            {
            	logger.debug("\tNothing to test permission against.");
            }
            testNodeRef = null;
        }
        else if (StoreRef.class.isAssignableFrom(params[position]))
        {
            if (invocation.getArguments()[position] != null)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("\tPermission test against the store - using permissions on the root node");
                }
                StoreRef storeRef = (StoreRef) invocation.getArguments()[position];
                if (nodeService.exists(storeRef))
                {
                    testNodeRef = nodeService.getRootNode(storeRef);
                }
            }
        }
        else if (NodeRef.class.isAssignableFrom(params[position]))
        {
            testNodeRef = (NodeRef) invocation.getArguments()[position];
            if (parent)
            {
                testNodeRef = nodeService.getPrimaryParent(testNodeRef).getParentRef();
                if (logger.isDebugEnabled())
                {
                    if (nodeService.exists(testNodeRef))
                    {
                        logger.debug("\tPermission test for parent on node " + nodeService.getPath(testNodeRef));
                    }
                    else
                    {
                        logger.debug("\tPermission test for parent on non-existing node " + testNodeRef);
                    }
                    logger.debug("\tPermission test for parent on node " + nodeService.getPath(testNodeRef));
                }
            }
            else
            {
                if (logger.isDebugEnabled())
                {
                    if (nodeService.exists(testNodeRef))
                    {
                        logger.debug("\tPermission test on node " + nodeService.getPath(testNodeRef));
                    }
                    else
                    {
                        logger.debug("\tPermission test on non-existing node " + testNodeRef);
                    }
                }
            }
        }
        else if (ChildAssociationRef.class.isAssignableFrom(params[position]))
        {
            if (invocation.getArguments()[position] != null)
            {
                if (parent)
                {
                    testNodeRef = ((ChildAssociationRef) invocation.getArguments()[position]).getParentRef();
                }
                else
                {
                    testNodeRef = ((ChildAssociationRef) invocation.getArguments()[position]).getChildRef();
                }
                if (logger.isDebugEnabled())
                {
                    if (nodeService.exists(testNodeRef))
                    {
                        logger.debug("\tPermission test on node " + nodeService.getPath(testNodeRef));
                    }
                    else
                    {
                        logger.debug("\tPermission test on non-existing node " + testNodeRef);
                    }
                }
            }
        }
        else if (AssociationRef.class.isAssignableFrom(params[position]) && invocation.getArguments()[position] != null)
        {
            if (parent)
            {
                testNodeRef = ((AssociationRef) invocation.getArguments()[position]).getSourceRef();
            }
            else
            {
                testNodeRef = ((AssociationRef) invocation.getArguments()[position]).getTargetRef();
            }
            if (logger.isDebugEnabled())
            {
                if (nodeService.exists(testNodeRef))
                {
                    logger.debug("\tPermission test on node " + nodeService.getPath(testNodeRef));
                }
                else
                {
                    logger.debug("\tPermission test on non-existing node " + testNodeRef);
                }
            }
        }
        return testNodeRef;
    }
}

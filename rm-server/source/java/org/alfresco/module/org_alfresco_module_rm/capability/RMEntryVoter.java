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
package org.alfresco.module.org_alfresco_module_rm.capability;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.ConfigAttribute;
import net.sf.acegisecurity.ConfigAttributeDefinition;
import net.sf.acegisecurity.vote.AccessDecisionVoter;

import org.alfresco.module.org_alfresco_module_rm.RecordsManagementService;
import org.alfresco.module.org_alfresco_module_rm.action.RecordsManagementAction;
import org.alfresco.module.org_alfresco_module_rm.capability.impl.CreateCapability;
import org.alfresco.module.org_alfresco_module_rm.capability.impl.UpdateCapability;
import org.alfresco.module.org_alfresco_module_rm.capability.impl.UpdatePropertiesCapability;
import org.alfresco.module.org_alfresco_module_rm.caveat.RMCaveatConfigComponent;
import org.alfresco.module.org_alfresco_module_rm.disposition.DispositionService;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.impl.SimplePermissionReference;
import org.alfresco.repo.security.permissions.impl.acegi.ACLEntryVoterException;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class RMEntryVoter extends RMSecurityCommon
                          implements AccessDecisionVoter, InitializingBean, ApplicationContextAware
{
    private static Log logger = LogFactory.getLog(RMEntryVoter.class);

    private static final String RM = "RM";
    private static final String RM_ALLOW = "RM_ALLOW";
    private static final String RM_DENY = "RM_DENY";
    private static final String RM_CAP = "RM_CAP";
    private static final String RM_ABSTAIN = "RM_ABSTAIN";
    private static final String RM_QUERY = "RM_QUERY";

    private NamespacePrefixResolver nspr;
    private NodeService nodeService;
    private PermissionService permissionService;
    private RMCaveatConfigComponent caveatConfigComponent;
    private DictionaryService dictionaryService;
    private RecordsManagementService recordsManagementService;   
    private DispositionService dispositionService;    
    private SearchService searchService;    
    private OwnableService ownableService;

    private CapabilityService capabilityService;
    
    private static HashMap<String, Policy> policies = new HashMap<String, Policy>();

    private HashSet<QName> protectedProperties = new HashSet<QName>();

    private HashSet<QName> protectedAspects = new HashSet<QName>();


    static
    {
        policies.put("Read", new ReadPolicy());
        policies.put("Create", new CreatePolicy());
        policies.put("Move", new MovePolicy());
        policies.put("Update", new UpdatePolicy());
        policies.put("Delete", new DeletePolicy());
        policies.put("UpdateProperties", new UpdatePropertiesPolicy());
        policies.put("Assoc", new AssocPolicy());
        policies.put("WriteContent", new WriteContentPolicy());
        policies.put("Capability", new CapabilityPolicy());
        policies.put("Declare", new DeclarePolicy());
        policies.put("ReadProperty", new ReadPropertyPolicy());

        // restrictedProperties.put(RecordsManagementModel.PROP_IS_CLOSED, value)

    }

    /**
     * Set the permission service
     * 
     * @param permissionService
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    /**
     * Set the node service
     * 
     * @param nodeService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * @param capabilityService     capability service
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }
    
    /**
     * Get the search service
     * @return  search service
     */
    public SearchService getSearchService()
    {
        if (searchService == null)
        {
            searchService = (SearchService)applicationContext.getBean("SearchService");
        }
        return searchService;
    }
    
    /**
     * @return
     */
    public OwnableService getOwnableService() 
    {
    	if (ownableService == null)
        {
    		ownableService = (OwnableService)applicationContext.getBean("ownableService");
        }
		return ownableService;
	}
    
    /**
     * Set the name space prefix resolver
     * 
     * @param nspr
     */
    public void setNamespacePrefixResolver(NamespacePrefixResolver nspr)
    {
        this.nspr = nspr;
    }

    public void setCaveatConfigComponent(RMCaveatConfigComponent caveatConfigComponent)
    {
        this.caveatConfigComponent = caveatConfigComponent;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public boolean supports(ConfigAttribute attribute)
    {
        if ((attribute.getAttribute() != null)
                && (attribute.getAttribute().equals(RM_ABSTAIN)
                        || attribute.getAttribute().equals(RM_QUERY) || attribute.getAttribute().equals(RM_ALLOW) || attribute.getAttribute().equals(RM_DENY)
                        || attribute.getAttribute().startsWith(RM_CAP) || attribute.getAttribute().startsWith(RM)))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    public boolean supports(Class clazz)
    {
        return (MethodInvocation.class.isAssignableFrom(clazz));
    }

    public void addProtectedProperties(Set<QName> properties)
    {
        protectedProperties.addAll(properties);
    }

    public void addProtectedAspects(Set<QName> aspects)
    {
        protectedAspects.addAll(aspects);
    }

    public Set<QName> getProtectedProperties()
    {
        return Collections.unmodifiableSet(protectedProperties);
    }

    public Set<QName> getProtetcedAscpects()
    {
        return Collections.unmodifiableSet(protectedAspects);
    }

    @SuppressWarnings("unchecked")
	public int vote(Authentication authentication, Object object, ConfigAttributeDefinition config)
    {
        if (logger.isDebugEnabled())
        {
            MethodInvocation mi = (MethodInvocation) object;
            logger.debug("Method: " + mi.getMethod().toString());
        }
        // The system user can do anything
        if (AuthenticationUtil.isRunAsUserTheSystemUser())
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Access granted for the system user");
            }
            return AccessDecisionVoter.ACCESS_GRANTED;
        }

        List<ConfigAttributeDefintion> supportedDefinitions = extractSupportedDefinitions(config);

        // No RM definitions so we do not vote
        if (supportedDefinitions.size() == 0)
        {
            return AccessDecisionVoter.ACCESS_ABSTAIN;
        }

        MethodInvocation invocation = (MethodInvocation) object;

        Method method = invocation.getMethod();
        Class[] params = method.getParameterTypes();

        // If there are only capability (RM_CAP) and policy (RM) entries non must deny 
        // If any abstain we deny
        // All present must vote to allow unless an explicit direction comes first (e.g. RM_ALLOW)
       
        for (ConfigAttributeDefintion cad : supportedDefinitions)
        {
            // Whatever is found first takes precedence
            if (cad.typeString.equals(RM_DENY))
            {
                return AccessDecisionVoter.ACCESS_DENIED;
            }
            else if (cad.typeString.equals(RM_ABSTAIN))
            {
                return AccessDecisionVoter.ACCESS_ABSTAIN;
            }
            else if (cad.typeString.equals(RM_ALLOW))
            {
                return AccessDecisionVoter.ACCESS_GRANTED;
            }
            // RM_QUERY is a special case - the entry is allowed and filtering sorts out the results
            // It is distinguished from RM_ALLOW so query may have additional behaviour in the future
            else if (cad.typeString.equals(RM_QUERY))
            {
                return AccessDecisionVoter.ACCESS_GRANTED;
            }
            // Ignore config that references method arguments that do not exist
            // Arguably we should deny here but that requires a full impact analysis
            // These entries effectively abstain
            else if (((cad.parameters.get(0) != null) && (cad.parameters.get(0) >= invocation.getArguments().length))            		                                      
                    || ((cad.parameters.get(1) != null) && (cad.parameters.get(1) >= invocation.getArguments().length)))
            {
                continue;
            }
            else if (cad.typeString.equals(RM_CAP))
            {
                switch(checkCapability(invocation, params, cad))
                {
                case  AccessDecisionVoter.ACCESS_DENIED:
                    return AccessDecisionVoter.ACCESS_DENIED;
                case AccessDecisionVoter.ACCESS_ABSTAIN:
                    if(logger.isDebugEnabled())
                    {
                        if(logger.isTraceEnabled())
                        {
                            logger.trace("Capability " + cad.required + " abstained for " + invocation.getMethod(), new IllegalStateException());
                        }
                        else
                        {
                            logger.debug("Capability " + cad.required + " abstained for " + invocation.getMethod());
                        }
                    }
                    // abstain denies
                    return AccessDecisionVoter.ACCESS_DENIED;
                case AccessDecisionVoter.ACCESS_GRANTED:
                    break;
                }
            }
            else if (cad.typeString.equals(RM))
            {
                switch(checkPolicy(invocation, params, cad))
                {
                case  AccessDecisionVoter.ACCESS_DENIED:
                    return AccessDecisionVoter.ACCESS_DENIED;
                case AccessDecisionVoter.ACCESS_ABSTAIN:
                    if(logger.isDebugEnabled())
                    {
                        if(logger.isTraceEnabled())
                        {
                            logger.trace("Policy " + cad.policyName + " abstained for " + invocation.getMethod(), new IllegalStateException());
                        }
                        else
                        {
                            logger.debug("Policy " + cad.policyName + " abstained for " + invocation.getMethod());
                        }
                    }
                    // abstain denies
                    return AccessDecisionVoter.ACCESS_DENIED;
                case AccessDecisionVoter.ACCESS_GRANTED:
                    break;
                }
            }
        }
        
        // all voted to allow

        return AccessDecisionVoter.ACCESS_GRANTED;

    }

    @SuppressWarnings("unchecked")
	private int checkCapability(MethodInvocation invocation, Class[] params, ConfigAttributeDefintion cad)
    {
        NodeRef testNodeRef = getTestNode(getNodeService(), getRecordsManagementService(), invocation, params, cad.parameters.get(0), cad.parent);
        if (testNodeRef == null)
        {
            return AccessDecisionVoter.ACCESS_ABSTAIN;
        }
        Capability capability = capabilityService.getCapability(cad.required.getName());
        if (capability == null)
        {
            return AccessDecisionVoter.ACCESS_DENIED;
        }
        return capability.hasPermissionRaw(testNodeRef);

    }

    @SuppressWarnings("unchecked")
	private static QName getType(NodeService nodeService, MethodInvocation invocation, Class[] params, int position, boolean parent)
    {
        if (QName.class.isAssignableFrom(params[position]))
        {
            if (invocation.getArguments()[position] != null)
            {
                QName qname = (QName) invocation.getArguments()[position];
                return qname;
            }
        }
        else if (NodeRef.class.isAssignableFrom(params[position]))
        {
            if (invocation.getArguments()[position] != null)
            {
                NodeRef nodeRef = (NodeRef) invocation.getArguments()[position];
                return nodeService.getType(nodeRef);
            }
        }

        return null;
    }

    @SuppressWarnings("unchecked")
	private static QName getQName(MethodInvocation invocation, Class[] params, int position)
    {
        if (QName.class.isAssignableFrom(params[position]))
        {
            if (invocation.getArguments()[position] != null)
            {
                QName qname = (QName) invocation.getArguments()[position];
                return qname;
            }
        }
        throw new ACLEntryVoterException("Unknown type");
    }

    @SuppressWarnings("unchecked")
	private static Serializable getProperty(MethodInvocation invocation, Class[] params, int position)
    {
        if (invocation.getArguments()[position] == null)
        {
            return null;
        }
        if (Serializable.class.isAssignableFrom(params[position]))
        {
            if (invocation.getArguments()[position] != null)
            {
                Serializable property = (Serializable) invocation.getArguments()[position];
                return property;
            }
        }
        throw new ACLEntryVoterException("Unknown type");
    }

    @SuppressWarnings("unchecked")
	private static Map<QName, Serializable> getProperties(MethodInvocation invocation, Class[] params, int position)
    {
        if (invocation.getArguments()[position] == null)
        {
            return null;
        }
        if (Map.class.isAssignableFrom(params[position]))
        {
            if (invocation.getArguments()[position] != null)
            {
                Map<QName, Serializable> properties = (Map<QName, Serializable>) invocation.getArguments()[position];
                return properties;
            }
        }
        throw new ACLEntryVoterException("Unknown type");
    }

    @SuppressWarnings("unchecked")
	private static NodeRef getTestNode(NodeService nodeService, RecordsManagementService rmService, MethodInvocation invocation, Class[] params, int position, boolean parent)
    {
        NodeRef testNodeRef = null;
        if (position < 0)
        {
        	// Test against the fileplan root node
        	List<NodeRef> rmRoots = rmService.getFilePlans();
        	if (rmRoots.size() != 0)
        	{
        		// TODO for now we can take the first one as we only support a single rm site
        		testNodeRef = rmRoots.get(0);
        		
        		if (logger.isDebugEnabled())
                {
                    logger.debug("\tPermission test against the rm root node " + nodeService.getPath(testNodeRef));
                }
        	}
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
        else if (AssociationRef.class.isAssignableFrom(params[position]))
        {
            if (invocation.getArguments()[position] != null)
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
        }
        return testNodeRef;
    }

    @SuppressWarnings("unchecked")
	private int checkPolicy(MethodInvocation invocation, Class[] params, ConfigAttributeDefintion cad)
    {
        Policy policy = policies.get(cad.policyName);
        if (policy == null)
        {
            return AccessDecisionVoter.ACCESS_GRANTED;
        }
        else
        {
            return policy.evaluate(this.nodeService, this.recordsManagementService, this.capabilityService, invocation, params, cad);
        }
    }

    public void afterPropertiesSet() throws Exception
    {
        // TODO Auto-generated method stub

    }

    @SuppressWarnings("unchecked")
	private List<ConfigAttributeDefintion> extractSupportedDefinitions(ConfigAttributeDefinition config)
    {
        List<ConfigAttributeDefintion> definitions = new ArrayList<ConfigAttributeDefintion>(2);
        Iterator iter = config.getConfigAttributes();

        while (iter.hasNext())
        {
            ConfigAttribute attr = (ConfigAttribute) iter.next();

            if (this.supports(attr))
            {
                definitions.add(new ConfigAttributeDefintion(attr));
            }

        }
        return definitions;
    }

    /**
     * @return the nodeService
     */
    public NodeService getNodeService()
    {
        return nodeService;
    }

    /**
     * @return the permissionService
     */
    public PermissionService getPermissionService()
    {
        return permissionService;
    }

    /**
     * @return the caveatConfigService
     */
    public RMCaveatConfigComponent getCaveatConfigComponent()
    {
        return caveatConfigComponent;
    }

    /**
     * @param recordsManagementService
     *            the recordsManagementService to set
     */
    public void setRecordsManagementService(RecordsManagementService recordsManagementService)
    {
        this.recordsManagementService = recordsManagementService;
    }

    /**
     * @return the recordsManagementService
     */
    public RecordsManagementService getRecordsManagementService()
    {
        return recordsManagementService;
    }
    
    public void setDispositionService(DispositionService dispositionService)
    {
        this.dispositionService = dispositionService;
    }
    
    public DispositionService getDispositionService()
    {
        return dispositionService;
    }

    /**
     * @return the dictionaryService
     */
    public DictionaryService getDictionaryService()
    {
        return dictionaryService;
    }

    public boolean isProtectedAspect(NodeRef nodeRef, QName aspectQName)
    {
        if(protectedAspects.contains(aspectQName))
        {
            for(Capability capability : capabilityService.getCapabilities())
            {
                for(RecordsManagementAction action : capability.getActions())
                {
                    if(action.getProtectedAspects().contains(aspectQName))
                    {
                        if(action.isExecutable(nodeRef, null))
                        {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean isProtectedProperty(NodeRef nodeRef, QName propertyQName)
    {
        if(protectedProperties.contains(propertyQName))
        {
            for(Capability capability : capabilityService.getCapabilities())
            {
                for(RecordsManagementAction action : capability.getActions())
                {
                    if(action.getProtectedProperties().contains(propertyQName))
                    {
                        if(action.isExecutable(nodeRef, null))
                        {
                            return false;
                        }
                    }
                }
            }
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean includesProtectedPropertyChange(NodeRef nodeRef, Map<QName, Serializable> properties)
    {
        Map<QName, Serializable> originals = nodeService.getProperties(nodeRef);
        for (QName test : properties.keySet())
        {
            if (isProtectedProperty(nodeRef, test))
            {
                if (!EqualsHelper.nullSafeEquals(originals.get(test), properties.get(test)))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private class ConfigAttributeDefintion
    {
        String typeString;

        String policyName;

        SimplePermissionReference required;

        HashMap<Integer, Integer> parameters = new HashMap<Integer, Integer>(2, 1.0f);

        boolean parent = false;

        ConfigAttributeDefintion(ConfigAttribute attr)
        {
            StringTokenizer st = new StringTokenizer(attr.getAttribute(), ".", false);
            if (st.countTokens() < 1)
            {
                throw new ACLEntryVoterException("There must be at least one token in a config attribute");
            }
            typeString = st.nextToken();

            if (!(typeString.equals(RM) || typeString.equals(RM_ALLOW) || typeString.equals(RM_CAP) || typeString.equals(RM_DENY) || typeString.equals(RM_QUERY) || typeString
                    .equals(RM_ABSTAIN)))
            {
                throw new ACLEntryVoterException("Invalid type: must be ACL_NODE, ACL_PARENT or ACL_ALLOW");
            }

            if (typeString.equals(RM))
            {
                policyName = st.nextToken();
                int position = 0;
                while (st.hasMoreElements())
                {
                    String numberString = st.nextToken();
                    Integer value = Integer.parseInt(numberString);
                    parameters.put(position, value);
                    position++;
                }
            }
            else if (typeString.equals(RM_CAP))
            {
                String numberString = st.nextToken();
                String qNameString = st.nextToken();
                String permissionString = st.nextToken();

                Integer value = Integer.parseInt(numberString);
                parameters.put(0, value);

                QName qName = QName.createQName(qNameString, nspr);

                required = SimplePermissionReference.getPermissionReference(qName, permissionString);

                if (st.hasMoreElements())
                {
                    parent = true;
                }
            }
        }
    }

    interface Policy
    {
        /**
         * 
         * @param nodeService
         * @param rmService
         * @param capabilitiesService
         * @param invocation
         * @param params
         * @param cad
         * @return
         */
        @SuppressWarnings("unchecked")
		int evaluate(
                NodeService nodeService,               
                RecordsManagementService rmService, 
                CapabilityService capabilitiesService, 
                MethodInvocation invocation, 
                Class[] params, 
                ConfigAttributeDefintion cad);
    }

    private static class ReadPolicy implements Policy
    {

        @SuppressWarnings("unchecked")
		public int evaluate(
                NodeService nodeService,               
                RecordsManagementService rmService, 
                CapabilityService capabilityService,  
                MethodInvocation invocation, 
                Class[] params, 
                ConfigAttributeDefintion cad)
        {
            NodeRef testNodeRef = getTestNode(nodeService, rmService, invocation, params, cad.parameters.get(0), cad.parent);
            return capabilityService.getCapability(RMPermissionModel.VIEW_RECORDS).evaluate(testNodeRef);
        }

    }

    private static class CreatePolicy implements Policy
    {

        @SuppressWarnings("unchecked")
		public int evaluate(
                NodeService nodeService,               
                RecordsManagementService rmService, 
                CapabilityService capabilityService, 
                MethodInvocation invocation, 
                Class[] params, 
                ConfigAttributeDefintion cad)
        {

            NodeRef destination = getTestNode(nodeService, rmService, invocation, params, cad.parameters.get(0), cad.parent);
            QName type = getType(nodeService, invocation, params, cad.parameters.get(1), cad.parent);
            // linkee is not null for creating secondary child assocs
            NodeRef linkee = getTestNode(nodeService, rmService, invocation, params, cad.parameters.get(1), cad.parent);
            QName assocType = null;
            if(cad.parameters.size() > 2)
            {
                assocType = getType(nodeService, invocation, params, cad.parameters.get(2), cad.parent);
            }

            return ((CreateCapability)capabilityService.getCapability("Create")).evaluate(destination, linkee, type, assocType);
        }

    }

    private static class MovePolicy implements Policy
    {

        @SuppressWarnings("unchecked")
		public int evaluate(
                NodeService nodeService,               
                RecordsManagementService rmService, 
                CapabilityService capabilityService,  
                MethodInvocation invocation, 
                Class[] params, 
                ConfigAttributeDefintion cad)
        {

            NodeRef movee = null;
            if (cad.parameters.get(0) > -1)
            {
                movee = getTestNode(nodeService, rmService, invocation, params, cad.parameters.get(0), cad.parent);
            }

            NodeRef destination = null;
            if (cad.parameters.get(1) > -1)
            {
                destination = getTestNode(nodeService, rmService, invocation, params, cad.parameters.get(1), cad.parent);
            }

            if ((movee != null) && (destination != null))
            {
                return capabilityService.getCapability("Move").evaluate(movee, destination);
            }
            else
            {
                return AccessDecisionVoter.ACCESS_DENIED;
            }

        }
    }

    private static class UpdatePolicy implements Policy
    {

        @SuppressWarnings("unchecked")
		public int evaluate(
                NodeService nodeService,               
                RecordsManagementService rmService, 
                CapabilityService capabilityService,  
                MethodInvocation invocation, 
                Class[] params, 
                ConfigAttributeDefintion cad)
        {
            NodeRef updatee = getTestNode(nodeService, rmService, invocation, params, cad.parameters.get(0), cad.parent);
            QName aspectQName = null;
            if (cad.parameters.size() > 1)
            {
                if (cad.parameters.get(1) > -1)
                {
                    aspectQName = getQName(invocation, params, cad.parameters.get(1));
                }
            }
            Map<QName, Serializable> properties = null;
            if (cad.parameters.size() > 2)
            {
                if (cad.parameters.get(2) > -1)
                {
                    properties = getProperties(invocation, params, cad.parameters.get(2));
                }
            }
            
            UpdateCapability updateCapability = (UpdateCapability)capabilityService.getCapability("Update");
            return updateCapability.evaluate(updatee, aspectQName, properties);
        }

    }

    private static class DeletePolicy implements Policy
    {

        @SuppressWarnings("unchecked")
		public int evaluate(
                NodeService nodeService,               
                RecordsManagementService rmService, 
                CapabilityService capabilityService,  
                MethodInvocation invocation, 
                Class[] params, 
                ConfigAttributeDefintion cad)
        {
            NodeRef deletee = null;
            if (cad.parameters.get(0) > -1)
            {
                deletee = getTestNode(nodeService, rmService, invocation, params, cad.parameters.get(0), cad.parent);
            }
            if (deletee != null)
            {

                return capabilityService.getCapability("Delete").evaluate(deletee);

            }
            else
            {
                return AccessDecisionVoter.ACCESS_DENIED;
            }
        }

    }

    private static class UpdatePropertiesPolicy implements Policy
    {
        @SuppressWarnings("unchecked")
		public int evaluate(
                NodeService nodeService,               
                RecordsManagementService rmService, 
                CapabilityService capabilityService,  
                MethodInvocation invocation, 
                Class[] params, 
                ConfigAttributeDefintion cad)
        {
            NodeRef updatee = getTestNode(nodeService, rmService, invocation, params, cad.parameters.get(0), cad.parent);
            Map<QName, Serializable> properties;
            if (QName.class.isAssignableFrom(params[cad.parameters.get(1)]))
            {
                // single update/delete
                // We have a specific property
                QName propertyQName = getQName(invocation, params, cad.parameters.get(1));
                properties = new HashMap<QName, Serializable>(1, 1.0f);
                if (cad.parameters.size() > 2)
                {
                    properties.put(propertyQName, getProperty(invocation, params, cad.parameters.get(2)));
                }
                else
                {
                    properties.put(propertyQName, null);
                }
            }
            else
            {
                properties = getProperties(invocation, params, cad.parameters.get(1));
            }

            return ((UpdatePropertiesCapability)capabilityService.getCapability("UpdateProperties")).evaluate(updatee, properties);
        }

    }

    private static class AssocPolicy implements Policy
    {

        @SuppressWarnings("unchecked")
		public int evaluate(
                NodeService nodeService,               
                RecordsManagementService rmService, 
                CapabilityService capabilityService,  
                MethodInvocation invocation, 
                Class[] params, 
                ConfigAttributeDefintion cad)
        {
            Policy policy = policies.get("Read");
            if (policy == null)
            {
                return AccessDecisionVoter.ACCESS_DENIED;
            }
            else
            {
                return policy.evaluate(nodeService, rmService, capabilityService, invocation, params, cad);
            }
        }

    }

    private static class WriteContentPolicy implements Policy
    {

        @SuppressWarnings("unchecked")
		public int evaluate(
                NodeService nodeService,               
                RecordsManagementService rmService, 
                CapabilityService capabilityService,  
                MethodInvocation invocation, 
                Class[] params, 
                ConfigAttributeDefintion cad)
        {
            NodeRef updatee = getTestNode(nodeService, rmService, invocation, params, cad.parameters.get(0), cad.parent);
            return capabilityService.getCapability("WriteContent").evaluate(updatee);
        }

    }

    private static class CapabilityPolicy implements Policy
    {

        @SuppressWarnings("unchecked")
		public int evaluate(
                NodeService nodeService,               
                RecordsManagementService rmService, 
                CapabilityService capabilityService,  
                MethodInvocation invocation, 
                Class[] params, 
                ConfigAttributeDefintion cad)
        {
            NodeRef assignee = getTestNode(nodeService, rmService, invocation, params, cad.parameters.get(0), cad.parent);
            return capabilityService.getCapability(RMPermissionModel.MANAGE_ACCESS_CONTROLS).evaluate(assignee);
        }

    }

    private static class DeclarePolicy implements Policy
    {

        @SuppressWarnings("unchecked")
		public int evaluate(
                NodeService nodeService,               
                RecordsManagementService rmService, 
                CapabilityService capabilityService,  
                MethodInvocation invocation, 
                Class[] params, 
                ConfigAttributeDefintion cad)
        {
            NodeRef declaree = getTestNode(nodeService, rmService, invocation, params, cad.parameters.get(0), cad.parent);
            return capabilityService.getCapability("Declare").evaluate(declaree);
        }

    }
    
    private static class ReadPropertyPolicy implements Policy
    {

        @SuppressWarnings("unchecked")
		public int evaluate(
                NodeService nodeService,               
                RecordsManagementService rmService, 
                CapabilityService capabilityService,  
                MethodInvocation invocation, 
                Class[] params, 
                ConfigAttributeDefintion cad)
        {
            NodeRef nodeRef = getTestNode(nodeService, rmService, invocation, params, cad.parameters.get(0), cad.parent);
            QName propertyQName = getQName(invocation, params, cad.parameters.get(1));
            if(propertyQName.equals(RecordsManagementModel.PROP_HOLD_REASON))
            {
                return capabilityService.getCapability(RMPermissionModel.VIEW_UPDATE_REASONS_FOR_FREEZE).evaluate(nodeRef);
            }
            else
            {
                return AccessDecisionVoter.ACCESS_GRANTED;
            }
        }

    }

    private ApplicationContext applicationContext;
    
    @Override
    public void setApplicationContext(ApplicationContext applicationContext)
            throws BeansException
    {
        this.applicationContext = applicationContext;
    }
    
}

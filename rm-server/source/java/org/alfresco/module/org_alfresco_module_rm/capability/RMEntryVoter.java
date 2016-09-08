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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.ConfigAttribute;
import net.sf.acegisecurity.vote.AccessDecisionVoter;

import org.alfresco.module.org_alfresco_module_rm.capability.policy.ConfigAttributeDefinition;
import org.alfresco.module.org_alfresco_module_rm.capability.policy.Policy;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Records managment entry voter.
 * 
 * @author Roy Wetherall, Andy Hind
 */
public class RMEntryVoter extends RMSecurityCommon
                          implements AccessDecisionVoter, InitializingBean, ApplicationContextAware, PolicyRegister
{
	/** Logger */
    private static Log logger = LogFactory.getLog(RMEntryVoter.class);

    /** Namespace resolver */
    private NamespacePrefixResolver nspr;
    
    /** Search service */
    private SearchService searchService;
    
    /** Ownable service */
    private OwnableService ownableService;
    
    /** Capability Service */
    private CapabilityService capabilityService;
    
    /** Policy map */
    private HashMap<String, Policy> policies = new HashMap<String, Policy>();
    
    /** Application context */
    private ApplicationContext applicationContext;
    
    /**
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException
    {
        this.applicationContext = applicationContext;        
    }

    /**
     * @param capabilityService     capability service
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }
    
    /**
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
     * @return	ownable service
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
     * @param nspr	namespace prefix resolver
     */
    public void setNamespacePrefixResolver(NamespacePrefixResolver nspr)
    {
        this.nspr = nspr;
    }
    
    /**
     * Register a policy the voter
     * 
     * @param policy    policy
     */
    public void registerPolicy(Policy policy)
    {
        policies.put(policy.getName(), policy);
    }

    /**
     * @see net.sf.acegisecurity.vote.AccessDecisionVoter#supports(net.sf.acegisecurity.ConfigAttribute)
     */
    @Override
    public boolean supports(ConfigAttribute attribute)
    {
        if ((attribute.getAttribute() != null) &&
            (attribute.getAttribute().equals(ConfigAttributeDefinition.RM_ABSTAIN) ||
             attribute.getAttribute().equals(ConfigAttributeDefinition.RM_QUERY) || 
             attribute.getAttribute().equals(ConfigAttributeDefinition.RM_ALLOW) || 
             attribute.getAttribute().equals(ConfigAttributeDefinition.RM_DENY) ||
             attribute.getAttribute().startsWith(ConfigAttributeDefinition.RM_CAP) || 
             attribute.getAttribute().startsWith(ConfigAttributeDefinition.RM)))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * @see net.sf.acegisecurity.vote.AccessDecisionVoter#supports(java.lang.Class)
     */
    @SuppressWarnings("rawtypes")
    public boolean supports(Class clazz)
    {
        return (MethodInvocation.class.isAssignableFrom(clazz));
    }

    /**
     * @see net.sf.acegisecurity.vote.AccessDecisionVoter#vote(net.sf.acegisecurity.Authentication, java.lang.Object, net.sf.acegisecurity.ConfigAttributeDefinition)
     */
    @SuppressWarnings("rawtypes")
	public int vote(Authentication authentication, Object object, net.sf.acegisecurity.ConfigAttributeDefinition config)
    {
    	MethodInvocation mi = (MethodInvocation)object;
    	
    	if (TransactionalResourceHelper.isResourcePresent("voting") == true)
    	{
    		if (logger.isDebugEnabled())
            {
                logger.debug(" .. grant access already voting: " + mi.getMethod().getDeclaringClass().getName() + "." + mi.getMethod().getName());
            }
    		return AccessDecisionVoter.ACCESS_GRANTED;    		
    	}
    	
    	if (logger.isDebugEnabled())
        {        	
            logger.debug("Method: " + mi.getMethod().getDeclaringClass().getName() + "." + mi.getMethod().getName());
        }
    	
    	AlfrescoTransactionSupport.bindResource("voting", true);
    	try
    	{	        
	        // The system user can do anything
	        if (AuthenticationUtil.isRunAsUserTheSystemUser())
	        {
	            if (logger.isDebugEnabled())
	            {
	                logger.debug("Access granted for the system user");
	            }
	            return AccessDecisionVoter.ACCESS_GRANTED;
	        }
	
	        List<ConfigAttributeDefinition> supportedDefinitions = extractSupportedDefinitions(config);
	
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
	       
	        for (ConfigAttributeDefinition cad : supportedDefinitions)
	        {
	            // Whatever is found first takes precedence
	            if (cad.getTypeString().equals(ConfigAttributeDefinition.RM_DENY))
	            {
	                return AccessDecisionVoter.ACCESS_DENIED;
	            }
	            else if (cad.getTypeString().equals(ConfigAttributeDefinition.RM_ABSTAIN))
	            {
	                return AccessDecisionVoter.ACCESS_ABSTAIN;
	            }
	            else if (cad.getTypeString().equals(ConfigAttributeDefinition.RM_ALLOW))
	            {
	                return AccessDecisionVoter.ACCESS_GRANTED;
	            }
	            // RM_QUERY is a special case - the entry is allowed and filtering sorts out the results
	            // It is distinguished from RM_ALLOW so query may have additional behaviour in the future
	            else if (cad.getTypeString().equals(ConfigAttributeDefinition.RM_QUERY))
	            {
	                return AccessDecisionVoter.ACCESS_GRANTED;
	            }
	            // Ignore config that references method arguments that do not exist
	            // Arguably we should deny here but that requires a full impact analysis
	            // These entries effectively abstain
	            else if (((cad.getParameters().get(0) != null) && (cad.getParameters().get(0) >= invocation.getArguments().length)) ||            		                                      
	                     ((cad.getParameters().get(1) != null) && (cad.getParameters().get(1) >= invocation.getArguments().length)))
	            {
	                continue;
	            }
	            else if (cad.getTypeString().equals(ConfigAttributeDefinition.RM_CAP))
	            {
	                switch(checkCapability(invocation, params, cad))
	                {
		                case  AccessDecisionVoter.ACCESS_DENIED:
		                {
		                    return AccessDecisionVoter.ACCESS_DENIED;
		                }
		                case AccessDecisionVoter.ACCESS_ABSTAIN:
		                {
		                    if(logger.isDebugEnabled())
		                    {
		                        if(logger.isTraceEnabled())
		                        {
		                            logger.trace("Capability " + cad.getRequired() + " abstained for " + invocation.getMethod(), new IllegalStateException());
		                        }
		                        else
		                        {
		                            logger.debug("Capability " + cad.getRequired() + " abstained for " + invocation.getMethod());
		                        }
		                    }
		                    // abstain denies
		                    return AccessDecisionVoter.ACCESS_DENIED;
		                }
		                case AccessDecisionVoter.ACCESS_GRANTED:
		                {
		                    break;
		                }
	                }
	            }
	            else if (cad.getTypeString().equals(ConfigAttributeDefinition.RM))
	            {
	                switch(checkPolicy(invocation, params, cad))
	                {
		                case  AccessDecisionVoter.ACCESS_DENIED:
		                {
		                    return AccessDecisionVoter.ACCESS_DENIED;
		                }
		                case AccessDecisionVoter.ACCESS_ABSTAIN:
		                {
		                    if(logger.isDebugEnabled())
		                    {
		                        if(logger.isTraceEnabled())
		                        {
		                            logger.trace("Policy " + cad.getPolicyName() + " abstained for " + invocation.getMethod(), new IllegalStateException());
		                        }
		                        else
		                        {
		                            logger.debug("Policy " + cad.getPolicyName() + " abstained for " + invocation.getMethod());
		                        }
		                    }
		                    // abstain denies
		                    return AccessDecisionVoter.ACCESS_DENIED;
		                }
		                case AccessDecisionVoter.ACCESS_GRANTED:
		                {
		                    break;
		                }
	                }
	            }
	        }
    	}
    	finally
    	{
    		AlfrescoTransactionSupport.unbindResource("voting");
    	}
        
        // all voted to allow
        return AccessDecisionVoter.ACCESS_GRANTED;
    }

    /**
     * 
     * @param invocation
     * @param params
     * @param cad
     * @return
     */
    @SuppressWarnings("rawtypes")
	private int checkCapability(MethodInvocation invocation, Class[] params, ConfigAttributeDefinition cad)
    {
        NodeRef testNodeRef = getTestNode(invocation, params, cad.getParameters().get(0), cad.isParent());
        if (testNodeRef == null)
        {
            return AccessDecisionVoter.ACCESS_ABSTAIN;
        }
        Capability capability = capabilityService.getCapability(cad.getRequired().getName());
        if (capability == null)
        {
            return AccessDecisionVoter.ACCESS_DENIED;
        }
        return capability.hasPermissionRaw(testNodeRef);

    }

    /**
     * 
     * @param invocation
     * @param params
     * @param cad
     * @return
     */
    @SuppressWarnings("rawtypes")
	private int checkPolicy(MethodInvocation invocation, Class[] params, ConfigAttributeDefinition cad)
    {
        Policy policy = policies.get(cad.getPolicyName());
        if (policy == null)
        {
            return AccessDecisionVoter.ACCESS_GRANTED;
        }
        else
        {
            return policy.evaluate(invocation, params, cad);
        }
    }

    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet() throws Exception
    {       
    }

    /**
     * 
     * @param config
     * @return
     */
    @SuppressWarnings("rawtypes")
	private List<ConfigAttributeDefinition> extractSupportedDefinitions(net.sf.acegisecurity.ConfigAttributeDefinition config)
    {
        List<ConfigAttributeDefinition> definitions = new ArrayList<ConfigAttributeDefinition>(2);
        Iterator iter = config.getConfigAttributes();

        while (iter.hasNext())
        {
            ConfigAttribute attr = (ConfigAttribute) iter.next();

            if (this.supports(attr))
            {
                definitions.add(new ConfigAttributeDefinition(attr, nspr));
            }

        }
        return definitions;
    }    
}

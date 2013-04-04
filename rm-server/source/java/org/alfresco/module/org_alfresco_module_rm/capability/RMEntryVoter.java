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

public class RMEntryVoter extends RMSecurityCommon
                          implements AccessDecisionVoter, InitializingBean, ApplicationContextAware
{
    private static Log logger = LogFactory.getLog(RMEntryVoter.class);

    private NamespacePrefixResolver nspr; 
    private SearchService searchService;    
    private OwnableService ownableService;
    private CapabilityService capabilityService;
    
    private HashMap<String, Policy> policies = new HashMap<String, Policy>();

//    static
//    {
//        policies.put("Read", new ReadPolicy());
//        policies.put("Create", new CreatePolicy());
//        policies.put("Move", new MovePolicy());
//        policies.put("Update", new UpdatePolicy());
//        policies.put("Delete", new DeletePolicy());
//        policies.put("UpdateProperties", new UpdatePropertiesPolicy());
//        policies.put("Assoc", new AssocPolicy());
//        policies.put("WriteContent", new WriteContentPolicy());
//        policies.put("Capability", new CapabilityPolicy());
//        policies.put("Declare", new DeclarePolicy());
//        policies.put("ReadProperty", new ReadPropertyPolicy());
//    }
    
    private ApplicationContext applicationContext;
    
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
     * 
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
        if ((attribute.getAttribute() != null)
                && (attribute.getAttribute().equals(ConfigAttributeDefinition.RM_ABSTAIN)
                        || attribute.getAttribute().equals(ConfigAttributeDefinition.RM_QUERY) || attribute.getAttribute().equals(ConfigAttributeDefinition.RM_ALLOW) || attribute.getAttribute().equals(ConfigAttributeDefinition.RM_DENY)
                        || attribute.getAttribute().startsWith(ConfigAttributeDefinition.RM_CAP) || attribute.getAttribute().startsWith(ConfigAttributeDefinition.RM)))
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

    @SuppressWarnings("unchecked")
	public int vote(Authentication authentication, Object object, net.sf.acegisecurity.ConfigAttributeDefinition config)
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
                    return AccessDecisionVoter.ACCESS_DENIED;
                case AccessDecisionVoter.ACCESS_ABSTAIN:
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
                case AccessDecisionVoter.ACCESS_GRANTED:
                    break;
                }
            }
            else if (cad.getTypeString().equals(ConfigAttributeDefinition.RM))
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
                            logger.trace("Policy " + cad.getPolicyName() + " abstained for " + invocation.getMethod(), new IllegalStateException());
                        }
                        else
                        {
                            logger.debug("Policy " + cad.getPolicyName() + " abstained for " + invocation.getMethod());
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

    @SuppressWarnings("unchecked")
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

    public void afterPropertiesSet() throws Exception
    {
        // TODO Auto-generated method stub

    }

    @SuppressWarnings("unchecked")
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

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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.capability.policy.ConfigAttributeDefinition;
import org.alfresco.module.org_alfresco_module_rm.capability.policy.Policy;
import org.alfresco.module.org_alfresco_module_rm.security.RMMethodSecurityInterceptor;
import org.alfresco.module.org_alfresco_module_rm.util.AlfrescoTransactionSupport;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.module.org_alfresco_module_rm.util.TransactionalResourceHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.ConfigAttribute;
import net.sf.acegisecurity.vote.AccessDecisionVoter;

/**
 * Records managment entry voter.
 *
 * @author Roy Wetherall, Andy Hind
 */
public class RMEntryVoter extends RMSecurityCommon
                          implements AccessDecisionVoter, InitializingBean, PolicyRegister
{
	/** Logger */
    private static Log logger = LogFactory.getLog(RMEntryVoter.class);

    /** Namespace resolver */
    private NamespacePrefixResolver nspr;

    /** Capability Service */
    private CapabilityService capabilityService;
    
    /** Transactional Resource Helper */
    private TransactionalResourceHelper transactionalResourceHelper;
    
    /** Alfresco transaction support */
    private AlfrescoTransactionSupport alfrescoTransactionSupport;
    
    /** authentication util */
    private AuthenticationUtil authenticationUtil;

    /** Policy map */
    private Map<String, Policy> policies = new HashMap<>();

    /**
     * @param capabilityService     capability service
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }

    /**
     * @param nspr	namespace prefix resolver
     */
    public void setNamespacePrefixResolver(NamespacePrefixResolver nspr)
    {
        this.nspr = nspr;
    }
    
    /**
     * @param transactionalResourceHelper   transactional resource helper
     */
    public void setTransactionalResourceHelper(TransactionalResourceHelper transactionalResourceHelper)
    {
        this.transactionalResourceHelper = transactionalResourceHelper;
    }
    
    /**
     * @param alfrescoTransactionSupport    alfresco transaction support helper
     */
    public void setAlfrescoTransactionSupport(AlfrescoTransactionSupport alfrescoTransactionSupport)
    {
        this.alfrescoTransactionSupport = alfrescoTransactionSupport;
    }
    
    /**
     * @param authenticationUtil    authentication util
     */
    public void setAuthenticationUtil(AuthenticationUtil authenticationUtil)
    {
        this.authenticationUtil = authenticationUtil;
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
    public boolean supports(ConfigAttribute configAttribute)
    {
        boolean supports = false;
        String attribute = configAttribute.getAttribute();
        if (StringUtils.isNotBlank(attribute) &&
            (attribute.equals(ConfigAttributeDefinition.RM_ABSTAIN) ||
             attribute.equals(ConfigAttributeDefinition.RM_QUERY) ||
             attribute.equals(ConfigAttributeDefinition.RM_ALLOW) ||
             attribute.equals(ConfigAttributeDefinition.RM_DENY) ||
             attribute.startsWith(ConfigAttributeDefinition.RM_CAP) ||
             attribute.startsWith(ConfigAttributeDefinition.RM)))
        {
            supports = true;
        }
        return supports;
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
        // logging
        RMMethodSecurityInterceptor.isRMSecurityChecked(true);

    	MethodInvocation mi = (MethodInvocation)object;

    	if (transactionalResourceHelper.isResourcePresent("voting"))
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

    	alfrescoTransactionSupport.bindResource("voting", true);
    	try
    	{
	        // The system user can do anything
	        if (authenticationUtil.isRunAsUserTheSystemUser())
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

	        // check we have an instance of a method invocation
	        if (!(object instanceof MethodInvocation))
	        {
	            // we expect a method invocation
	            throw new AlfrescoRuntimeException("Passed object is not an instance of MethodInvocation as expected.");
	        }
	        
	        // get information about the method
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
	                // log message
	                RMMethodSecurityInterceptor.addMessage("RM_DENY: check that a security policy has been set for this method");

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
                    switch (checkCapability(invocation, params, cad))
                    {
                        case AccessDecisionVoter.ACCESS_DENIED:
                        {
                            return AccessDecisionVoter.ACCESS_DENIED;
                        }
                        case AccessDecisionVoter.ACCESS_ABSTAIN:
                        {
                            if (logger.isDebugEnabled())
                            {
                                if (logger.isTraceEnabled())
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
                        default:
                        {
                            //do nothing
                        }
                    }
	            }
	            else if (cad.getTypeString().equals(ConfigAttributeDefinition.RM))
                {
                    switch (checkPolicy(invocation, params, cad))
                    {
                        case AccessDecisionVoter.ACCESS_DENIED:
                        {
                            // log message
                            RMMethodSecurityInterceptor.addMessage("Policy " + cad.getPolicyName() + " denied.");

                            return AccessDecisionVoter.ACCESS_DENIED;
                        }
                        case AccessDecisionVoter.ACCESS_ABSTAIN:
                        {
                            if (logger.isDebugEnabled())
                            {
                                if (logger.isTraceEnabled())
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
                        default:
                        {
                            //do nothing
                        }
                    }
                }
	        }
    	}
    	finally
    	{
    		alfrescoTransactionSupport.unbindResource("voting");
    	}

        // all voted to allow
        return AccessDecisionVoter.ACCESS_GRANTED;
    }

    /**
     * Check the capability
     *
     * @param invocation    method invocation
     * @param params        parameters
     * @param cad           config definition
     * @return int          evaluation result
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
            throw new AlfrescoRuntimeException("The capability '" + cad.getRequired().getName() + "' set on method '" + invocation.getMethod().getName() + "' does not exist.");
        }
        return capability.hasPermissionRaw(testNodeRef);

    }

    /**
     * Evaluate policy to determine access
     *
     * @param  invocation   invocation information
     * @param  params       parameters
     * @param  cad          configuration attribute definition
     * @return int          policy evaluation
     */
    @SuppressWarnings("rawtypes")
	private int checkPolicy(MethodInvocation invocation, Class[] params, ConfigAttributeDefinition cad)
    {
        // try to get the policy
        Policy policy = policies.get(cad.getPolicyName());
        if (policy == null)
        {
            // throw an exception if the policy is invalid
            throw new AlfrescoRuntimeException("The policy '" + cad.getPolicyName() + "' set on the method '" + invocation.getMethod().getName() + "' does not exist.");
        }
        else
        {
            // evaluate the policy
            return policy.evaluate(invocation, params, cad);
        }
    }

    /**
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    public void afterPropertiesSet()
    {
        //Do nothing
    }

    /**
     *
     * @param config
     * @return
     */
    @SuppressWarnings("rawtypes")
	private List<ConfigAttributeDefinition> extractSupportedDefinitions(net.sf.acegisecurity.ConfigAttributeDefinition config)
    {
        List<ConfigAttributeDefinition> definitions = new ArrayList<>(2);
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

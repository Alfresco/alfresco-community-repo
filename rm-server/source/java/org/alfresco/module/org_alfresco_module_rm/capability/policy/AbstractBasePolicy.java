/*
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
package org.alfresco.module.org_alfresco_module_rm.capability.policy;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.module.org_alfresco_module_rm.capability.CapabilityService;
import org.alfresco.module.org_alfresco_module_rm.capability.PolicyRegister;
import org.alfresco.module.org_alfresco_module_rm.capability.RMSecurityCommon;
import org.alfresco.repo.security.permissions.impl.acegi.ACLEntryVoterException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Abstract base policy implementation 
 * 
 * @author Roy Wetherall
 * @since 2.1
 */
public abstract class AbstractBasePolicy extends RMSecurityCommon 
                                         implements Policy
{
    /** Logger */
    protected static Log logger = LogFactory.getLog(AbstractBasePolicy.class);
    
    /** Capability service */
    protected CapabilityService capabilityService;
    
    /** Policy register */
    protected PolicyRegister policyRegister;
    
    /** Policy name */
    protected String name;
    
    /**
     * @param name  policy name
     */
    public void setName(String name)
    {
        this.name = name;
    }
    
    /**
     * @see org.alfresco.module.org_alfresco_module_rm.capability.policy.Policy#getName()
     */
    @Override
    public String getName()
    {
        return name;
    }
    
    /**
     * @param capabilityService capability service
     */
    public void setCapabilityService(CapabilityService capabilityService)
    {
        this.capabilityService = capabilityService;
    }
    
    /**
     * @param policyRegister	policy register
     */
    public void setPolicyRegister(PolicyRegister policyRegister) 
    {
		this.policyRegister = policyRegister;
	}
    
    /**
     * Init method
     */
    public void init()
    {
    	policyRegister.registerPolicy(this);
    }
    
    /**
     * 
     * @param invocation
     * @param params
     * @param position
     * @param parent
     * @return
     */
    @SuppressWarnings("rawtypes")
    protected QName getType(MethodInvocation invocation, Class[] params, int position, boolean parent)
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
    
    /**
     * 
     * @param invocation
     * @param params
     * @param position
     * @return
     */
    @SuppressWarnings("rawtypes")
    protected QName getQName(MethodInvocation invocation, Class[] params, int position)
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

    /**
     * 
     * @param invocation
     * @param params
     * @param position
     * @return
     */
    @SuppressWarnings("rawtypes")
    protected Serializable getProperty(MethodInvocation invocation, Class[] params, int position)
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

    /**
     * 
     * @param invocation
     * @param params
     * @param position
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected Map<QName, Serializable> getProperties(MethodInvocation invocation, Class[] params, int position)
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
}

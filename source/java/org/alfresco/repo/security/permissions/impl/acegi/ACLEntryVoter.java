/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.security.permissions.impl.acegi;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
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

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.impl.SimplePermissionReference;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author andyh
 */

public class ACLEntryVoter implements AccessDecisionVoter, InitializingBean
{
    private static Log log = LogFactory.getLog(ACLEntryVoter.class);

    private static final String ACL_NODE = "ACL_NODE";

    private static final String ACL_ITEM = "ACL_ITEM";

    private static final String ACL_PRI_CHILD_ASSOC_ON_CHILD = "ACL_PRI_CHILD_ASSOC_ON_CHILD";

    private static final String ACL_PARENT = "ACL_PARENT";

    private static final String ACL_ALLOW = "ACL_ALLOW";

    private static final String ACL_METHOD = "ACL_METHOD";
    
    private static final String ACL_DENY = "ACL_DENY";

    private PermissionService permissionService;

    private NamespacePrefixResolver nspr;

    private NodeService nodeService;

    private OwnableService ownableService;

    private AuthorityService authorityService;
    
    private Set<QName> abstainForClassQNames = new HashSet<QName>();
    
    private Set<String> abstainFor = null;

    /**
     * Default constructor
     *
     */
    public ACLEntryVoter()
    {
        super();
    }

    // ~ Methods
    // ================================================================

    /**
     * Set the permission service
     * @param permissionService 
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    /**
     * Get the permission service
     * @return the permission service
     */
    public PermissionService getPermissionService()
    {
        return permissionService;
    }

    /**
     * Get the name space prefix resolver
     * @return the name space prefix resolver
     */
    public NamespacePrefixResolver getNamespacePrefixResolver()
    {
        return nspr;
    }

    /**
     * Set the name space prefix resolver
     * @param nspr
     */
    public void setNamespacePrefixResolver(NamespacePrefixResolver nspr)
    {
        this.nspr = nspr;
    }

    /**
     * Get the node service
     * @return the node service
     */
    public NodeService getNodeService()
    {
        return nodeService;
    }

    /**
     * Get the ownable service
     * @return the ownable service
     */
    public OwnableService getOwnableService()
    {
        return ownableService;
    }

    /**
     * Set the node service
     * @param nodeService
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Set the ownable service
     * @param nodeService
     */
    public void setOwnableService(OwnableService ownableService)
    {
        this.ownableService = ownableService;
    }

    /**
     * Set the authentication service
     * @param authenticationService
     */
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        log.warn("Bean property 'authenticationService' no longer required on 'ACLEntryVoter'.");
    }

    /**
     * Set the authority service
     * @param authorityService
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    
    /**
     * Types and aspects for which we will abstain on voting if they are present.
     * @param abstainFor
     */
    public void setAbstainFor(Set<String> abstainFor)
    {
        this.abstainFor = abstainFor;
    }

    public void afterPropertiesSet() throws Exception
    {
        if (permissionService == null)
        {
            throw new IllegalArgumentException("There must be a permission service");
        }
        if (nspr == null)
        {
            throw new IllegalArgumentException("There must be a namespace service");
        }
        if (nodeService == null)
        {
            throw new IllegalArgumentException("There must be a node service");
        }
        if (authorityService == null)
        {
            throw new IllegalArgumentException("There must be an authority service");
        }
        if(abstainFor != null)
        {
            for(String qnameString : abstainFor)
            {
                QName qname = QName.resolveToQName(nspr, qnameString);
                abstainForClassQNames.add(qname);
            }
        }

    }

    public boolean supports(ConfigAttribute attribute)
    {
        if ((attribute.getAttribute() != null)
                && (attribute.getAttribute().startsWith(ACL_NODE)
                        || attribute.getAttribute().startsWith(ACL_ITEM)
                        || attribute.getAttribute().startsWith(ACL_PRI_CHILD_ASSOC_ON_CHILD)
                        || attribute.getAttribute().startsWith(ACL_PARENT)
                        || attribute.getAttribute().equals(ACL_ALLOW)
                        || attribute.getAttribute().startsWith(ACL_METHOD)
                        || attribute.getAttribute().equals(ACL_DENY)))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * This implementation supports only <code>MethodSecurityInterceptor</code>, because it queries the presented <code>MethodInvocation</code>.
     * 
     * @param clazz
     *            the secure object
     * @return <code>true</code> if the secure object is <code>MethodInvocation</code>, <code>false</code> otherwise
     */
    public boolean supports(Class clazz)
    {
        return (MethodInvocation.class.isAssignableFrom(clazz));
    }

    public int vote(Authentication authentication, Object object, ConfigAttributeDefinition config)
    {
        if (log.isDebugEnabled())
        {
            MethodInvocation mi = (MethodInvocation) object;
            log.debug("Method: " + mi.getMethod().toString());
        }
        if (AuthenticationUtil.isRunAsUserTheSystemUser())
        {
            if (log.isDebugEnabled())
            {
                log.debug("Access granted for the system user");
            }
            return AccessDecisionVoter.ACCESS_GRANTED;
        }

        List<ConfigAttributeDefintion> supportedDefinitions = extractSupportedDefinitions(config);

        if (supportedDefinitions.size() == 0)
        {
            return AccessDecisionVoter.ACCESS_ABSTAIN;
        }

        MethodInvocation invocation = (MethodInvocation) object;

        Method method = invocation.getMethod();
        Class<?>[] params = method.getParameterTypes();

        Boolean hasMethodEntry = null;
        
        for (ConfigAttributeDefintion cad : supportedDefinitions)
        {
            NodeRef testNodeRef = null;

            if (cad.typeString.equals(ACL_DENY))
            {
                return AccessDecisionVoter.ACCESS_DENIED;
            }
            else if (cad.typeString.equals(ACL_ALLOW))
            {
                return AccessDecisionVoter.ACCESS_GRANTED;
            }
            else if (cad.typeString.equals(ACL_METHOD))
            {
                if(hasMethodEntry == null)
                {
                    hasMethodEntry = Boolean.FALSE;
                }
                
                if (cad.authority.equals(AuthenticationUtil.getRunAsUser()))
                {
                    hasMethodEntry = Boolean.TRUE;
                }
                else if(authorityService.getAuthorities().contains(cad.authority)) 
                {
                    hasMethodEntry = Boolean.TRUE;
                }
            }
            else if (cad.typeString.equals(ACL_PRI_CHILD_ASSOC_ON_CHILD))
            {
                if (cad.parameter.length == 2 && NodeRef.class.isAssignableFrom(params[cad.parameter[0]])
                        && NodeRef.class.isAssignableFrom(params[cad.parameter[1]]))
                {
                    testNodeRef = getArgument(invocation, cad.parameter[1]);
                    if (testNodeRef != null)
                    {
                        if (nodeService.exists(testNodeRef))
                        {
                            if (log.isDebugEnabled())
                            {
                                log.debug("\tPermission test on node " + nodeService.getPath(testNodeRef));
                            }
                            ChildAssociationRef primaryParent = nodeService.getPrimaryParent(testNodeRef);
                            NodeRef testParentNodeRef = getArgument(invocation, cad.parameter[0]); 
                            if (primaryParent == null || testParentNodeRef == null
                                    || !testParentNodeRef.equals(primaryParent.getParentRef()))
                            {
                                if (log.isDebugEnabled())
                                {
                                    log.debug("\tPermission test ignoring secondary parent association to "
                                            + testParentNodeRef);
                                }
                                testNodeRef = null;
                            }
                        }
                        else if (log.isDebugEnabled())
                        {
                            log.debug("\tPermission test on non-existing node " + testNodeRef);
                        }
                    }
                }
                else if (cad.parameter.length == 1 && ChildAssociationRef.class.isAssignableFrom(params[cad.parameter[0]]))
                {
                    ChildAssociationRef testParentRef = getArgument(invocation, cad.parameter[0]);
                    if (testParentRef != null)
                    {
                        if (testParentRef.isPrimary())
                        {
                            testNodeRef = testParentRef.getChildRef();
                            if (log.isDebugEnabled())
                            {
                                if (nodeService.exists(testNodeRef))
                                {
                                    log.debug("\tPermission test on node " + nodeService.getPath(testNodeRef));
                                }
                                else
                                {
                                    log.debug("\tPermission test on non-existing node " + testNodeRef);
                                }
                            }
                        }
                        else if (log.isDebugEnabled())
                        {
                            log.debug("\tPermission test ignoring secondary parent association to "
                                    + testParentRef.getParentRef());
                        }
                    }
                }
                else
                {
                    throw new ACLEntryVoterException("The specified parameter is not a NodeRef or ChildAssociationRef");
                }
            }
            else if (cad.typeString.equals(ACL_NODE))
            {
                if (cad.parameter.length != 1)
                {
                    throw new ACLEntryVoterException("The specified parameter is not a NodeRef or ChildAssociationRef");
                }
                else if (StoreRef.class.isAssignableFrom(params[cad.parameter[0]]))
                {
                    StoreRef storeRef = getArgument(invocation, cad.parameter[0]);
                    if (storeRef != null)
                    {
                        if (log.isDebugEnabled())
                        {
                            log.debug("\tPermission test against the store - using permissions on the root node");
                        }
                        if (nodeService.exists(storeRef))
                        {
                            testNodeRef = nodeService.getRootNode(storeRef);
                        }
                    }
                }
                else if (NodeRef.class.isAssignableFrom(params[cad.parameter[0]]))
                {
                    testNodeRef = getArgument(invocation, cad.parameter[0]);
                    if (log.isDebugEnabled())
                    {
                        if (nodeService.exists(testNodeRef))
                        {
                            log.debug("\tPermission test on node " + nodeService.getPath(testNodeRef));
                        }
                        else
                        {
                            log.debug("\tPermission test on non-existing node " +testNodeRef);
                        }
                    }
                }
                else if (ChildAssociationRef.class.isAssignableFrom(params[cad.parameter[0]]))
                {
                    ChildAssociationRef testChildRef = getArgument(invocation, cad.parameter[0]);
                    if (testChildRef != null)
                    {
                        testNodeRef = testChildRef.getChildRef();
                        if (log.isDebugEnabled())
                        {
                            if (nodeService.exists(testNodeRef))
                            {
                                log.debug("\tPermission test on node " + nodeService.getPath(testNodeRef));
                            }
                            else
                            {
                                log.debug("\tPermission test on non-existing node " + testNodeRef);
                            }
                        }
                    }
                }
                else
                {
                    throw new ACLEntryVoterException("The specified parameter is not a NodeRef or ChildAssociationRef");
                }
            }
            else if (cad.typeString.equals(ACL_ITEM))
            {
                if (NodeRef.class.isAssignableFrom(params[cad.parameter[0]]))
                {
                    if (Map.class.isAssignableFrom(params[1]) || Map.class.isAssignableFrom(params[2]))
                    {
                        Map<QName, Serializable> properties = (Map<QName, Serializable>) (Map.class.isAssignableFrom(params[1]) ? getArgument(invocation, 1) : getArgument(invocation, 2));
                        if (properties != null && properties.containsKey(ContentModel.PROP_OWNER))
                        {
                            testNodeRef = getArgument(invocation, cad.parameter[0]);

                            boolean isChanged = !properties.get(ContentModel.PROP_OWNER).toString().equals(ownableService.getOwner(testNodeRef));

                            if (!isChanged)
                            {
                                testNodeRef = null;
                            }

                            if (log.isDebugEnabled())
                            {
                                if (nodeService.exists(testNodeRef))
                                {
                                    log.debug("\tPermission test on node " + nodeService.getPath(testNodeRef));
                                }
                                else
                                {
                                    log.debug("\tPermission test on non-existing node " + testNodeRef);
                                }
                            }
                        }
                    }
                    else if (QName.class.isAssignableFrom(params[1]) && params[2] != null)
                    {
                        testNodeRef = getArgument(invocation, cad.parameter[0]);
                        QName arg1 = getArgument(invocation, 1);
                        boolean isOwnerProperty = ContentModel.PROP_OWNER.equals(arg1);
                        if(isOwnerProperty)
                        {
                            Object arg2 = getArgument(invocation, 2);
                            boolean isChanged = (arg2 != null && !arg2.toString().equals(ownableService.getOwner(testNodeRef)));

                            if (!isChanged)
                            {
                                testNodeRef = null;
                            }  
                        }
                        else
                        {
                            testNodeRef = null;
                        }

                        if (log.isDebugEnabled())
                        {
                            if (nodeService.exists(testNodeRef))
                            {
                                log.debug("\tPermission test on node " + nodeService.getPath(testNodeRef));
                            }
                            else
                            {
                                log.debug("\tPermission test on non-existing node " + testNodeRef);
                            }
                        }
                    }
                }
                else
                {
                    throw new ACLEntryVoterException("The specified parameter is not a Item");
                }
            }
            else if (cad.typeString.equals(ACL_PARENT))
            {
                // There is no point having parent permissions for store
                // refs
                if (cad.parameter.length != 1)
                {
                    throw new ACLEntryVoterException("The specified parameter is not a NodeRef or ChildAssociationRef");
                }
                else if (NodeRef.class.isAssignableFrom(params[cad.parameter[0]]))
                {
                    NodeRef child = getArgument(invocation, cad.parameter[0]);
                    if (child != null)
                    {
                        testNodeRef = nodeService.getPrimaryParent(child).getParentRef();
                        if (log.isDebugEnabled())
                        {
                            if (nodeService.exists(testNodeRef))
                            {
                                log.debug("\tPermission test for parent on node " + nodeService.getPath(testNodeRef));
                            }
                            else
                            {
                                log.debug("\tPermission test for parent on non-existing node " + testNodeRef);
                            }
                            log.debug("\tPermission test for parent on node " + nodeService.getPath(testNodeRef));
                        }
                    }
                }
                else if (ChildAssociationRef.class.isAssignableFrom(params[cad.parameter[0]]))
                {
                    ChildAssociationRef testParentRef = getArgument(invocation, cad.parameter[0]);
                    if (testParentRef != null)
                    {
                        testNodeRef = testParentRef.getParentRef();
                        if (log.isDebugEnabled())
                        {
                            if (nodeService.exists(testNodeRef))
                            {
                                log.debug("\tPermission test for parent on child assoc ref for node "
                                        + nodeService.getPath(testNodeRef));
                            }
                            else
                            {
                                log.debug("\tPermission test for parent on child assoc ref for non existing node "
                                        + testNodeRef);
                            }
                           
                        }
                    }

                }
                else
                {
                    throw new ACLEntryVoterException("The specified parameter is not a ChildAssociationRef");
                }
            }

            if (testNodeRef != null)
            {
                // now we know the node - we can abstain for certain types and aspects (eg. RM)
                if(abstainForClassQNames.size() > 0)
                {
                    // check node exists
                    if (nodeService.exists(testNodeRef))
                    {
                        QName typeQName = nodeService.getType(testNodeRef);
                        if(abstainForClassQNames.contains(typeQName))
                        {
                            return AccessDecisionVoter.ACCESS_ABSTAIN;
                        }
                       
                        Set<QName> aspectQNames = nodeService.getAspects(testNodeRef);
                        for(QName abstain : abstainForClassQNames)
                        {
                            if(aspectQNames.contains(abstain))
                            {
                                return AccessDecisionVoter.ACCESS_ABSTAIN;
                            }
                        }
                    }
                }
                
                if (log.isDebugEnabled())
                {
                    log.debug("\t\tNode ref is not null");
                }
                if (permissionService.hasPermission(testNodeRef, cad.required.toString()) == AccessStatus.DENIED)
                {
                    if (log.isDebugEnabled())
                    {
                        log.debug("\t\tPermission is denied");
                        Thread.dumpStack();
                    }
                    return AccessDecisionVoter.ACCESS_DENIED;
                }
            }
        }

        if((hasMethodEntry == null) || (hasMethodEntry.booleanValue()))
        {
             return AccessDecisionVoter.ACCESS_GRANTED;
        }
        else
        {
            return AccessDecisionVoter.ACCESS_DENIED;
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getArgument(MethodInvocation invocation, int index)
    {
        Object[] args = invocation.getArguments();
        return index > args.length ? null : (T)args[index];        
    }

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

    private class ConfigAttributeDefintion
    {
        String typeString;

        SimplePermissionReference required;

        int[] parameter;

        String authority;

        ConfigAttributeDefintion(ConfigAttribute attr)
        {
            StringTokenizer st = new StringTokenizer(attr.getAttribute(), ".", false);
            if (st.countTokens() < 1)
            {
                throw new ACLEntryVoterException("There must be at least one token in a config attribute");
            }
            typeString = st.nextToken();

            if (!(typeString.equals(ACL_NODE) || typeString.equals(ACL_ITEM) || typeString.equals(ACL_PRI_CHILD_ASSOC_ON_CHILD)
                    || typeString.equals(ACL_PARENT) || typeString.equals(ACL_ALLOW) || typeString.equals(ACL_METHOD) || typeString
                    .equals(ACL_DENY)))
            {
                throw new ACLEntryVoterException("Invalid type: must be ACL_NODE, ACL_ITEM, ACL_PARENT or ACL_ALLOW");
            }

            if (typeString.equals(ACL_NODE) || typeString.equals(ACL_ITEM) || typeString.equals(ACL_PRI_CHILD_ASSOC_ON_CHILD)
                    || typeString.equals(ACL_PARENT))
            {
                int count = st.countTokens();
                if (typeString.equals(ACL_PRI_CHILD_ASSOC_ON_CHILD))
                {
                    if (count != 3 && count != 4)
                    {
                        throw new ACLEntryVoterException("There must be three or four . separated tokens in each config attribute");                        
                    }
                }
                else if (count != 3)
                {
                    throw new ACLEntryVoterException("There must be three . separated tokens in each config attribute");
                }
                // Handle a variable number of parameters
                parameter = new int[count - 2];
                for (int i=0; i<parameter.length; i++)
                {
                    parameter[i] = Integer.parseInt(st.nextToken());                    
                }
                String qNameString = st.nextToken();
                String permissionString = st.nextToken();
                QName qName = QName.createQName(qNameString, nspr);

                required = SimplePermissionReference.getPermissionReference(qName, permissionString);
            }
            else if (typeString.equals(ACL_METHOD))
            {
                if (st.countTokens() != 1)
                {
                    throw new ACLEntryVoterException(
                            "There must be two . separated tokens in each group or role config attribute");
                }
                authority = st.nextToken();
            }

        }
    }
}

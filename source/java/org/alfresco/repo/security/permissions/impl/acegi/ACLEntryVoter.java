/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.security.permissions.impl.acegi;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.ConfigAttribute;
import net.sf.acegisecurity.ConfigAttributeDefinition;
import net.sf.acegisecurity.vote.AccessDecisionVoter;

import org.alfresco.repo.security.permissions.impl.SimplePermissionReference;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.AuthorityService;
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

    private static final String ACL_PARENT = "ACL_PARENT";

    private static final String ACL_ALLOW = "ACL_ALLOW";

    private static final String ACL_METHOD = "ACL_METHOD";

    private PermissionService permissionService;

    private NamespacePrefixResolver nspr;

    private NodeService nodeService;

    private AuthenticationService authenticationService;

    private AuthorityService authorityService;

    public ACLEntryVoter()
    {
        super();
    }

    // ~ Methods
    // ================================================================

    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    public PermissionService getPermissionService()
    {
        return permissionService;
    }

    public NamespacePrefixResolver getNamespacePrefixResolver()
    {
        return nspr;
    }

    public void setNamespacePrefixResolver(NamespacePrefixResolver nspr)
    {
        this.nspr = nspr;
    }

    public NodeService getNodeService()
    {
        return nodeService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public AuthenticationService getAuthenticationService()
    {
        return authenticationService;
    }

    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        this.authenticationService = authenticationService;
    }

    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
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
        if (authenticationService == null)
        {
            throw new IllegalArgumentException("There must be an authentication service");
        }
        if (authorityService == null)
        {
            throw new IllegalArgumentException("There must be an authority service");
        }

    }

    public boolean supports(ConfigAttribute attribute)
    {
        if ((attribute.getAttribute() != null)
                && (attribute.getAttribute().startsWith(ACL_NODE)
                        || attribute.getAttribute().startsWith(ACL_PARENT)
                        || attribute.getAttribute().startsWith(ACL_ALLOW) || attribute.getAttribute().startsWith(
                        ACL_METHOD)))
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
        if (authenticationService.isCurrentUserTheSystemUser())
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
            return AccessDecisionVoter.ACCESS_GRANTED;
        }

        MethodInvocation invocation = (MethodInvocation) object;

        Method method = invocation.getMethod();
        Class[] params = method.getParameterTypes();

        for (ConfigAttributeDefintion cad : supportedDefinitions)
        {
            NodeRef testNodeRef = null;

            if (cad.typeString.equals(ACL_ALLOW))
            {
                return AccessDecisionVoter.ACCESS_GRANTED;
            }
            else if (cad.typeString.equals(ACL_METHOD))
            {
                if (authenticationService.getCurrentUserName().equals(cad.authority))
                {
                    return AccessDecisionVoter.ACCESS_GRANTED;
                }
                else
                {
                    return authorityService.getAuthorities().contains(cad.authority) ? AccessDecisionVoter.ACCESS_GRANTED
                            : AccessDecisionVoter.ACCESS_DENIED;
                }
            }
            else if (cad.parameter >= invocation.getArguments().length)
            {
                continue;
            }
            else if (cad.typeString.equals(ACL_NODE))
            {
                if (StoreRef.class.isAssignableFrom(params[cad.parameter]))
                {
                    if (invocation.getArguments()[cad.parameter] != null)
                    {
                        if (log.isDebugEnabled())
                        {
                            log.debug("\tPermission test against the store - using permissions on the root node");
                        }
                        StoreRef storeRef = (StoreRef) invocation.getArguments()[cad.parameter];
                        if (nodeService.exists(storeRef))
                        {
                            testNodeRef = nodeService.getRootNode(storeRef);
                        }
                    }
                }
                else if (NodeRef.class.isAssignableFrom(params[cad.parameter]))
                {
                    testNodeRef = (NodeRef) invocation.getArguments()[cad.parameter];
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
                else if (ChildAssociationRef.class.isAssignableFrom(params[cad.parameter]))
                {
                    if (invocation.getArguments()[cad.parameter] != null)
                    {
                        testNodeRef = ((ChildAssociationRef) invocation.getArguments()[cad.parameter]).getChildRef();
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
            else if (cad.typeString.equals(ACL_PARENT))
            {
                // There is no point having parent permissions for store
                // refs
                if (NodeRef.class.isAssignableFrom(params[cad.parameter]))
                {
                    NodeRef child = (NodeRef) invocation.getArguments()[cad.parameter];
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
                else if (ChildAssociationRef.class.isAssignableFrom(params[cad.parameter]))
                {
                    if (invocation.getArguments()[cad.parameter] != null)
                    {
                        testNodeRef = ((ChildAssociationRef) invocation.getArguments()[cad.parameter]).getParentRef();
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

        return AccessDecisionVoter.ACCESS_GRANTED;
    }

    private List<ConfigAttributeDefintion> extractSupportedDefinitions(ConfigAttributeDefinition config)
    {
        List<ConfigAttributeDefintion> definitions = new ArrayList<ConfigAttributeDefintion>();
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

        int parameter;

        String authority;

        ConfigAttributeDefintion(ConfigAttribute attr)
        {
            StringTokenizer st = new StringTokenizer(attr.getAttribute(), ".", false);
            if (st.countTokens() < 1)
            {
                throw new ACLEntryVoterException("There must be at least one token in a config attribute");
            }
            typeString = st.nextToken();

            if (!(typeString.equals(ACL_NODE) || typeString.equals(ACL_PARENT) || typeString.equals(ACL_ALLOW) || typeString
                    .equals(ACL_METHOD)))
            {
                throw new ACLEntryVoterException("Invalid type: must be ACL_NODE, ACL_PARENT or ACL_ALLOW");
            }

            if (typeString.equals(ACL_NODE) || typeString.equals(ACL_PARENT))
            {
                if (st.countTokens() != 3)
                {
                    throw new ACLEntryVoterException("There must be four . separated tokens in each config attribute");
                }
                String numberString = st.nextToken();
                String qNameString = st.nextToken();
                String permissionString = st.nextToken();

                parameter = Integer.parseInt(numberString);

                QName qName = QName.createQName(qNameString, nspr);

                required = new SimplePermissionReference(qName, permissionString);
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

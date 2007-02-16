/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.security.permissions.impl.acegi;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import net.sf.acegisecurity.AccessDeniedException;
import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.ConfigAttribute;
import net.sf.acegisecurity.ConfigAttributeDefinition;
import net.sf.acegisecurity.afterinvocation.AfterInvocationProvider;

import org.alfresco.repo.search.SimpleResultSetMetaData;
import org.alfresco.repo.security.permissions.impl.SimplePermissionReference;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthenticationService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

public class ACLEntryAfterInvocationProvider implements AfterInvocationProvider, InitializingBean
{
    private static Log log = LogFactory.getLog(ACLEntryAfterInvocationProvider.class);

    private static final String AFTER_ACL_NODE = "AFTER_ACL_NODE";

    private static final String AFTER_ACL_PARENT = "AFTER_ACL_PARENT";

    private PermissionService permissionService;
    private NamespacePrefixResolver nspr;
    private NodeService nodeService;
    private AuthenticationService authenticationService;
    private int maxPermissionChecks;
    private long maxPermissionCheckTimeMillis;

    public ACLEntryAfterInvocationProvider()
    {
        super();
        maxPermissionChecks = Integer.MAX_VALUE;
        maxPermissionCheckTimeMillis = Long.MAX_VALUE;
    }

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
    
    public void setMaxPermissionChecks(int maxPermissionChecks)
    {
        this.maxPermissionChecks = maxPermissionChecks;
    }
    
    public void setMaxPermissionCheckTimeMillis(long maxPermissionCheckTimeMillis)
    {
        this.maxPermissionCheckTimeMillis = maxPermissionCheckTimeMillis;
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

    }

    public Object decide(Authentication authentication, Object object, ConfigAttributeDefinition config,
            Object returnedObject) throws AccessDeniedException
    {
        if (log.isDebugEnabled())
        {
            MethodInvocation mi = (MethodInvocation) object;
            log.debug("Method: " + mi.getMethod().toString());
        }
        try
        {
            if (authenticationService.isCurrentUserTheSystemUser())
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Allowing system user access");
                }
                return returnedObject;
            }
            else if (returnedObject == null)
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Allowing null object access");
                }
                return null;
            }
            else if (StoreRef.class.isAssignableFrom(returnedObject.getClass()))
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Store access");
                }
                return decide(authentication, object, config, nodeService.getRootNode((StoreRef) returnedObject))
                        .getStoreRef();
            }
            else if (NodeRef.class.isAssignableFrom(returnedObject.getClass()))
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Node access");
                }
                return decide(authentication, object, config, (NodeRef) returnedObject);
            }
            else if (FileInfo.class.isAssignableFrom(returnedObject.getClass()))
            {
                return decide(authentication, object, config, (FileInfo) returnedObject);
            }
            else if (ChildAssociationRef.class.isAssignableFrom(returnedObject.getClass()))
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Child Association access");
                }
                return decide(authentication, object, config, (ChildAssociationRef) returnedObject);
            }
            else if (ResultSet.class.isAssignableFrom(returnedObject.getClass()))
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Result Set access");
                }
                return decide(authentication, object, config, (ResultSet) returnedObject);
            }
            else if (Collection.class.isAssignableFrom(returnedObject.getClass()))
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Collection Access");
                }
                return decide(authentication, object, config, (Collection) returnedObject);
            }
            else if (returnedObject.getClass().isArray())
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Array Access");
                }
                return decide(authentication, object, config, (Object[]) returnedObject);
            }
            else
            {
                if (log.isDebugEnabled())
                {
                    log.debug("Uncontrolled object - access allowed for " + object.getClass().getName());
                }
                return returnedObject;
            }
        }
        catch (AccessDeniedException ade)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Access denied");
                ade.printStackTrace();
            }
            throw ade;
        }
        catch (RuntimeException re)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Access denied by runtime exception");
                re.printStackTrace();
            }
            throw re;
        }

    }

    public NodeRef decide(
            Authentication authentication,
            Object object,
            ConfigAttributeDefinition config,
            NodeRef returnedObject) throws AccessDeniedException

    {
        if (returnedObject == null)
        {
            return null;
        }

        List<ConfigAttributeDefintion> supportedDefinitions = extractSupportedDefinitions(config);

        if (supportedDefinitions.size() == 0)
        {
            return returnedObject;
        }

        for (ConfigAttributeDefintion cad : supportedDefinitions)
        {
            NodeRef testNodeRef = null;

            if (cad.typeString.equals(AFTER_ACL_NODE))
            {
                testNodeRef = returnedObject;
            }
            else if (cad.typeString.equals(AFTER_ACL_PARENT))
            {
                testNodeRef = nodeService.getPrimaryParent(returnedObject).getParentRef();
            }

            if ((testNodeRef != null)
                    && (permissionService.hasPermission(testNodeRef, cad.required.toString()) == AccessStatus.DENIED))
            {
                throw new AccessDeniedException("Access Denied");
            }

        }

        return returnedObject;
    }

    public FileInfo decide(
            Authentication authentication,
            Object object,
            ConfigAttributeDefinition config,
            FileInfo returnedObject) throws AccessDeniedException

    {
        NodeRef nodeRef = returnedObject.getNodeRef();
        // this is virtually equivalent to the noderef
        decide(authentication, object, config, nodeRef);
        // the noderef was allowed
        return returnedObject;
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

    public ChildAssociationRef decide(Authentication authentication, Object object, ConfigAttributeDefinition config,
            ChildAssociationRef returnedObject) throws AccessDeniedException

    {
        if (returnedObject == null)
        {
            return null;
        }

        List<ConfigAttributeDefintion> supportedDefinitions = extractSupportedDefinitions(config);

        if (supportedDefinitions.size() == 0)
        {
            return returnedObject;
        }

        for (ConfigAttributeDefintion cad : supportedDefinitions)
        {
            NodeRef testNodeRef = null;

            if (cad.typeString.equals(AFTER_ACL_NODE))
            {
                testNodeRef = ((ChildAssociationRef) returnedObject).getChildRef();
            }
            else if (cad.typeString.equals(AFTER_ACL_PARENT))
            {
                testNodeRef = ((ChildAssociationRef) returnedObject).getParentRef();
            }

            if ((testNodeRef != null)
                    && (permissionService.hasPermission(testNodeRef, cad.required.toString()) == AccessStatus.DENIED))
            {
                throw new AccessDeniedException("Access Denied");
            }

        }

        return returnedObject;
    }

    public ResultSet decide(Authentication authentication, Object object, ConfigAttributeDefinition config,
            ResultSet returnedObject) throws AccessDeniedException

    {
        if (returnedObject == null)
        {
            return null;
        }
        
        FilteringResultSet filteringResultSet = new FilteringResultSet(returnedObject);

       
        List<ConfigAttributeDefintion> supportedDefinitions = extractSupportedDefinitions(config);

        Integer maxSize = null;
        if(returnedObject.getResultSetMetaData().getSearchParameters().getLimitBy() == LimitBy.FINAL_SIZE)
        {
            maxSize = new Integer(returnedObject.getResultSetMetaData().getSearchParameters().getLimit());
        }
        
        if (supportedDefinitions.size() == 0)
        {
            if(maxSize == null)
            {
               return returnedObject;
            }
            else if (returnedObject.length() > maxSize.intValue())
            {
                for(int i = 0; i < maxSize.intValue(); i++)
                {
                    filteringResultSet.setIncluded(i, true);
                }
                filteringResultSet.setResultSetMetaData(new SimpleResultSetMetaData(LimitBy.FINAL_SIZE, PermissionEvaluationMode.EAGER, returnedObject.getResultSetMetaData().getSearchParameters()));
            }
            else
            {
                for(int i = 0; i < maxSize.intValue(); i++)
                {
                    filteringResultSet.setIncluded(i, true);
                }
                filteringResultSet.setResultSetMetaData(new SimpleResultSetMetaData(LimitBy.UNLIMITED, PermissionEvaluationMode.EAGER, returnedObject.getResultSetMetaData().getSearchParameters()));
            }
        }

        // record the start time
        long startTimeMillis = System.currentTimeMillis();
        // set the default, unlimited resultset type
        filteringResultSet.setResultSetMetaData(new SimpleResultSetMetaData(LimitBy.UNLIMITED, PermissionEvaluationMode.EAGER, returnedObject.getResultSetMetaData().getSearchParameters()));
        
        for (int i = 0; i < returnedObject.length(); i++)
        {
            long currentTimeMillis = System.currentTimeMillis();
            if ( i >= maxPermissionChecks || (currentTimeMillis - startTimeMillis) > maxPermissionCheckTimeMillis)
            {
                filteringResultSet.setResultSetMetaData(
                        new SimpleResultSetMetaData(
                                LimitBy.NUMBER_OF_PERMISSION_EVALUATIONS,
                                PermissionEvaluationMode.EAGER,
                                returnedObject.getResultSetMetaData().getSearchParameters()));
                break;
            }
            
            // All permission checks must pass
            filteringResultSet.setIncluded(i, true);
            
            for (ConfigAttributeDefintion cad : supportedDefinitions)
            {
                NodeRef testNodeRef = null;
                if (cad.typeString.equals(AFTER_ACL_NODE))
                {
                    testNodeRef = returnedObject.getNodeRef(i);
                }
                else if (cad.typeString.equals(AFTER_ACL_PARENT))
                {
                    testNodeRef = returnedObject.getChildAssocRef(i).getParentRef();
                }

                if (filteringResultSet.getIncluded(i) 
                        && (testNodeRef != null)
                        && (permissionService.hasPermission(testNodeRef, cad.required.toString()) == AccessStatus.DENIED))
                {
                    filteringResultSet.setIncluded(i, false);
                }
            }
            
            // Bug out if we are limiting by size
            if ((maxSize != null) && (filteringResultSet.length() > maxSize.intValue()))
            {
                // Renove the last match to fix the correct size
                filteringResultSet.setIncluded(i, false);
                filteringResultSet.setResultSetMetaData(new SimpleResultSetMetaData(LimitBy.FINAL_SIZE, PermissionEvaluationMode.EAGER, returnedObject.getResultSetMetaData().getSearchParameters()));
                break;
            }
        }
        return filteringResultSet;
    }

    public Collection decide(Authentication authentication, Object object, ConfigAttributeDefinition config,
            Collection returnedObject) throws AccessDeniedException

    {
        if (returnedObject == null)
        {
            return null;
        }

        List<ConfigAttributeDefintion> supportedDefinitions = extractSupportedDefinitions(config);

        if (supportedDefinitions.size() == 0)
        {
            return returnedObject;
        }

        Set<Object> removed = new HashSet<Object>();

        if (log.isDebugEnabled())
        {
            log.debug("Entries are " + supportedDefinitions);
        }

        // record search start time
        long startTimeMillis = System.currentTimeMillis();
        int count = 0;
        
        Iterator iterator = returnedObject.iterator();
        while (iterator.hasNext())
        {
            Object nextObject = iterator.next();
            
            // if the maximum result size or time has been exceeded, then we have to remove only
            long currentTimeMillis = System.currentTimeMillis();
            if ( count >= maxPermissionChecks || (currentTimeMillis - startTimeMillis) > maxPermissionCheckTimeMillis)
            {
                // just remove it
                iterator.remove();
                continue;
            }
            
            boolean allowed = true;
            for (ConfigAttributeDefintion cad : supportedDefinitions)
            {
                NodeRef testNodeRef = null;

                if (cad.typeString.equals(AFTER_ACL_NODE))
                {
                    if (StoreRef.class.isAssignableFrom(nextObject.getClass()))
                    {
                        testNodeRef = nodeService.getRootNode((StoreRef) nextObject);
                    }
                    else if (NodeRef.class.isAssignableFrom(nextObject.getClass()))
                    {
                        testNodeRef = (NodeRef) nextObject;
                    }
                    else if (ChildAssociationRef.class.isAssignableFrom(nextObject.getClass()))
                    {
                        testNodeRef = ((ChildAssociationRef) nextObject).getChildRef();
                    }
                    else if (FileInfo.class.isAssignableFrom(nextObject.getClass()))
                    {
                        testNodeRef = ((FileInfo) nextObject).getNodeRef();
                    }
                    else
                    {
                        throw new ACLEntryVoterException(
                                "The specified parameter is not a collection of NodeRefs, ChildAssociationRefs or FileInfos");
                    }
                }
                else if (cad.typeString.equals(AFTER_ACL_PARENT))
                {
                    if (StoreRef.class.isAssignableFrom(nextObject.getClass()))
                    {
                        // Will be allowed
                        testNodeRef = null;
                    }
                    else if (NodeRef.class.isAssignableFrom(nextObject.getClass()))
                    {
                        testNodeRef = nodeService.getPrimaryParent((NodeRef) nextObject).getParentRef();
                    }
                    else if (ChildAssociationRef.class.isAssignableFrom(nextObject.getClass()))
                    {
                        testNodeRef = ((ChildAssociationRef) nextObject).getParentRef();
                    }
                    else if (FileInfo.class.isAssignableFrom(nextObject.getClass()))
                    {
                        testNodeRef = ((FileInfo) nextObject).getNodeRef();
                    }
                    else
                    {
                        throw new ACLEntryVoterException(
                                "The specified parameter is not a collection of NodeRefs or ChildAssociationRefs");
                    }
                }
                
                if (log.isDebugEnabled())
                {
                    log.debug("\t" + cad.typeString + " test on " + testNodeRef + " from " + nextObject.getClass().getName());
                }
                
                if (allowed
                        && (testNodeRef != null)
                        && (permissionService.hasPermission(testNodeRef, cad.required.toString()) == AccessStatus.DENIED))
                {
                    allowed = false;
                }
            }
            if (!allowed)
            {
                removed.add(nextObject);
            }
        }
        for (Object toRemove : removed)
        {
            while (returnedObject.remove(toRemove))
                ;
        }
        return returnedObject;
    }

    public Object[] decide(Authentication authentication, Object object, ConfigAttributeDefinition config,
            Object[] returnedObject) throws AccessDeniedException

    {
        BitSet incudedSet = new BitSet(returnedObject.length);

        if (returnedObject == null)
        {
            return null;
        }

        List<ConfigAttributeDefintion> supportedDefinitions = extractSupportedDefinitions(config);

        if (supportedDefinitions.size() == 0)
        {
            return returnedObject;
        }

        for (int i = 0, l = returnedObject.length; i < l; i++)
        {
            Object current = returnedObject[i];
            for (ConfigAttributeDefintion cad : supportedDefinitions)
            {
                incudedSet.set(i, true);
                NodeRef testNodeRef = null;
                if (cad.typeString.equals(AFTER_ACL_NODE))
                {
                    if (StoreRef.class.isAssignableFrom(current.getClass()))
                    {
                        testNodeRef = nodeService.getRootNode((StoreRef) current);
                    }
                    else if (NodeRef.class.isAssignableFrom(current.getClass()))
                    {
                        testNodeRef = (NodeRef) current;
                    }
                    else if (ChildAssociationRef.class.isAssignableFrom(current.getClass()))
                    {
                        testNodeRef = ((ChildAssociationRef) current).getChildRef();
                    }
                    else if (FileInfo.class.isAssignableFrom(current.getClass()))
                    {
                        testNodeRef = ((FileInfo) current).getNodeRef();
                    }
                    else
                    {
                        throw new ACLEntryVoterException("The specified array is not of NodeRef or ChildAssociationRef");
                    }
                }

                else if (cad.typeString.equals(AFTER_ACL_PARENT))
                {
                    if (StoreRef.class.isAssignableFrom(current.getClass()))
                    {
                        testNodeRef = null;
                    }
                    else if (NodeRef.class.isAssignableFrom(current.getClass()))
                    {
                        testNodeRef = nodeService.getPrimaryParent((NodeRef) current).getParentRef();
                    }
                    else if (ChildAssociationRef.class.isAssignableFrom(current.getClass()))
                    {
                        testNodeRef = ((ChildAssociationRef) current).getParentRef();
                    }
                    else if (FileInfo.class.isAssignableFrom(current.getClass()))
                    {
                        testNodeRef = ((FileInfo) current).getNodeRef();
                    }
                    else
                    {
                        throw new ACLEntryVoterException("The specified array is not of NodeRef or ChildAssociationRef");
                    }
                }

                if (log.isDebugEnabled())
                {
                    log.debug("\t" + cad.typeString + " test on " + testNodeRef + " from " + current.getClass().getName());
                }

                if (incudedSet.get(i)
                        && (testNodeRef != null)
                        && (permissionService.hasPermission(testNodeRef, cad.required.toString()) == AccessStatus.DENIED))
                {
                    incudedSet.set(i, false);
                }

            }
        }

        if (incudedSet.cardinality() == returnedObject.length)
        {
            return returnedObject;
        }
        else
        {
            Object[] answer = new Object[incudedSet.cardinality()];
            for (int i = incudedSet.nextSetBit(0), p = 0; i >= 0; i = incudedSet.nextSetBit(++i), p++)
            {
                answer[p] = returnedObject[i];
            }
            return answer;
        }
    }

    public boolean supports(ConfigAttribute attribute)
    {
        if ((attribute.getAttribute() != null)
                && (attribute.getAttribute().startsWith(AFTER_ACL_NODE) || attribute.getAttribute().startsWith(
                        AFTER_ACL_PARENT)))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean supports(Class clazz)
    {
        return (MethodInvocation.class.isAssignableFrom(clazz));
    }

    private class ConfigAttributeDefintion
    {

        String typeString;

        SimplePermissionReference required;

        ConfigAttributeDefintion(ConfigAttribute attr)
        {

            StringTokenizer st = new StringTokenizer(attr.getAttribute(), ".", false);
            if (st.countTokens() != 3)
            {
                throw new ACLEntryVoterException("There must be three . separated tokens in each config attribute");
            }
            typeString = st.nextToken();
            String qNameString = st.nextToken();
            String permissionString = st.nextToken();

            if (!(typeString.equals(AFTER_ACL_NODE) || typeString.equals(AFTER_ACL_PARENT)))
            {
                throw new ACLEntryVoterException("Invalid type: must be ACL_NODE or ACL_PARENT");
            }

            QName qName = QName.createQName(qNameString, nspr);

            required = new SimplePermissionReference(qName, permissionString);
        }
    }
}

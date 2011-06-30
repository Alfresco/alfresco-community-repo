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
package org.alfresco.repo.security.permissions.impl.acegi;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import net.sf.acegisecurity.AccessDeniedException;
import net.sf.acegisecurity.Authentication;
import net.sf.acegisecurity.ConfigAttribute;
import net.sf.acegisecurity.ConfigAttributeDefinition;
import net.sf.acegisecurity.afterinvocation.AfterInvocationProvider;

import org.alfresco.cmis.CMISResultSet;
import org.alfresco.repo.search.SimpleResultSetMetaData;
import org.alfresco.repo.search.impl.lucene.PagingLuceneResultSet;
import org.alfresco.repo.search.impl.lucene.SolrJSONResultSet;
import org.alfresco.repo.search.impl.querymodel.QueryEngineResults;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.PermissionCheckCollection;
import org.alfresco.repo.security.permissions.PermissionCheckValue;
import org.alfresco.repo.security.permissions.PermissionCheckedCollection.PermissionCheckedCollectionMixin;
import org.alfresco.repo.security.permissions.PermissionCheckedValue;
import org.alfresco.repo.security.permissions.impl.SimplePermissionReference;
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
import org.alfresco.util.Pair;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Enforce permission after the method call
 * 
 * @author andyh
 */
public class ACLEntryAfterInvocationProvider implements AfterInvocationProvider, InitializingBean
{
    private static Log log = LogFactory.getLog(ACLEntryAfterInvocationProvider.class);

    private static final String AFTER_ACL_NODE = "AFTER_ACL_NODE";

    private static final String AFTER_ACL_PARENT = "AFTER_ACL_PARENT";

    private PermissionService permissionService;

    private NamespacePrefixResolver nspr;

    private NodeService nodeService;

    private int maxPermissionChecks;

    private long maxPermissionCheckTimeMillis;
    
    private Set<QName> unfilteredForClassQNames = new HashSet<QName>();
    
    private Set<String> unfilteredFor = null;

	private boolean optimisePermissionsCheck;
	private int optimisePermissionsBulkFetchSize;

    /**
     * Default constructor
     */
    public ACLEntryAfterInvocationProvider()
    {
        super();
        maxPermissionChecks = Integer.MAX_VALUE;
        maxPermissionCheckTimeMillis = Long.MAX_VALUE;
    }

    /**
     * Set the permission service.
     * 
     * @param permissionService
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }

    /**
     * Get the permission service.
     * 
     * @return - the permission service
     */
    public PermissionService getPermissionService()
    {
        return permissionService;
    }

    /**
     * Get the namespace prefix resolver
     * 
     * @return the namespace prefix resolver
     */
    public NamespacePrefixResolver getNamespacePrefixResolver()
    {
        return nspr;
    }

    /**
     * Set the namespace prefix resolver
     * 
     * @param nspr
     */
    public void setNamespacePrefixResolver(NamespacePrefixResolver nspr)
    {
        this.nspr = nspr;
    }

    /**
     * Get the node service
     * 
     * @return the node service
     */
    public NodeService getNodeService()
    {
        return nodeService;
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
     * Set the authentication service
     * 
     * @param authenticationService
     */
    public void setAuthenticationService(AuthenticationService authenticationService)
    {
        log.warn("Bean property 'authenticationService' no longer required.");
    }

    /**
     * Set the max number of permission checks
     * 
     * @param maxPermissionChecks
     */
    public void setMaxPermissionChecks(int maxPermissionChecks)
    {
        this.maxPermissionChecks = maxPermissionChecks;
    }

    /**
     * Set the max time for permission checks
     * 
     * @param maxPermissionCheckTimeMillis
     */
    public void setMaxPermissionCheckTimeMillis(long maxPermissionCheckTimeMillis)
    {
        this.maxPermissionCheckTimeMillis = maxPermissionCheckTimeMillis;
    }

    /**
     * Types and aspects for which we will abstain on voting if they are present.
     * @param abstainFor
     */
    public void setUnfilteredFor(Set<String> unfilteredFor)
    {
        this.unfilteredFor = unfilteredFor;
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
        if(unfilteredFor != null)
        {
            for(String qnameString : unfilteredFor)
            {
                QName qname = QName.resolveToQName(nspr, qnameString);
                unfilteredForClassQNames.add(qname);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public Object decide(Authentication authentication, Object object, ConfigAttributeDefinition config, Object returnedObject) throws AccessDeniedException
    {
        if (log.isDebugEnabled())
        {
            MethodInvocation mi = (MethodInvocation) object;
            log.debug("Method: " + mi.getMethod().toString());
        }
        try
        {
            if (AuthenticationUtil.isRunAsUserTheSystemUser())
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
            else if (PermissionCheckedValue.class.isAssignableFrom(returnedObject.getClass()))
            {
                // The security provider was not already present
                return decide(authentication, object, config, (PermissionCheckedValue) returnedObject);
            }
            else if (PermissionCheckValue.class.isAssignableFrom(returnedObject.getClass()))
            {
                return decide(authentication, object, config, (PermissionCheckValue) returnedObject);
            }
            else if (StoreRef.class.isAssignableFrom(returnedObject.getClass()))
            {
                return decide(authentication, object, config, nodeService.getRootNode((StoreRef) returnedObject)).getStoreRef();
            }
            else if (NodeRef.class.isAssignableFrom(returnedObject.getClass()))
            {
                return decide(authentication, object, config, (NodeRef) returnedObject);
            }
            else if (Pair.class.isAssignableFrom(returnedObject.getClass()))
            {
                return decide(authentication, object, config, (Pair) returnedObject);
            }
            else if (ChildAssociationRef.class.isAssignableFrom(returnedObject.getClass()))
            {
                return decide(authentication, object, config, (ChildAssociationRef) returnedObject);
            }
            else if (SolrJSONResultSet.class.isAssignableFrom(returnedObject.getClass()))
            {
                return returnedObject;
            }
            else if (CMISResultSet.class.isAssignableFrom(returnedObject.getClass()))
            {
                return returnedObject;
            }
            else if (PagingLuceneResultSet.class.isAssignableFrom(returnedObject.getClass()))
            {
                return decide(authentication, object, config, (PagingLuceneResultSet) returnedObject);
            }
            else if (ResultSet.class.isAssignableFrom(returnedObject.getClass()))
            {
                return decide(authentication, object, config, (ResultSet) returnedObject);
            }
            else if (QueryEngineResults.class.isAssignableFrom(returnedObject.getClass()))
            {
                return decide(authentication, object, config, (QueryEngineResults) returnedObject);
            }
            else if (Collection.class.isAssignableFrom(returnedObject.getClass()))
            {
                return decide(authentication, object, config, (Collection) returnedObject);
            }
            else if (returnedObject.getClass().isArray())
            {
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

    private NodeRef decide(Authentication authentication, Object object, ConfigAttributeDefinition config, NodeRef returnedObject) throws AccessDeniedException

    {
        if (returnedObject == null)
        {
            return null;
        }

        if(isUnfiltered(returnedObject))
        {
            return returnedObject;
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

            if ((testNodeRef != null) && (permissionService.hasPermission(testNodeRef, cad.required.toString()) == AccessStatus.DENIED))
            {
                throw new AccessDeniedException("Access Denied");
            }

        }

        return returnedObject;
    }

    private boolean isUnfiltered(NodeRef returnedObject)
    {
        if (returnedObject == null || !nodeService.exists(returnedObject))
        {
            // Standard practice for non-existent NodeRef is to pass it as unfiltered.
            // See PermissionServiceImpl.hasPermission
            // See ALF-5559: Permission interceptors can fail if Lucene returns invalid NodeRefs
            return true;
        }
        if(unfilteredForClassQNames.size() > 0)
        {
            QName typeQName = nodeService.getType(returnedObject);
            if(unfilteredForClassQNames.contains(typeQName))
            {
                return true;
            }
            Set<QName> aspectQNames = nodeService.getAspects(returnedObject);
            for(QName abstain : unfilteredForClassQNames)
            {
                if(aspectQNames.contains(abstain))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private PermissionCheckedValue decide(Authentication authentication, Object object, ConfigAttributeDefinition config, PermissionCheckedValue returnedObject) throws AccessDeniedException
    {
        // This passes as it has already been filtered
        // TODO: Get the filter that was applied and double-check
        return returnedObject;
    }
    
    private PermissionCheckValue decide(Authentication authentication, Object object, ConfigAttributeDefinition config, PermissionCheckValue returnedObject) throws AccessDeniedException
    {
        // Get the wrapped value
        NodeRef nodeRef = returnedObject.getNodeRef();
        decide(authentication, object, config, nodeRef);
        // This passes
        return returnedObject;
    }
    
    @SuppressWarnings("rawtypes")
    private Pair decide(Authentication authentication, Object object, ConfigAttributeDefinition config, Pair returnedObject) throws AccessDeniedException
    {
        NodeRef nodeRef = (NodeRef) returnedObject.getSecond();
        decide(authentication, object, config, nodeRef);
        // the noderef was allowed
        return returnedObject;
    }

    @SuppressWarnings("rawtypes")
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

    private ChildAssociationRef decide(Authentication authentication, Object object, ConfigAttributeDefinition config, ChildAssociationRef returnedObject)
            throws AccessDeniedException

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

            if(isUnfiltered(testNodeRef))
            {
                continue;
            }
            
            if ((testNodeRef != null) && (permissionService.hasPermission(testNodeRef, cad.required.toString()) == AccessStatus.DENIED))
            {
                throw new AccessDeniedException("Access Denied");
            }

        }

        return returnedObject;
    }
    
    private ResultSet decide(Authentication authentication, Object object, ConfigAttributeDefinition config, PagingLuceneResultSet returnedObject) throws AccessDeniedException

    {
        ResultSet raw = returnedObject.getWrapped();
        ResultSet filteredForPermissions = decide(authentication, object, config, raw);
        PagingLuceneResultSet newPaging = new PagingLuceneResultSet(filteredForPermissions, returnedObject.getResultSetMetaData().getSearchParameters(), nodeService);
        return newPaging;
    }

    public void setOptimisePermissionsCheck(boolean optimisePermissionsCheck)
    {
    	this.optimisePermissionsCheck = optimisePermissionsCheck;
    }

    public void setOptimisePermissionsBulkFetchSize(int optimisePermissionsBulkFetchSize)
    {
    	this.optimisePermissionsBulkFetchSize = optimisePermissionsBulkFetchSize;
    }
    
	private ResultSet decide(Authentication authentication, Object object, ConfigAttributeDefinition config, ResultSet returnedObject) throws AccessDeniedException
    {
    	ResultSet rs = optimisePermissionsCheck ? decideNew(authentication, object, config, returnedObject) :
    			decideOld(authentication, object, config, returnedObject);
        return rs;
    }
 
    private ResultSet decideNew(Authentication authentication, Object object, ConfigAttributeDefinition config, ResultSet returnedObject) throws AccessDeniedException

    {
        if (returnedObject == null)
        {
            return null;
        }

        FilteringResultSet filteringResultSet = new FilteringResultSet(returnedObject);

        List<ConfigAttributeDefintion> supportedDefinitions = extractSupportedDefinitions(config);

        Integer maxSize = null;
        if (returnedObject.getResultSetMetaData().getSearchParameters().getMaxItems() >= 0)
        {
            maxSize = new Integer(returnedObject.getResultSetMetaData().getSearchParameters().getMaxItems());
        }
        if ((maxSize == null) && (returnedObject.getResultSetMetaData().getSearchParameters().getLimitBy() == LimitBy.FINAL_SIZE))
        {
            maxSize = new Integer(returnedObject.getResultSetMetaData().getSearchParameters().getLimit());
        }
        // Allow for skip
        if ((maxSize != null) && (returnedObject.getResultSetMetaData().getSearchParameters().getSkipCount() >= 0))
        {
            maxSize = new Integer(maxSize + returnedObject.getResultSetMetaData().getSearchParameters().getSkipCount());
        }

        int maxChecks = maxPermissionChecks;
        if (returnedObject.getResultSetMetaData().getSearchParameters().getMaxPermissionChecks() >= 0)
        {
            maxChecks = returnedObject.getResultSetMetaData().getSearchParameters().getMaxPermissionChecks();
        }

        long maxCheckTime = maxPermissionCheckTimeMillis;
        if (returnedObject.getResultSetMetaData().getSearchParameters().getMaxPermissionCheckTimeMillis() >= 0)
        {
            maxCheckTime = returnedObject.getResultSetMetaData().getSearchParameters().getMaxPermissionCheckTimeMillis();
        }

        if (supportedDefinitions.size() == 0)
        {
            if (maxSize == null)
            {
                return returnedObject;
            }
            else if (returnedObject.length() > maxSize.intValue())
            {
                for (int i = 0; i < maxSize.intValue(); i++)
                {
                    filteringResultSet.setIncluded(i, true);
                }
                filteringResultSet.setResultSetMetaData(new SimpleResultSetMetaData(LimitBy.FINAL_SIZE, PermissionEvaluationMode.EAGER, returnedObject.getResultSetMetaData()
                        .getSearchParameters()));
            }
            else
            {
                for (int i = 0; i < maxSize.intValue(); i++)
                {
                    filteringResultSet.setIncluded(i, true);
                }
                filteringResultSet.setResultSetMetaData(new SimpleResultSetMetaData(LimitBy.UNLIMITED, PermissionEvaluationMode.EAGER, returnedObject.getResultSetMetaData()
                        .getSearchParameters()));
            }
        }

        // record the start time
        long startTimeMillis = System.currentTimeMillis();
        // set the default, unlimited resultset type
        filteringResultSet.setResultSetMetaData(new SimpleResultSetMetaData(LimitBy.UNLIMITED, PermissionEvaluationMode.EAGER, returnedObject.getResultSetMetaData()
                .getSearchParameters()));

        // use the result set to do bulk loading
        boolean oldBulkFetch = returnedObject.setBulkFetch(true);
        int oldFetchSize = returnedObject.setBulkFetchSize(optimisePermissionsBulkFetchSize);

        try
        {
           for (int i = 0; i < returnedObject.length(); i++)
           {
               long currentTimeMillis = System.currentTimeMillis();
               if (i >= maxChecks)
               {
                   log.warn("maxChecks exceeded (" + maxChecks + ")", new Exception("Back Trace"));
                   filteringResultSet.setResultSetMetaData(new SimpleResultSetMetaData(LimitBy.NUMBER_OF_PERMISSION_EVALUATIONS, PermissionEvaluationMode.EAGER, returnedObject
                           .getResultSetMetaData().getSearchParameters()));
                   break;
               }
               else if ((currentTimeMillis - startTimeMillis) > maxCheckTime)
               {
                   log.warn("maxCheckTime exceeded (" + (currentTimeMillis - startTimeMillis) + " milliseconds)", new Exception("Back Trace"));
                   filteringResultSet.setResultSetMetaData(new SimpleResultSetMetaData(LimitBy.NUMBER_OF_PERMISSION_EVALUATIONS, PermissionEvaluationMode.EAGER, returnedObject
                           .getResultSetMetaData().getSearchParameters()));
                   break;
               }
               
               // All permission checks must pass
               filteringResultSet.setIncluded(i, true);
   
               NodeRef nodeRef = returnedObject.getNodeRef(i);
   
               if (filteringResultSet.getIncluded(i) && permissionService.hasReadPermission(nodeRef) == AccessStatus.DENIED)
               {
                   filteringResultSet.setIncluded(i, false);
               }
   
               // Bug out if we are limiting by size
               if ((maxSize != null) && (filteringResultSet.length() > maxSize.intValue()))
               {
                   // Renove the last match to fix the correct size
                   filteringResultSet.setIncluded(i, false);
                   filteringResultSet.setResultSetMetaData(new SimpleResultSetMetaData(LimitBy.FINAL_SIZE, PermissionEvaluationMode.EAGER, returnedObject.getResultSetMetaData()
                           .getSearchParameters()));
                   break;
               }
           }
        }
        finally
        {
           // put them back to how they were
           returnedObject.setBulkFetch(oldBulkFetch);
           returnedObject.setBulkFetchSize(oldFetchSize);
        }
        
        return filteringResultSet;
    }


    private ResultSet decideOld(Authentication authentication, Object object, ConfigAttributeDefinition config, ResultSet returnedObject) throws AccessDeniedException

    {
        if (returnedObject == null)
        {
            return null;
        }

        FilteringResultSet filteringResultSet = new FilteringResultSet(returnedObject);

        List<ConfigAttributeDefintion> supportedDefinitions = extractSupportedDefinitions(config);

        Integer maxSize = null;
        if (returnedObject.getResultSetMetaData().getSearchParameters().getMaxItems() >= 0)
        {
            maxSize = new Integer(returnedObject.getResultSetMetaData().getSearchParameters().getMaxItems());
        }
        if ((maxSize == null) && (returnedObject.getResultSetMetaData().getSearchParameters().getLimitBy() == LimitBy.FINAL_SIZE))
        {
            maxSize = new Integer(returnedObject.getResultSetMetaData().getSearchParameters().getLimit());
        }
        // Allow for skip
        if ((maxSize != null) && (returnedObject.getResultSetMetaData().getSearchParameters().getSkipCount() >= 0))
        {
            maxSize = new Integer(maxSize + returnedObject.getResultSetMetaData().getSearchParameters().getSkipCount());
        }

        int maxChecks = maxPermissionChecks;
        if (returnedObject.getResultSetMetaData().getSearchParameters().getMaxPermissionChecks() >= 0)
        {
            maxChecks = returnedObject.getResultSetMetaData().getSearchParameters().getMaxPermissionChecks();
        }

        long maxCheckTime = maxPermissionCheckTimeMillis;
        if (returnedObject.getResultSetMetaData().getSearchParameters().getMaxPermissionCheckTimeMillis() >= 0)
        {
            maxCheckTime = returnedObject.getResultSetMetaData().getSearchParameters().getMaxPermissionCheckTimeMillis();
        }

        if (supportedDefinitions.size() == 0)
        {
            if (maxSize == null)
            {
                return returnedObject;
            }
            else if (returnedObject.length() > maxSize.intValue())
            {
                for (int i = 0; i < maxSize.intValue(); i++)
                {
                    filteringResultSet.setIncluded(i, true);
                }
                filteringResultSet.setResultSetMetaData(new SimpleResultSetMetaData(LimitBy.FINAL_SIZE, PermissionEvaluationMode.EAGER, returnedObject.getResultSetMetaData()
                        .getSearchParameters()));
                return filteringResultSet;
            }
            else
            {
                for (int i = 0; i < returnedObject.length(); i++)
                {
                    filteringResultSet.setIncluded(i, true);
                }
                filteringResultSet.setResultSetMetaData(new SimpleResultSetMetaData(returnedObject.getResultSetMetaData().getLimitedBy(), PermissionEvaluationMode.EAGER, returnedObject.getResultSetMetaData()
                        .getSearchParameters()));
                return filteringResultSet;
            }
            
        }

        // record the start time
        long startTimeMillis = System.currentTimeMillis();
        // set the default, unlimited resultset type
        filteringResultSet.setResultSetMetaData(new SimpleResultSetMetaData(returnedObject.getResultSetMetaData().getLimitedBy(), PermissionEvaluationMode.EAGER, returnedObject.getResultSetMetaData()
                .getSearchParameters()));

        for (int i = 0; i < returnedObject.length(); i++)
        {
            long currentTimeMillis = System.currentTimeMillis();
            if (i >= maxChecks)
            {
                log.warn("maxChecks exceeded (" + maxChecks + ")", new Exception("Back Trace"));
                filteringResultSet.setResultSetMetaData(new SimpleResultSetMetaData(LimitBy.NUMBER_OF_PERMISSION_EVALUATIONS, PermissionEvaluationMode.EAGER, returnedObject
                        .getResultSetMetaData().getSearchParameters()));
                break;
            }
            else if ((currentTimeMillis - startTimeMillis) > maxCheckTime)
            {
                log.warn("maxCheckTime exceeded (" + (currentTimeMillis - startTimeMillis) + " milliseconds)", new Exception("Back Trace"));
                filteringResultSet.setResultSetMetaData(new SimpleResultSetMetaData(LimitBy.NUMBER_OF_PERMISSION_EVALUATIONS, PermissionEvaluationMode.EAGER, returnedObject
                        .getResultSetMetaData().getSearchParameters()));
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

                if(isUnfiltered(testNodeRef))
                {
                    continue;
                }
                
                if (filteringResultSet.getIncluded(i) && (testNodeRef != null) && (permissionService.hasPermission(testNodeRef, cad.required.toString()) == AccessStatus.DENIED))
                {
                    filteringResultSet.setIncluded(i, false);
                }
            }

            // Bug out if we are limiting by size
            if ((maxSize != null) && (filteringResultSet.length() > maxSize.intValue()))
            {
                // Remove the last match to fix the correct size
                filteringResultSet.setIncluded(i, false);
                filteringResultSet.setResultSetMetaData(new SimpleResultSetMetaData(LimitBy.FINAL_SIZE, PermissionEvaluationMode.EAGER, returnedObject.getResultSetMetaData()
                        .getSearchParameters()));
                break;
            }
        }
        return filteringResultSet;
    }

    private QueryEngineResults decide(Authentication authentication, Object object, ConfigAttributeDefinition config, QueryEngineResults returnedObject)
            throws AccessDeniedException

    {
        Map<Set<String>, ResultSet> map = returnedObject.getResults();
        Map<Set<String>, ResultSet> answer = new HashMap<Set<String>, ResultSet>(map.size(), 1.0f);

        for (Set<String> group : map.keySet())
        {
            ResultSet raw = map.get(group);
            ResultSet permed;
            if (PagingLuceneResultSet.class.isAssignableFrom(raw.getClass()))
            {
                permed = decide(authentication, object, config, (PagingLuceneResultSet)raw);
            }
            else
            {
                permed = decide(authentication, object, config, raw);
            }
            answer.put(group, permed);
        }
        return new QueryEngineResults(answer);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Collection decide(Authentication authentication, Object object, ConfigAttributeDefinition config, Collection returnedObject) throws AccessDeniedException
    {
        if (returnedObject == null)
        {
            return null;
        }
        
        List<ConfigAttributeDefintion> supportedDefinitions = extractSupportedDefinitions(config);
        if (log.isDebugEnabled())
        {
            log.debug("Entries are " + supportedDefinitions);
        }
        
        if (supportedDefinitions.size() == 0)
        {
            return returnedObject;
        }
        
        // Default to the system-wide values and we'll see if they need to be reduced
        long targetResultCount = returnedObject.size(); 
        int maxPermissionChecks = Integer.MAX_VALUE;
        long maxPermissionCheckTimeMillis = this.maxPermissionCheckTimeMillis; 
        if (returnedObject instanceof PermissionCheckCollection<?>)
        {
            PermissionCheckCollection permissionCheckCollection = (PermissionCheckCollection) returnedObject;
            // Get values
            targetResultCount = permissionCheckCollection.getTargetResultCount();
            if (permissionCheckCollection.getCutOffAfterCount() > 0)
            {
                maxPermissionChecks = permissionCheckCollection.getCutOffAfterCount();
            }
            if (permissionCheckCollection.getCutOffAfterTimeMs() > 0)
            {
                maxPermissionCheckTimeMillis = permissionCheckCollection.getCutOffAfterTimeMs();
            }
        }
        
        // Start timer and counter for cut-off
        boolean cutoff = false;
        long startTimeMillis = System.currentTimeMillis();
        int count = 0;
        
        // Keep values explicitly
        List<Object> keepValues = new ArrayList<Object>(returnedObject.size());
        
        for (Object nextObject : returnedObject)
        {
            // if the maximum result size or time has been exceeded, then we have to remove only
            long currentTimeMillis = System.currentTimeMillis();
            
            if (count >= targetResultCount)
            {
                // We have enough results.  We stop without cutoff.
                break;
            }
            else if (count >= maxPermissionChecks)
            {
                // We have been cut off by count
                cutoff = true;
                if (log.isDebugEnabled())
                {
                    log.debug("decide (collection) cut-off: " + count + " checks exceeded " + maxPermissionChecks + " checks");
                }
                break;
            }
            else if ((currentTimeMillis - startTimeMillis) > maxPermissionCheckTimeMillis)
            {
                // We have been cut off by time
                cutoff = true;
                if (log.isDebugEnabled())
                {
                    log.debug("decide (collection) cut-off: " + (currentTimeMillis - startTimeMillis) + "ms exceeded " + maxPermissionCheckTimeMillis + "ms");
                }
                break;
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
                    else if (Pair.class.isAssignableFrom(nextObject.getClass()))
                    {
                        testNodeRef = (NodeRef) ((Pair)nextObject).getSecond();
                    }
                    else if (PermissionCheckValue.class.isAssignableFrom(nextObject.getClass()))
                    {
                        testNodeRef = ((PermissionCheckValue) nextObject).getNodeRef();
                    }
                    else
                    {
                        throw new ACLEntryVoterException("The specified parameter is recognized: " + nextObject.getClass());
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
                    else if (Pair.class.isAssignableFrom(nextObject.getClass()))
                    {
                        testNodeRef = (NodeRef) ((Pair)nextObject).getSecond();
                    }
                    else if (PermissionCheckValue.class.isAssignableFrom(nextObject.getClass()))
                    {
                        NodeRef nodeRef = ((PermissionCheckValue) nextObject).getNodeRef();
                        testNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
                    }
                    else
                    {
                        throw new ACLEntryVoterException("The specified parameter is recognized: " + nextObject.getClass());
                    }
                }
                
                if (log.isDebugEnabled())
                {
                    log.debug("\t" + cad.typeString + " test on " + testNodeRef + " from " + nextObject.getClass().getName());
                }
                
                if (isUnfiltered(testNodeRef))      // Null allows
                {
                    continue;                       // Continue to next ConfigAttributeDefintion
                }
                
                if (allowed && (testNodeRef != null) && (permissionService.hasPermission(testNodeRef, cad.required.toString()) == AccessStatus.DENIED))
                {
                    allowed = false;
                    break;                          // No point evaluating more ConfigAttributeDefintions
                }
            }
            
            // Failure or success, increase the count
            count++;
            
            if (allowed)
            {
                keepValues.add(nextObject);
            }
        }
        // Work out how many were left unchecked (for whatever reason)
        int sizeOriginal = returnedObject.size();
        int checksRemaining = sizeOriginal - count;
        // Note: There are use-cases where unmodifiable collections are passing through.
        //       So make sure that the collection needs modification at all
        if (keepValues.size() < sizeOriginal)
        {
            // There are values that need to be removed.  We have to modify the collection.
            try
            {
                returnedObject.clear();
                returnedObject.addAll(keepValues);
            }
            catch (UnsupportedOperationException e)
            {
                throw new AccessDeniedException("Permission-checked list must be modifiable", e);
            }
        }

        // Attach the extra permission-check data to the collection
        return PermissionCheckedCollectionMixin.create(returnedObject, cutoff, checksRemaining, sizeOriginal);
    }

    @SuppressWarnings("rawtypes")
    private Object[] decide(Authentication authentication, Object object, ConfigAttributeDefinition config, Object[] returnedObject) throws AccessDeniedException
    {
        // Assumption: value is not null
        BitSet incudedSet = new BitSet(returnedObject.length);

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
                    else if (Pair.class.isAssignableFrom(current.getClass()))
                    {
                        testNodeRef = (NodeRef) ((Pair)current).getSecond();
                    }
                    else if (PermissionCheckValue.class.isAssignableFrom(current.getClass()))
                    {
                        testNodeRef = ((PermissionCheckValue) current).getNodeRef();
                    }
                    else
                    {
                        throw new ACLEntryVoterException("The specified parameter is recognized: " + current.getClass());
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
                    else if (Pair.class.isAssignableFrom(current.getClass()))
                    {
                        testNodeRef = (NodeRef) ((Pair)current).getSecond();
                    }
                    else if (PermissionCheckValue.class.isAssignableFrom(current.getClass()))
                    {
                        NodeRef nodeRef = ((PermissionCheckValue) current).getNodeRef();
                        testNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
                    }
                    else
                    {
                        throw new ACLEntryVoterException("The specified parameter is recognized: " + current.getClass());
                    }
                }

                if (log.isDebugEnabled())
                {
                    log.debug("\t" + cad.typeString + " test on " + testNodeRef + " from " + current.getClass().getName());
                }

                if(isUnfiltered(testNodeRef))
                {
                    continue;
                }
                
                if (incudedSet.get(i) && (testNodeRef != null) && (permissionService.hasPermission(testNodeRef, cad.required.toString()) == AccessStatus.DENIED))
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
        if ((attribute.getAttribute() != null) && (attribute.getAttribute().startsWith(AFTER_ACL_NODE) || attribute.getAttribute().startsWith(AFTER_ACL_PARENT)))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    @SuppressWarnings("rawtypes")
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

            required = SimplePermissionReference.getPermissionReference(qName, permissionString);
        }
    }
}

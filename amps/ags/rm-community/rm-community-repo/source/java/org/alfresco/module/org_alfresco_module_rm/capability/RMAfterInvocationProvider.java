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

import static org.alfresco.service.cmr.search.SearchService.LANGUAGE_INDEX_SQL;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collection;
import java.util.HashMap;
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
import net.sf.acegisecurity.vote.AccessDecisionVoter;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.module.org_alfresco_module_rm.util.AuthenticationUtil;
import org.alfresco.repo.search.SimpleResultSetMetaData;
import org.alfresco.repo.search.impl.lucene.PagingLuceneResultSet;
import org.alfresco.repo.search.impl.querymodel.QueryEngineResults;
import org.alfresco.repo.security.permissions.PermissionCheckCollection;
import org.alfresco.repo.security.permissions.PermissionCheckValue;
import org.alfresco.repo.security.permissions.PermissionCheckedCollection.PermissionCheckedCollectionMixin;
import org.alfresco.repo.security.permissions.PermissionCheckedValue;
import org.alfresco.repo.security.permissions.impl.acegi.ACLEntryVoterException;
import org.alfresco.repo.security.permissions.impl.acegi.FilteringResultSet;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.PermissionEvaluationMode;
import org.alfresco.service.cmr.search.ResultSet;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * RM After Invocation Provider
 */
@SuppressWarnings("unused")
public class RMAfterInvocationProvider extends RMSecurityCommon
                                       implements AfterInvocationProvider, InitializingBean
{
    private static Log logger = LogFactory.getLog(RMAfterInvocationProvider.class);

    private static final String AFTER_RM = "AFTER_RM";

    private AuthenticationUtil authenticationUtil;
    private int maxPermissionChecks;
    private long maxPermissionCheckTimeMillis;

    public boolean supports(ConfigAttribute configAttribute)
    {
        String attribute = configAttribute.getAttribute();
        return (StringUtils.isNotBlank(attribute) && attribute.startsWith(AFTER_RM));
    }

    @SuppressWarnings("rawtypes")
    public boolean supports(Class clazz)
    {
        return (MethodInvocation.class.isAssignableFrom(clazz));
    }

    public void afterPropertiesSet()
    {
        //Do nothing
    }

    /**
     * Default constructor
     */
    public RMAfterInvocationProvider()
    {
        super();
        maxPermissionChecks = Integer.MAX_VALUE;
        maxPermissionCheckTimeMillis = Long.MAX_VALUE;
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
     * Sets the authentication util
     *
     * @param authenticationUtil The authentication util to set
     */
    public void setAuthenticationUtil(AuthenticationUtil authenticationUtil)
    {
        this.authenticationUtil = authenticationUtil;
    }

    @SuppressWarnings("rawtypes")
    public Object decide(Authentication authentication, Object object, ConfigAttributeDefinition config, Object returnedObject)
    {
        if (logger.isDebugEnabled())
        {
            MethodInvocation mi = (MethodInvocation) object;
            if (mi == null)
            {
                logger.debug("Method is null.");
            }
            else
            {
                logger.debug("Method: " + mi.getMethod().toString());
            }
        }
        try
        {
            if (authenticationUtil.isRunAsUserTheSystemUser())
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Allowing system user access");
                }
                return returnedObject;
            }
            else if (returnedObject == null)
            {
                if (logger.isDebugEnabled())
                {
                    logger.debug("Allowing null object access");
                }
                return null;
            }
            else if (PermissionCheckedValue.class.isAssignableFrom(returnedObject.getClass()))
            {
                return decide(authentication, object, config, (PermissionCheckedValue) returnedObject);
            }
            else if (PermissionCheckValue.class.isAssignableFrom(returnedObject.getClass()))
            {
                return decide(authentication, object, config, (PermissionCheckValue) returnedObject);
            }
            else if (StoreRef.class.isAssignableFrom(returnedObject.getClass()))
            {
                NodeRef rootNodeRef = decide(authentication, object, config, nodeService.getRootNode((StoreRef) returnedObject));
                if (rootNodeRef == null)
                {
                    throw new AlfrescoRuntimeException("Root node reference of '" + returnedObject + "' is null.");
                }
                return rootNodeRef.getStoreRef();
            }
            else if (NodeRef.class.isAssignableFrom(returnedObject.getClass()))
            {
                return decide(authentication, object, config, (NodeRef) returnedObject);
            }
            else if (ChildAssociationRef.class.isAssignableFrom(returnedObject.getClass()))
            {
                return decide(authentication, object, config, (ChildAssociationRef) returnedObject);
            }
            else if (AssociationRef.class.isAssignableFrom(returnedObject.getClass()))
            {
                return decide(authentication, object, config, (AssociationRef) returnedObject);
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
                if (logger.isDebugEnabled() && object != null)
                {
                    logger.debug("Uncontrolled object - access allowed for " + object.getClass().getName());
                }
                return returnedObject;
            }
        }
        catch (AccessDeniedException ade)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Access denied: " + ade.getMessage());
            }
            throw ade;
        }
        catch (RuntimeException re)
        {
            if (logger.isDebugEnabled())
            {
                logger.debug("Access denied by runtime exception: " + re.getMessage());
            }
            throw re;
        }

    }

    private PermissionCheckedValue decide(Authentication authentication, Object object, ConfigAttributeDefinition config, PermissionCheckedValue returnedObject)
    {
        // This passes as it has already been filtered
        // TODO: Get the filter that was applied and double-check
        return returnedObject;
    }

    private PermissionCheckValue decide(Authentication authentication, Object object, ConfigAttributeDefinition config, PermissionCheckValue returnedObject)
    {
        // Get the wrapped value
        NodeRef nodeRef = returnedObject.getNodeRef();
        decide(authentication, object, config, nodeRef);
        // This passes
        return returnedObject;
    }

    private NodeRef decide(Authentication authentication, Object object, ConfigAttributeDefinition config, NodeRef returnedObject)
    {
        if (returnedObject == null)
        {
            return null;
        }

        if (isUnfiltered(returnedObject))
        {
            return returnedObject;
        }

        List<ConfigAttributeDefintion> supportedDefinitions = extractSupportedDefinitions(config);
        if (supportedDefinitions.size() == 0)
        {
            return returnedObject;
        }

        int parentResult = checkRead(nodeService.getPrimaryParent(returnedObject).getParentRef());
        int childResult = checkRead(returnedObject);
        checkSupportedDefinitions(supportedDefinitions, parentResult, childResult);

        return returnedObject;
    }

    private void checkSupportedDefinitions(List<ConfigAttributeDefintion> supportedDefinitions, int parentResult, int childResult)
    {
        for (ConfigAttributeDefintion cad : supportedDefinitions)
        {
            if ((cad.parent && parentResult == AccessDecisionVoter.ACCESS_DENIED)
                        || (!cad.parent && childResult == AccessDecisionVoter.ACCESS_DENIED))
            {
                throw new AccessDeniedException("Access Denied");
            }
        }
    }

    protected boolean isUnfiltered(NodeRef nodeRef)
    {
        return !nodeService.hasAspect(nodeRef, RecordsManagementModel.ASPECT_FILE_PLAN_COMPONENT);

    }

    @SuppressWarnings("rawtypes")
    private List<ConfigAttributeDefintion> extractSupportedDefinitions(ConfigAttributeDefinition config)
    {
        List<ConfigAttributeDefintion> definitions = new ArrayList<>();
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

        int parentReadCheck = checkRead(returnedObject.getParentRef());
        int childReadCheck = checkRead(returnedObject.getChildRef());

        for (ConfigAttributeDefintion cad : supportedDefinitions)
        {
            NodeRef testNodeRef = null;

            if (cad.parent)
            {
                testNodeRef = returnedObject.getParentRef();
            }
            else
            {
                testNodeRef = returnedObject.getChildRef();
            }

            // Enforce Read Policy

            if (isUnfiltered(testNodeRef))
            {
                continue;
            }

            if ((cad.parent && parentReadCheck != AccessDecisionVoter.ACCESS_GRANTED)
                        || (childReadCheck != AccessDecisionVoter.ACCESS_GRANTED))
            {
                throw new AccessDeniedException("Access Denied");
            }
        }

        return returnedObject;
    }

    private AssociationRef decide(Authentication authentication, Object object, ConfigAttributeDefinition config, AssociationRef returnedObject)
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

            if (cad.parent)
            {
                testNodeRef = returnedObject.getSourceRef();
            }
            else
            {
                testNodeRef = returnedObject.getTargetRef();
            }

            if (isUnfiltered(testNodeRef))
            {
                continue;
            }

            if (checkRead(testNodeRef) != AccessDecisionVoter.ACCESS_GRANTED)
            {
                throw new AccessDeniedException("Access Denied");
            }

        }

        return returnedObject;
    }

    private ResultSet decide(Authentication authentication, Object object, ConfigAttributeDefinition config, PagingLuceneResultSet returnedObject)
    {
        ResultSet raw = ((FilteringResultSet) returnedObject.getWrapped()).getUnFilteredResultSet();
        ResultSet filteredForPermissions = decide(authentication, object, config, raw);
        PagingLuceneResultSet plrs = new PagingLuceneResultSet(filteredForPermissions, returnedObject.getResultSetMetaData().getSearchParameters(), nodeService);
        plrs.setTrimmedResultSet(true);
        return plrs;
    }

    private ResultSet decide(Authentication authentication, Object object, ConfigAttributeDefinition config, ResultSet returnedObject)
    {
        if (returnedObject == null)
        {
            return null;
        }

        // FIXME see: RM-6895
        if (returnedObject.getResultSetMetaData().getSearchParameters().getLanguage().equalsIgnoreCase(LANGUAGE_INDEX_SQL))
        {
            return returnedObject;
        }

        class RMFilteringResultSet extends FilteringResultSet
        {
            private long numberFound;

            public RMFilteringResultSet(ResultSet unfiltered, BitSet inclusionMask)
            {
                super(unfiltered, inclusionMask);
            }

            @Override
            public long getNumberFound()
            {
                return numberFound;
            }

            private void setNumberFound(long numberFound)
            {
                this.numberFound = numberFound;
            }
        }

        BitSet inclusionMask = new BitSet(returnedObject.length());
        RMFilteringResultSet filteringResultSet = new RMFilteringResultSet(returnedObject, inclusionMask);

        List<ConfigAttributeDefintion> supportedDefinitions = extractSupportedDefinitions(config);

        Integer maxSize = null;
        if (returnedObject.getResultSetMetaData().getSearchParameters().getMaxItems() >= 0)
        {
            maxSize = Integer.valueOf(returnedObject.getResultSetMetaData().getSearchParameters().getMaxItems());
        }
        if ((maxSize == null) && (returnedObject.getResultSetMetaData().getSearchParameters().getLimitBy() == LimitBy.FINAL_SIZE))
        {
            maxSize = Integer.valueOf(returnedObject.getResultSetMetaData().getSearchParameters().getLimit());
        }
        // Allow for skip
        if ((maxSize != null) && (returnedObject.getResultSetMetaData().getSearchParameters().getSkipCount() >= 0))
        {
            maxSize = Integer.valueOf(maxSize + returnedObject.getResultSetMetaData().getSearchParameters().getSkipCount());
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
                    inclusionMask.set(i, true);
                }
                filteringResultSet.setResultSetMetaData(
                        new SimpleResultSetMetaData(
                                returnedObject.getResultSetMetaData().getLimitedBy(),
                                PermissionEvaluationMode.EAGER,
                                returnedObject.getResultSetMetaData().getSearchParameters()));
                filteringResultSet.setNumberFound(returnedObject.getNumberFound());
                return filteringResultSet;
            }
            else
            {
                for (int i = 0; i < returnedObject.length(); i++)
                {
                    inclusionMask.set(i, true);
                }
                filteringResultSet.setResultSetMetaData(
                        new SimpleResultSetMetaData(
                                returnedObject.getResultSetMetaData().getLimitedBy(),
                                PermissionEvaluationMode.EAGER,
                                returnedObject.getResultSetMetaData().getSearchParameters()));
                filteringResultSet.setNumberFound(returnedObject.getNumberFound());
                return filteringResultSet;
            }
        }

        // record the start time
        long startTimeMillis = System.currentTimeMillis();

        // set the default, unlimited resultset type
        filteringResultSet.setResultSetMetaData(
                new SimpleResultSetMetaData(
                        returnedObject.getResultSetMetaData().getLimitedBy(),
                        PermissionEvaluationMode.EAGER,
                        returnedObject.getResultSetMetaData().getSearchParameters()));

        for (int i = 0; i < returnedObject.length(); i++)
        {
            long currentTimeMillis = System.currentTimeMillis();

            // All permission checks must pass
            inclusionMask.set(i, true);

            if (!nodeService.exists(returnedObject.getNodeRef(i)))
            {
            	inclusionMask.set(i, false);
            }
            else
            {
	            int parentCheckRead = checkRead(returnedObject.getChildAssocRef(i).getParentRef());
	            int childCheckRead = checkRead(returnedObject.getNodeRef(i));

	            for (ConfigAttributeDefintion cad : supportedDefinitions)
	            {
	                NodeRef testNodeRef = returnedObject.getNodeRef(i);
	                int checkRead = childCheckRead;
	                if (cad.parent)
	                {
	                    testNodeRef = returnedObject.getChildAssocRef(i).getParentRef();
	                    checkRead = parentCheckRead;
	                }

	                if (isUnfiltered(testNodeRef))
	                {
	                    continue;
	                }

	                if (inclusionMask.get(i) && (testNodeRef != null) && (checkRead != AccessDecisionVoter.ACCESS_GRANTED))
	                {
	                    inclusionMask.set(i, false);
	                }
	            }
            }

            // Bug out if we are limiting by size
            if ((maxSize != null) && (filteringResultSet.length() > maxSize.intValue()))
            {
                // Remove the last match to fix the correct size
                inclusionMask.set(i, false);
                filteringResultSet.setResultSetMetaData(new SimpleResultSetMetaData(LimitBy.FINAL_SIZE, PermissionEvaluationMode.EAGER, returnedObject.getResultSetMetaData()
                        .getSearchParameters()));
                break;
            }
        }

        if (maxSize != null)
        {
            LimitBy limitBy = returnedObject.length() > maxSize ? LimitBy.FINAL_SIZE : LimitBy.UNLIMITED;
            filteringResultSet.setResultSetMetaData(new SimpleResultSetMetaData(limitBy,
                    PermissionEvaluationMode.EAGER, returnedObject.getResultSetMetaData().getSearchParameters()));
        }

        filteringResultSet.setNumberFound(returnedObject.getNumberFound());

        return filteringResultSet;
    }

    private QueryEngineResults decide(Authentication authentication, Object object, ConfigAttributeDefinition config, QueryEngineResults returnedObject)
    {
        Map<Set<String>, ResultSet> map = returnedObject.getResults();
        Map<Set<String>, ResultSet> answer = new HashMap<>(map.size(), 1.0f);

        for (Map.Entry<Set<String>, ResultSet> entry : map.entrySet())
        {
            ResultSet raw = entry.getValue();
            ResultSet permed;
            if (PagingLuceneResultSet.class.isAssignableFrom(raw.getClass()))
            {
                permed = decide(authentication, object, config, (PagingLuceneResultSet) raw);
            }
            else
            {
                permed = decide(authentication, object, config, raw);
            }
            answer.put(entry.getKey(), permed);
        }

        return new QueryEngineResults(answer);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Collection decide(Authentication authentication, Object object, ConfigAttributeDefinition config, Collection returnedObject)
    {
        if (returnedObject == null)
        {
            return null;
        }

        List<ConfigAttributeDefintion> supportedDefinitions = extractSupportedDefinitions(config);
        if (logger.isDebugEnabled())
        {
            logger.debug("Entries are " + supportedDefinitions);
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
        List<Object> keepValues = new ArrayList<>(returnedObject.size());

        for (Object nextObject : returnedObject)
        {
            // if the maximum result size or time has been exceeded, then we have to remove only
            long currentTimeMillis = System.currentTimeMillis();

            // NOTE: for reference - the "maxPermissionChecks" has never been honoured by this loop (since previously the count was not being incremented)
            if (count >= targetResultCount)
            {
                // We have enough results.  We stop without cutoff.
                break;
            }
            else if (count >= maxPermissionChecks)
            {
                // We have been cut off by count
                cutoff = true;
                if (logger.isDebugEnabled())
                {
                    logger.debug("decide (collection) cut-off: " + count + " checks exceeded " + maxPermissionChecks + " checks");
                }
                break;
            }
            else if ((currentTimeMillis - startTimeMillis) > maxPermissionCheckTimeMillis)
            {
                // We have been cut off by time
                cutoff = true;
                if (logger.isDebugEnabled())
                {
                    logger.debug("decide (collection) cut-off: " + (currentTimeMillis - startTimeMillis) + "ms exceeded " + maxPermissionCheckTimeMillis + "ms");
                }
                break;
            }

            boolean allowed = true;
            for (ConfigAttributeDefintion cad : supportedDefinitions)
            {
                if (cad.mode.equalsIgnoreCase("FilterNode"))
                {
                    NodeRef testNodeRef = null;
                    if (cad.parent)
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
                        else if (AssociationRef.class.isAssignableFrom(nextObject.getClass()))
                        {
                            testNodeRef = ((AssociationRef) nextObject).getSourceRef();
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
                    else
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
                        else if (AssociationRef.class.isAssignableFrom(nextObject.getClass()))
                        {
                            testNodeRef = ((AssociationRef) nextObject).getTargetRef();
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

                    if (logger.isDebugEnabled())
                    {
                        logger.debug("\t" + cad.typeString + " test on " + testNodeRef + " from " + nextObject.getClass().getName());
                    }

                    // Null allows
                    if (isUnfiltered(testNodeRef))
                    {
                        // Continue to next ConfigAttributeDefintion
                        continue;
                    }

                    if (allowed &&
                        testNodeRef != null &&
                        checkRead(testNodeRef) != AccessDecisionVoter.ACCESS_GRANTED)
                    {
                        allowed = false;
                        // No point evaluating more ConfigAttributeDefintions
                        break;
                    }
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

    private Object[] decide(Authentication authentication, Object object, ConfigAttributeDefinition config, Object[] returnedObject)
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

            int parentReadCheck = checkRead(getParentReadCheckNode(current));
            int childReadChek = checkRead(getChildReadCheckNode(current));

            for (ConfigAttributeDefintion cad : supportedDefinitions)
            {
                incudedSet.set(i, true);
                NodeRef testNodeRef = null;
                if (cad.parent)
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
                else
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
                    else if (PermissionCheckValue.class.isAssignableFrom(current.getClass()))
                    {
                        testNodeRef = ((PermissionCheckValue) current).getNodeRef();
                    }
                    else
                    {
                        throw new ACLEntryVoterException("The specified parameter is recognized: " + current.getClass());
                    }
                }

                if (logger.isDebugEnabled())
                {
                    logger.debug("\t" + cad.typeString + " test on " + testNodeRef + " from " + current.getClass().getName());
                }

                if (isUnfiltered(testNodeRef))
                {
                    continue;
                }

                int readCheck = childReadChek;
                if (cad.parent)
                {
                    readCheck = parentReadCheck;
                }

                if (incudedSet.get(i) && (testNodeRef != null) && (readCheck != AccessDecisionVoter.ACCESS_GRANTED))
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

    private NodeRef getParentReadCheckNode(Object current)
    {
        NodeRef testNodeRef = null;
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
        else if (PermissionCheckValue.class.isAssignableFrom(current.getClass()))
        {
            NodeRef nodeRef = ((PermissionCheckValue) current).getNodeRef();
            testNodeRef = nodeService.getPrimaryParent(nodeRef).getParentRef();
        }
        else
        {
            throw new ACLEntryVoterException("The specified array is not of NodeRef or ChildAssociationRef");
        }
        return testNodeRef;
    }

    private NodeRef getChildReadCheckNode(Object current)
    {
        NodeRef testNodeRef = null;
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
        else if (PermissionCheckValue.class.isAssignableFrom(current.getClass()))
        {
            testNodeRef = ((PermissionCheckValue) current).getNodeRef();
        }
        else
        {
            throw new ACLEntryVoterException("The specified array is not of NodeRef or ChildAssociationRef");
        }
        return testNodeRef;
    }

    private class ConfigAttributeDefintion
    {

        String typeString;

        String mode;

        boolean parent = false;

        ConfigAttributeDefintion(ConfigAttribute attr)
        {

            StringTokenizer st = new StringTokenizer(attr.getAttribute(), ".", false);
            typeString = st.nextToken();
            if (!(typeString.equals(AFTER_RM)))
            {
                throw new ACLEntryVoterException("Invalid type: must be AFTER_RM");
            }
            mode = st.nextToken();

            if (st.hasMoreElements())
            {
                parent = true;
            }
        }
    }

}

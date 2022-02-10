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

package org.alfresco.module.org_alfresco_module_rm.caveat;

import static org.apache.commons.lang3.exception.ExceptionUtils.getStackTrace;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.acegisecurity.AccessDeniedException;
import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.caveat.RMListOfValuesConstraint.MatchLogic;
import org.alfresco.module.org_alfresco_module_rm.model.RecordsManagementModel;
import org.alfresco.repo.cache.SimpleCache;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.dictionary.Constraint;
import org.alfresco.service.cmr.dictionary.ConstraintDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.JSONtoFmModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * RM Caveat Config component impl
 *
 * @author janv
 */
@BehaviourBean(defaultType = "rma:caveatConfig")
public class RMCaveatConfigComponentImpl implements ContentServicePolicies.OnContentUpdatePolicy,
                                                    NodeServicePolicies.BeforeDeleteNodePolicy,
                                                    NodeServicePolicies.OnCreateNodePolicy,
                                                    RMCaveatConfigComponent
{
    private static Log logger = LogFactory.getLog(RMCaveatConfigComponentImpl.class);

    private ContentService contentService;
    private DictionaryService dictionaryService;
    private NamespaceService namespaceService;
    private AuthorityService authorityService;
    private PersonService personService;
    private NodeService nodeService;

    // Default
    private StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");

    private List<String> caveatAspectURINames = new ArrayList<>(0);
    private List<QName> caveatAspectQNames = new ArrayList<>(0);

    private List<String> caveatModelURINames = new ArrayList<>(0);
    private List<QName> caveatModelQNames = new ArrayList<>(0);

    private static final String CAVEAT_CONFIG_NAME = "caveatConfig.json";

    private static final QName DATATYPE_TEXT = DataTypeDefinition.TEXT;

    /**
     * Lock objects
     */
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    private Lock readLock = lock.readLock();
    private Lock writeLock = lock.writeLock();

    /*
     * Caveat Config (Shared) config
     * first string is property name
     * second string is authority name (user or group full name)
     * third string is list of values of property
     */
    private SimpleCache<String, Map<String, List<String>>> caveatConfig;

    public void setCaveatConfig(SimpleCache<String, Map<String, List<String>>> caveatConfig)
    {
        this.caveatConfig = caveatConfig;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setContentService(ContentService contentService)
    {
        this.contentService = contentService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    public void setPersonService(PersonService personService)
    {
        this.personService = personService;
    }

    public void setStoreRef(String storeRef)
    {
        this.storeRef = new StoreRef(storeRef);
    }

    public void setCaveatAspects(List<String> caveatAspectNames)
    {
        this.caveatAspectURINames = caveatAspectNames;
    }

    public void setCaveatModels(List<String> caveatModelNames)
    {
        this.caveatModelURINames = caveatModelNames;
    }

    /**
     * Initialise behaviours and caveat config cache
     */
    public void init()
    {
        if (caveatAspectURINames.size() > 0)
        {
            for (String caveatAspectURIName : caveatAspectURINames)
            {
                caveatAspectQNames.add(QName.createQName(caveatAspectURIName));
            }

            if (logger.isInfoEnabled())
            {
                logger.info("Caveat aspects configured "+caveatAspectQNames);
            }
        }
        else
        {
            logger.warn("No caveat aspects configured - caveats will not be applied");
        }

        if (caveatModelURINames.size() > 0)
        {
            for (String caveatModelURIName : caveatModelURINames)
            {
                caveatModelQNames.add(QName.createQName(caveatModelURIName));
            }

            if (logger.isInfoEnabled())
            {
                logger.info("Caveat models configured "+caveatModelQNames);
            }
        }
        else
        {
            logger.info("No caveat models configured - all models will be checked");
        }

        NodeRef caveatConfigNodeRef = getCaveatConfigNode();
        if (caveatConfigNodeRef != null)
        {
            validateAndReset(caveatConfigNodeRef);
        }
    }

    /**
     * @see org.alfresco.repo.content.ContentServicePolicies.OnContentUpdatePolicy#onContentUpdate(org.alfresco.service.cmr.repository.NodeRef, boolean)
     * RM-2770 - this method has to be fired on transaction commit to be able to validate the content when the content store is encrypted
     */
    @Override
    @Behaviour
    (
            kind = BehaviourKind.CLASS, 
            notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    public void onContentUpdate(NodeRef nodeRef, boolean newContent)
    {
        if (logger.isInfoEnabled())
        {
            logger.info("onContentUpdate: "+nodeRef+", "+newContent);
        }

        validateAndReset(nodeRef);
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy#beforeDeleteNode(org.alfresco.service.cmr.repository.NodeRef)
     */
    @Override
    @Behaviour(kind = BehaviourKind.CLASS)
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        if (logger.isInfoEnabled())
        {
            logger.info("beforeDeleteNode: "+nodeRef);
        }

        validateAndReset(nodeRef);
    }

    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy#onCreateNode(org.alfresco.service.cmr.repository.ChildAssociationRef)
     */
    @Override
    @Behaviour(kind = BehaviourKind.CLASS)
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        if (logger.isInfoEnabled())
        {
            logger.info("onCreateNode: "+childAssocRef);
        }

        validateAndReset(childAssocRef.getChildRef());
    }

    /**
     * Validate the caveat config and optionally update the cache.
     *
     * @param nodeRef The nodeRef of the config
    */
    @SuppressWarnings("unchecked")
    protected void validateAndReset(NodeRef nodeRef)
    {
        ContentReader cr = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);
        if (cr != null)
        {
            // TODO - check who can change caveat config !
            // TODO - locking (or checkout/checkin)

            String caveatConfigData = cr.getContentString();
            if (caveatConfigData != null)
            {
                NodeRef existing = getCaveatConfigNode();
                if ((existing != null && (! existing.equals(nodeRef))))
                {
                    throw new AlfrescoRuntimeException("Cannot create more than one caveat config (existing="+existing+", new="+nodeRef+")");
                }

                try
                {
                    if (logger.isTraceEnabled())
                    {
                        logger.trace(caveatConfigData);
                    }

                    Set<QName> models = new HashSet<>(1);
                    Set<QName> props = new HashSet<>(10);
                    Set<String> expectedPrefixes = new HashSet<>(10);

                    if (caveatModelQNames.size() > 0)
                    {
                        models.addAll(caveatModelQNames);
                    }
                    else
                    {
                        models.addAll(dictionaryService.getAllModels());
                    }

                    if (logger.isTraceEnabled())
                    {
                        logger.trace("validateAndReset: models to check "+models);
                    }

                    for (QName model : models)
                    {
                        props.addAll(dictionaryService.getProperties(model, DATATYPE_TEXT));
                        expectedPrefixes.addAll(namespaceService.getPrefixes(model.getNamespaceURI()));
                    }

                    if (props.size() == 0)
                    {
                        logger.warn("validateAndReset: no caveat properties found");
                    }
                    else
                    {
                        if (logger.isTraceEnabled())
                        {
                            logger.trace("validateAndReset: properties to check "+props);
                        }
                    }

                    Map<String, Object> caveatConfigMap = JSONtoFmModel.convertJSONObjectToMap(caveatConfigData);

                    for (Map.Entry<String, Object> conEntry : caveatConfigMap.entrySet())
                    {
                        String conStr = conEntry.getKey();

                        QName conQName = QName.resolveToQName(namespaceService, conStr);

                        // check prefix
                        String conPrefix = QName.splitPrefixedQName(conStr)[0];
                        boolean prefixFound = false;
                        for (String expectedPrefix : expectedPrefixes)
                        {
                            if (conPrefix.equals(expectedPrefix))
                            {
                                prefixFound = true;
                            }
                        }

                        if (! prefixFound)
                        {
                            throw new AlfrescoRuntimeException("Unexpected prefix: "+ conPrefix + " (" + conStr +") expected one of "+expectedPrefixes+")");
                        }

                        Map<String, List<String>> caveatMap = (Map<String, List<String>>)conEntry.getValue();

                        List<String> allowedValues = null;
                        @SuppressWarnings("unused")
                        boolean found = false;

                        for (QName propertyName : props)
                        {
                            PropertyDefinition propDef = dictionaryService.getProperty(propertyName);
                            List<ConstraintDefinition> conDefs = propDef.getConstraints();
                            for (ConstraintDefinition conDef : conDefs)
                            {
                                final Constraint con = conDef.getConstraint();
                                if (con instanceof RMListOfValuesConstraint)
                                {
                                    String conName = ((RMListOfValuesConstraint)con).getShortName();
                                    if (conName.equals(conStr))
                                    {
                                        // note: assumes only one caveat/LOV against a given property
                                        allowedValues = AuthenticationUtil.runAs(new RunAsWork<List<String>>()
                                        {
                                            public List<String> doWork()
                                            {
                                                return ((RMListOfValuesConstraint)con).getAllowedValues();
                                            }
                                        }, AuthenticationUtil.getSystemUserName());

                                        found = true;
                                        break;
                                    }
                                }
                            }
                        }

                        if (allowedValues != null)
                        {
                            if (logger.isInfoEnabled())
                            {
                                logger.info("Processing constraint: "+conQName);
                            }

                            for (Map.Entry<String, List<String>> caveatEntry : caveatMap.entrySet())
                            {
                                String authorityName = caveatEntry.getKey();
                                List<String> caveatList = caveatEntry.getValue();

                                // validate authority (user or group) - note: groups are configured with fullname (ie. GROUP_xxx)
                                if ((! authorityService.authorityExists(authorityName) && ! personService.personExists(authorityName)))
                                {
                                    // TODO - review warnings (& I18N)
                                    String msg = "User/group does not exist: "+authorityName+" (constraint="+conStr+")";
                                    logger.warn(msg);
                                }

                                // validate caveat list
                                for (String value : caveatList)
                                {
                                    if (! allowedValues.contains(value))
                                    {
                                        // TODO - review warnings (& add I18N)
                                        String msg = "Invalid value in list: "+value+" (authority="+authorityName+", constraint="+conStr+")";
                                        logger.warn(msg);
                                    }
                                }
                            }
                        }
                    }

                    try
                    {
                        writeLock.lock();
                        // we can't just clear the cache, as all puts to the cache afterwards in this transaction will be ignored
                        // first delete all keys that are now not in the config
                        caveatConfig.getKeys().retainAll(caveatConfigMap.keySet());

                        for (Map.Entry<String, Object> conEntry : caveatConfigMap.entrySet())
                        {
                            String conStr = conEntry.getKey();
                            Map<String, List<String>> caveatMap = (Map<String, List<String>>)conEntry.getValue();

                            Map<String, List<String>> cacheValue = caveatConfig.get(conStr);
                            if (cacheValue == null || !cacheValue.equals(caveatMap))
                            {
                                // update the cache
                                caveatConfig.put(conStr, caveatMap);
                            }
                        }
                    }
                    finally
                    {
                        writeLock.unlock();
                    }
                }
                catch (JSONException e)
                {
                    throw new AlfrescoRuntimeException("Invalid caveat config syntax: "+e);
                }
            }
        }
    }

    private NodeRef getCaveatConfigNode()
    {
        NodeRef rootNode = nodeService.getRootNode(storeRef);
        return nodeService.getChildByName(rootNode, RecordsManagementModel.ASSOC_CAVEAT_CONFIG, CAVEAT_CONFIG_NAME);
    }


    public NodeRef updateOrCreateCaveatConfig(InputStream is)
    {
        NodeRef caveatConfig = getOrCreateCaveatConfig();

        // Update the content
        ContentWriter writer = this.contentService.getWriter(caveatConfig, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(is);

        return caveatConfig;
    }

    public NodeRef updateOrCreateCaveatConfig(File jsonFile)
    {
        NodeRef caveatConfig = getOrCreateCaveatConfig();

        // Update the content
        ContentWriter writer = this.contentService.getWriter(caveatConfig, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(jsonFile);

        return caveatConfig;
    }

    public NodeRef updateOrCreateCaveatConfig(String jsonString)
    {
        NodeRef caveatConfig = getOrCreateCaveatConfig();

        // Update the content
        ContentWriter writer = this.contentService.getWriter(caveatConfig, ContentModel.PROP_CONTENT, true);
        writer.setMimetype(MimetypeMap.MIMETYPE_TEXT_PLAIN);
        writer.setEncoding("UTF-8");
        writer.putContent(jsonString);

        return caveatConfig;
    }

    private NodeRef getOrCreateCaveatConfig()
    {
        NodeRef caveatConfig = getCaveatConfigNode();
        if (caveatConfig == null)
        {
            NodeRef rootNode = nodeService.getRootNode(storeRef);
            nodeService.addAspect(rootNode, VersionModel.ASPECT_VERSION_STORE_ROOT, null);

            // Create caveat config
            caveatConfig = nodeService.createNode(rootNode,
                                                  RecordsManagementModel.ASSOC_CAVEAT_CONFIG,
                                                  QName.createQName(RecordsManagementModel.RM_URI, CAVEAT_CONFIG_NAME),
                                                  RecordsManagementModel.TYPE_CAVEAT_CONFIG).getChildRef();

            nodeService.setProperty(caveatConfig, ContentModel.PROP_NAME, CAVEAT_CONFIG_NAME);
        }

        return caveatConfig;
    }

    // Get list of all caveat qualified names
    public Collection<String> getRMConstraintNames()
    {
        Collection<String> rmConstraintNames = Collections.emptySet();
        try
        {
            readLock.lock();
            rmConstraintNames = caveatConfig.getKeys();
        }
        finally
        {
            readLock.unlock();
        }
        return Collections.unmodifiableCollection(rmConstraintNames);
    }

    // Get allowed values for given caveat (for current user)
    public List<String> getRMAllowedValues(String constraintName)
    {
        List<String> allowedValues = new ArrayList<>(0);

        String userName = AuthenticationUtil.getRunAsUser();
        if (userName != null && !(AuthenticationUtil.isMtEnabled() && AuthenticationUtil.isRunAsUserTheSystemUser()))
        {
            // note: userName and userGroupNames must not be null
            caveatConfig.get(constraintName);

            Set<String> userGroupFullNames = authorityService.getAuthoritiesForUser(userName);
            allowedValues = getRMAllowedValues(userName, userGroupFullNames, constraintName);
        }

        return allowedValues;
    }

    private List<String> getRMAllowedValues(String userName, Set<String> userGroupFullNames, String constraintName)
    {
        Set<String>allowedValues = new HashSet<>();

        // note: userName and userGroupNames must not be null
        Map<String, List<String>> caveatConstraintDef = null;
        try
        {
            readLock.lock();
            caveatConstraintDef = caveatConfig.get(constraintName);
        }
        finally
        {
            readLock.unlock();
        }

        if (caveatConstraintDef != null)
        {
            List<String> direct = caveatConstraintDef.get(userName);
            if(direct != null)
            {
                allowedValues.addAll(direct);
            }

            for (String group :  userGroupFullNames)
            {
                List<String> values = caveatConstraintDef.get(group);
                if(values != null)
                {
                    allowedValues.addAll(values);
                }
            }
        }

        List<String>ret = new ArrayList<>();
        ret.addAll(allowedValues);
        return Collections.unmodifiableList(ret);
    }

    /**
     * Check whether access to 'record component' node is vetoed for current user due to caveat(s)
     *
     * @param nodeRef
     * @return false, if caveat(s) veto access otherwise return true
     */
    @SuppressWarnings("unchecked")
    public boolean hasAccess(NodeRef nodeRef)
    {
        try
        {
            if ((! nodeService.exists(nodeRef)) || (caveatAspectQNames.size() == 0))
            {
                return true;
            }

            boolean found = false;
            for (QName caveatAspectQName : caveatAspectQNames)
            {
                if (nodeService.hasAspect(nodeRef, caveatAspectQName))
                {
                    found = true;
                    break;
                }
            }

            if (! found)
            {
                // no caveat aspect
                return true;
            }
            else
            {
                // check for caveats
                String userName = AuthenticationUtil.getRunAsUser();
                if (userName != null)
                {
                    // check all text properties
                    Map<QName, Serializable> props = nodeService.getProperties(nodeRef);
                    for (Map.Entry<QName, Serializable> entry : props.entrySet())
                    {
                        QName propName = entry.getKey();
                        PropertyDefinition propDef = dictionaryService.getProperty(propName);

                        if ((propDef != null) && (propDef.getDataType().getName().equals(DATATYPE_TEXT)))
                        {
                            List<ConstraintDefinition> conDefs = propDef.getConstraints();
                            for (ConstraintDefinition conDef : conDefs)
                            {
                                Constraint con = conDef.getConstraint();
                                if (con instanceof RMListOfValuesConstraint)
                                {
                                    RMListOfValuesConstraint rmCon = ((RMListOfValuesConstraint)con);
                                    String conName = rmCon.getShortName();
                                    MatchLogic matchLogic = rmCon.getMatchLogicEnum();
                                    Map<String, List<String>> caveatConstraintDef = caveatConfig.get(conName);
                                    if (caveatConstraintDef == null)
                                    {
                                        continue;
                                    }
                                    else
                                    {
                                        Set<String> userGroupNames = authorityService.getAuthoritiesForUser(userName);
                                        List<String> allowedValues = getRMAllowedValues(userName, userGroupNames, conName);

                                        List<String> propValues = null;
                                        Object val = entry.getValue();
                                        if (val instanceof String)
                                        {
                                            propValues = new ArrayList<>(1);
                                            propValues.add((String)val);
                                        }
                                        else if (val instanceof List)
                                        {
                                            propValues = (List<String>)val;
                                        }

                                        if (propValues != null && !isAllowed(propValues, allowedValues, matchLogic))
                                        {
                                            if (logger.isDebugEnabled())
                                            {
                                                logger.debug("Veto access: caveat="+conName+", userName="+userName+", nodeRef="+nodeRef+", propName="+propName+", propValues="+propValues+", allowedValues="+allowedValues);
                                            }
                                            return false;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                return true;
            }
        }
        catch (AccessDeniedException ade)
        {
            return false;
        }
    }

    private boolean isAllowed(List<String> propValues, List<String> userGroupValues, MatchLogic matchLogic)
    {
        if (matchLogic.equals(MatchLogic.AND))
        {
            // check user/group values match all values on node
            for (String propValue : propValues)
            {
                if (! userGroupValues.contains(propValue))
                {
                    if (logger.isTraceEnabled())
                    {
                        logger.trace("Not allowed: "+propValues+", "+userGroupValues+", "+matchLogic);
                    }

                    return false;
                }
            }

            return true;
        }
        else if (matchLogic.equals(MatchLogic.OR))
        {
            // check user/group values match at least one value on node
            for (String propValue : propValues)
            {
                if (userGroupValues.contains(propValue))
                {
                    return true;
                }
            }

            if (logger.isTraceEnabled())
            {
                logger.trace("Not allowed: "+propValues+", "+userGroupValues+", "+matchLogic);
            }

            return false;
        }

        logger.error("Unexpected match logic type: "+matchLogic);
        return false;
    }

    /**
     * Add a single value to an authority in a list.   The existing values of the list remain.
     *
     * @param listName the name of the RMConstraintList
     * @param authorityName
     * @param value
     * @throws AlfrescoRuntimeException if either the list or the authority do not already exist.
     */
    public void addRMConstraintListValue(String listName, String authorityName, String value)
    {
        Map<String, List<String>> members = null;
        try
        {
            readLock.lock();
            members = caveatConfig.get(listName);
            if(members == null)
            {
                throw new AlfrescoRuntimeException("unable to add to list, list not defined:"+ listName);
            }

            try
            {
                readLock.unlock();
                writeLock.lock();
                // check again
                members = caveatConfig.get(listName);
                if(members == null)
                {
                    throw new AlfrescoRuntimeException("unable to add to list, list not defined:"+ listName);
                }

                List<String> values = members.get(authorityName);
                if(values == null)
                {
                    throw new AlfrescoRuntimeException("Unable to add to authority in list.   Authority not member listName: "+ listName + " authorityName:" +authorityName);
                }
                values.add(value);

                caveatConfig.put(listName, members);
                updateOrCreateCaveatConfig(convertToJSONString(caveatConfig));
            }
            finally
            {
                readLock.lock();
                writeLock.unlock();
            }

        }
        finally
        {
            readLock.unlock();
        }
    }

    /**
     * Get the member details of the specified list
     * @param listName
     * @return the details of the specified list
     */
    public Map<String, List<String>> getListDetails(String listName)
    {
        Map<String, List<String>> listDetails = null;
        try
        {
            readLock.lock();
            listDetails = caveatConfig.get(listName);
        }
        finally
        {
            readLock.unlock();
        }
        if (listDetails == null)
        {
            return Collections.emptyMap();
        }
        else
        {
            return Collections.unmodifiableMap(listDetails);
        }
    }

    public List<QName> getRMCaveatModels()
    {
        return caveatModelQNames;
    }

    /**
     * Replace the values for an authority in a list.
     * The existing values are removed.
     *
     * If the authority does not already exist in the list, it will be added
     *
     * @param listName the name of the RMConstraintList
     * @param authorityName
     * @param values
     */
    public void updateRMConstraintListAuthority(String listName, String authorityName, List<String>values)
    {
        Map<String, List<String>> members = null;
        try
        {
            writeLock.lock();
            members = caveatConfig.get(listName);
            if(members == null)
            {
                // Create the new list, with the authority name
                Map<String, List<String>> constraint = new HashMap<>(0);
                constraint.put(authorityName, new ArrayList<>(values));
                members = constraint;
            }
            else
            {
                members.put(authorityName, new ArrayList<>(values));
            }

            caveatConfig.put(listName, members);
            updateOrCreateCaveatConfig(convertToJSONString(caveatConfig));
        }
        finally
        {
            writeLock.unlock();
        }
    }

    /**
     * Replace the authorities for a value in a list
     *
     * @param listName
     * @param valueName
     * @param authorities
     */
    public void updateRMConstraintListValue(String listName, String valueName, List<String>authorities)
    {

        Map<String, List<String>> members = caveatConfig.get(listName);
        try
        {
            writeLock.lock();

            if(members == null)
            {
                // Members List does not exist
                Map<String, List<String>> emptyConstraint = new HashMap<>(0);
                caveatConfig.put(listName, emptyConstraint);
                members = emptyConstraint;

            }
            // authorities contains authority, values[]
            // pivot contains value, members[]
            Map<String, List<String>> pivot = PivotUtil.getPivot(members);

            // remove all authorities which have this value
            List<String> existingAuthorities = pivot.get(valueName);
            if(existingAuthorities != null)
            {
                for(String authority : existingAuthorities)
                {
                    List<String> vals = members.get(authority);
                    vals.remove(valueName);
                }
            }
            // add the new authorities for this value
            for(String authority : authorities)
            {
                List<String> vals = members.get(authority);
                if(vals == null)
                {
                    vals= new ArrayList<>();
                    members.put(authority, vals);
                }
                vals.add(valueName);
            }
            caveatConfig.put(listName, members);
            updateOrCreateCaveatConfig(convertToJSONString(caveatConfig));
        }
        finally
        {
            writeLock.unlock();
        }
    }

    public void removeRMConstraintListValue(String listName, String valueName)
    {
        Map<String, List<String>> members = null;
        try
        {
            readLock.lock();

            members = caveatConfig.get(listName);
            if (members != null)
            {
                try
                {
                    readLock.unlock();
                    writeLock.lock();
                    // check again
                    members = caveatConfig.get(listName);
                    if (members != null)
                    {
                        // authorities contains authority, values[]
                        // pivot contains value, members[]
                        Map<String, List<String>> pivot = PivotUtil.getPivot(members);

                        // remove all authorities which have this value
                        List<String> existingAuthorities = pivot.get(valueName);
                        if(existingAuthorities != null)
                        {
                            for(String authority : existingAuthorities)
                            {
                                List<String> vals = members.get(authority);
                                vals.remove(valueName);
                            }
                            caveatConfig.put(listName, members);
                        }
                    }

                    updateOrCreateCaveatConfig(convertToJSONString(caveatConfig));
                }
                finally
                {
                    readLock.lock();
                    writeLock.unlock();
                }
            }
        }
        finally
        {
            readLock.unlock();
        }
    }

    /**
     * Remove an authority from a list
     *
     * @param listName the name of the RMConstraintList
     * @param authorityName
     */
    public void removeRMConstraintListAuthority(String listName, String authorityName)
    {
        Map<String, List<String>> members = null;
        try
        {
            writeLock.lock();
            members = caveatConfig.get(listName);
            if(members != null)
            {
                members.remove(listName);
            }

            caveatConfig.put(listName, members);
            updateOrCreateCaveatConfig(convertToJSONString(caveatConfig));

        }
        finally
        {
            writeLock.unlock();
        }
}

    /**
     * @param config the configuration to convert
     * @return a String containing the JSON representation of the configuration.
     */
    private String convertToJSONString(SimpleCache<String, Map<String, List<String>>> config)
    {
        JSONObject configJSONObject = new JSONObject();

        Collection<String> listNames = config.getKeys();
        for (String listName : listNames)
        {
            Map<String, List<String>> members = config.get(listName);
            JSONObject listMembers = new JSONObject();

            for (Map.Entry<String, List<String>> member : members.entrySet())
            {
                final String authorityName = member.getKey();
                final List<String> authorities = member.getValue();
                try
                {
                    listMembers.put(authorityName, authorities);
                }
                catch (JSONException error)
                {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Cannot add the key '");
                    sb.append(authorityName);
                    sb.append("' with the value '");
                    sb.append(authorities);
                    sb.append("' to the JSONObject 'listMembers' '");
                    sb.append(listMembers);
                    sb.append("': ");
                    sb.append(getStackTrace(error));
                    throw new AlfrescoRuntimeException(sb.toString());
                }
            }

            try
            {
                configJSONObject.put(listName, listMembers);
            }
            catch (JSONException error)
            {
                StringBuilder sb = new StringBuilder();
                sb.append("Cannot add the key '");
                sb.append(listName);
                sb.append("' with the value '");
                sb.append(listMembers);
                sb.append("' to the JSONObject 'configJSONObject' '");
                sb.append(configJSONObject);
                sb.append("': ");
                sb.append(getStackTrace(error));
                throw new AlfrescoRuntimeException(sb.toString());
            }
        }

        return configJSONObject.toString();
    }

    /**
     * Get an RMConstraintInfo
     * @param listQName
     * @return the constraint or null if it does not exist
     */
    public RMConstraintInfo getRMConstraint(QName listQName)
    {
        ConstraintDefinition dictionaryDef = dictionaryService.getConstraint(listQName);
        if(dictionaryDef != null)
        {
            Constraint con = dictionaryDef.getConstraint();
            if (con instanceof RMListOfValuesConstraint)
            {
                final RMListOfValuesConstraint def = (RMListOfValuesConstraint)con;

                RMConstraintInfo info = new RMConstraintInfo();
                info.setName(listQName.toPrefixString());
                info.setTitle(con.getTitle());
                List<String> allowedValues = AuthenticationUtil.runAs(new RunAsWork<List<String>>()
                {
                    public List<String> doWork()
                    {
                        return def.getAllowedValues();
                    }
                }, AuthenticationUtil.getSystemUserName());

                info.setAllowedValues(allowedValues.toArray(new String[allowedValues.size()]));
                info.setCaseSensitive(def.isCaseSensitive());
                return info;
            }
        }
        return null;
    }

    /**
     * Get RM Constraint detail.
     *
     * @return the constraintInfo or null
     */
    public RMConstraintInfo getRMConstraint(String listName)
    {
        QName listQName = QName.createQName(listName, namespaceService);
        return getRMConstraint(listQName);
    }

    public void deleteRMConstraint(String listName)
    {
        try
        {
            writeLock.lock();
            caveatConfig.remove(listName);
            updateOrCreateCaveatConfig(convertToJSONString(caveatConfig));
        }
        finally
        {
            writeLock.unlock();
        }
    }

    public void addRMConstraint(String listName)
    {
        try
        {
            writeLock.lock();
            Map<String, List<String>> emptyConstraint = new HashMap<>(0);
            caveatConfig.put(listName, emptyConstraint);
            updateOrCreateCaveatConfig(convertToJSONString(caveatConfig));
        }
        finally
        {
            writeLock.unlock();
        }
    }
}

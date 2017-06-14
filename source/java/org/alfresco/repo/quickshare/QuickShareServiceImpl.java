/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2017 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software. 
 * If the software was purchased under a paid Alfresco license, the terms of 
 * the paid license agreement will prevail.  Otherwise, the software is 
 * provided under the following open source license terms:
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
 * #L%
 */
package org.alfresco.repo.quickshare;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.events.types.ActivityEvent;
import org.alfresco.events.types.Event;
import org.alfresco.model.ContentModel;
import org.alfresco.model.QuickShareModel;
import org.alfresco.repo.Client;
import org.alfresco.repo.Client.ClientType;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.client.config.ClientAppConfig;
import org.alfresco.repo.client.config.ClientAppNotFoundException;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DoNothingCopyBehaviourCallback;
import org.alfresco.repo.events.EventPreparator;
import org.alfresco.repo.events.EventPublisher;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.client.config.ClientAppConfig.ClientApp;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.scheduled.ScheduledPersistedAction;
import org.alfresco.service.cmr.action.scheduled.ScheduledPersistedActionService;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.quickshare.InvalidSharedIdException;
import org.alfresco.service.cmr.quickshare.QuickShareDTO;
import org.alfresco.service.cmr.quickshare.QuickShareDisabledException;
import org.alfresco.service.cmr.quickshare.QuickShareLinkExpiryAction;
import org.alfresco.service.cmr.quickshare.QuickShareLinkExpiryActionPersister;
import org.alfresco.service.cmr.quickshare.QuickShareService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EmailHelper;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.Pair;
import org.alfresco.util.ParameterCheck;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.UrlUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.PeriodType;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

/**
 * QuickShare Service implementation.
 * 
 * In addition to the quick share service, this class also provides a BeforeDeleteNodePolicy and
 * OnCopyNodePolicy for content with the QuickShare aspect.
 *
 * @author Alex Miller, janv, Jamal Kaabi-Mofrad
 */
public class QuickShareServiceImpl implements QuickShareService,
            NodeServicePolicies.BeforeDeleteNodePolicy,
            CopyServicePolicies.OnCopyNodePolicy,
            NodeServicePolicies.OnRestoreNodePolicy
{
    private static final Log logger = LogFactory.getLog(QuickShareServiceImpl.class);

    static final String ATTR_KEY_SHAREDIDS_ROOT = ".sharedIds";

    private static final String FTL_SHARED_NODE_URL = "shared_node_url";
    private static final String FTL_SHARED_NODE_NAME = "shared_node_name";
    private static final String FTL_SENDER_MESSAGE = "sender_message";
    private static final String FTL_SENDER_FIRST_NAME = "sender_first_name";
    private static final String FTL_SENDER_LAST_NAME = "sender_last_name";
    private static final String FTL_TEMPLATE_ASSETS_URL = "template_assets_url";

    private static final String CONFIG_SHARED_LINK_BASE_URL = "sharedLinkBaseUrl";
    private static final String DEFAULT_EMAIL_SUBJECT = "quickshare.notifier.email.subject";
    private static final String EMAIL_TEMPLATE_REF ="alfresco/templates/quickshare-email-templates/quickshare-email.default.template.ftl";

    private AttributeService attributeService;
    private DictionaryService dictionaryService;
    private NodeService nodeService;
    private PermissionService permissionService;
    private PersonService personService;
    private PolicyComponent policyComponent;
    private TenantService tenantService;
    private ThumbnailService thumbnailService;
    private EventPublisher eventPublisher;
    private ActionService actionService;
    /** Component to determine which behaviours are active and which not */
    private BehaviourFilter behaviourFilter;
    private SearchService searchService;
    private SiteService siteService;
    private AuthorityService authorityService;
    private SysAdminParams sysAdminParams;
    private EmailHelper emailHelper;

    private boolean enabled;
    private String defaultEmailSender;
    private ClientAppConfig clientAppConfig;
    private ScheduledPersistedActionService scheduledPersistedActionService;
    private QuickShareLinkExpiryActionPersister quickShareLinkExpiryActionPersister;
    // The default period is in DAYS, but we allow HOURS|MINUTES as well for testing purposes.
    private ExpiryDatePeriod expiryDatePeriod = ExpiryDatePeriod.DAYS;
    /**
     * Set the attribute service
     */
    public void setAttributeService(AttributeService attributeService)
    {
        this.attributeService = attributeService;
    }

    /**
     * Set the dictionary service
     */
    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;	
    }
    
    /**
     * Set the node service
     */
    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    /**
     * Set the Permission service
     */
    public void setPermissionService(PermissionService permissionService)
    {
        this.permissionService = permissionService;
    }
    
    /**
     * Set the person service 
     */
    public void setPersonService(PersonService personService) 
    {
        this.personService = personService;
    }
    
    /**
     * Set the policy component
     */
    public void setPolicyComponent(PolicyComponent policyComponent)
    {
        this.policyComponent = policyComponent;
    }

    /**
     * Set the tenant service
     */
    public void setTenantService(TenantService tenantService) 
    {
        this.tenantService = tenantService;
    }
    
    /**
     * Set the thumbnail service
     */
    public void setThumbnailService(ThumbnailService thumbnailService)
    {
        this.thumbnailService = thumbnailService;
    }
    
    /**
     * Set the eventPublisher
     */
    public void setEventPublisher(EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }

    /**
     * Set the actionService
     */
    public void setActionService(ActionService actionService)
    {
        this.actionService = actionService;
    }

    /**
     * Spring configuration
     *
     * @param behaviourFilter the behaviourFilter to set
     */
    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    /**
     * Spring configuration
     *
     * @param searchService the searchService to set
     */
    public void setSearchService(SearchService searchService)
    {
        this.searchService = searchService;
    }

    /**
     * Spring configuration
     *
     * @param siteService the siteService to set
     */
    public void setSiteService(SiteService siteService)
    {
        this.siteService = siteService;
    }

    /**
     * Spring configuration
     *
     * @param authorityService the authorityService to set
     */
    public void setAuthorityService(AuthorityService authorityService)
    {
        this.authorityService = authorityService;
    }

    /**
     * Spring configuration
     *
     * @param sysAdminParams the sysAdminParams to set
     */
    public void setSysAdminParams(SysAdminParams sysAdminParams)
    {
        this.sysAdminParams = sysAdminParams;
    }

    /**
     * Spring configuration
     *
     * @param emailHelper the emailHelper to set
     */
    public void setEmailHelper(EmailHelper emailHelper)
    {
        this.emailHelper = emailHelper;
    }

    /**
     * Enable or disable this service.
     */
    public void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    /**
     * Set the default email sender
     */
    public void setDefaultEmailSender(String defaultEmailSender)
    {
        this.defaultEmailSender = defaultEmailSender;
    }

    /**
     * Set the quickShare clientAppConfig
     */
    public void setClientAppConfig(ClientAppConfig clientAppConfig)
    {
        this.clientAppConfig = clientAppConfig;
    }

    /**
     * Spring configuration
     *
     * @param scheduledPersistedActionService the scheduledPersistedActionService to set
     */
    public void setScheduledPersistedActionService(ScheduledPersistedActionService scheduledPersistedActionService)
    {
        this.scheduledPersistedActionService = scheduledPersistedActionService;
    }

    /**
     * Spring configuration
     *
     * @param quickShareLinkExpiryActionPersister the quickShareLinkExpiryActionPersister to set
     */
    public void setQuickShareLinkExpiryActionPersister(QuickShareLinkExpiryActionPersister quickShareLinkExpiryActionPersister)
    {
        this.quickShareLinkExpiryActionPersister = quickShareLinkExpiryActionPersister;
    }

    /**
     * Spring configuration
     *
     * @param expiryDatePeriod the expiryDatePeriod to set
     */
    public void setExpiryDatePeriod(String expiryDatePeriod)
    {
        if (expiryDatePeriod != null)
        {
            this.expiryDatePeriod = ExpiryDatePeriod.valueOf(expiryDatePeriod.toUpperCase());
        }
    }

    private void checkMandatoryProperties()
    {
        PropertyCheck.mandatory(this, "attributeService", attributeService);
        PropertyCheck.mandatory(this, "dictionaryService", dictionaryService);
        PropertyCheck.mandatory(this, "nodeService", nodeService);
        PropertyCheck.mandatory(this, "permissionService", permissionService);
        PropertyCheck.mandatory(this, "personService", personService);
        PropertyCheck.mandatory(this, "policyComponent", policyComponent);
        PropertyCheck.mandatory(this, "tenantService", tenantService);
        PropertyCheck.mandatory(this, "thumbnailService", thumbnailService);
        PropertyCheck.mandatory(this, "eventPublisher", eventPublisher);
        PropertyCheck.mandatory(this, "actionService", actionService);
        PropertyCheck.mandatory(this, "behaviourFilter", behaviourFilter);
        PropertyCheck.mandatory(this, "defaultEmailSender", defaultEmailSender);
        PropertyCheck.mandatory(this, "clientAppConfig", clientAppConfig);
        PropertyCheck.mandatory(this, "searchService", searchService);
        PropertyCheck.mandatory(this, "siteService", siteService);
        PropertyCheck.mandatory(this, "authorityService", authorityService);
        PropertyCheck.mandatory(this, "sysAdminParams", sysAdminParams);
        PropertyCheck.mandatory(this, "emailHelper", emailHelper);
        PropertyCheck.mandatory(this, "scheduledPersistedActionService", scheduledPersistedActionService);
        PropertyCheck.mandatory(this, "quickShareLinkExpiryActionPersister", quickShareLinkExpiryActionPersister);
    }

    /**
     * The initialise method. Register our policies.
     */
    public void init()
    {
        checkMandatoryProperties();

        // Register interest in the beforeDeleteNode policy - note: currently for content only !!
        policyComponent.bindClassBehaviour(
                QName.createQName(NamespaceService.ALFRESCO_URI, "beforeDeleteNode"),
                ContentModel.TYPE_CONTENT,
                new JavaBehaviour(this, "beforeDeleteNode"));
        
        //Register interest in the onCopyNodePolicy to block copying of quick share metadta
        policyComponent.bindClassBehaviour(
                    CopyServicePolicies.OnCopyNodePolicy.QNAME, 
                    QuickShareModel.ASPECT_QSHARE, 
                    new JavaBehaviour(this, "getCopyCallback"));

        this.policyComponent.bindClassBehaviour(
                    NodeServicePolicies.OnRestoreNodePolicy.QNAME,
                    QuickShareModel.ASPECT_QSHARE,
                    new JavaBehaviour(this, "onRestoreNode"));
    }


    @Override
    public QuickShareDTO shareContent(final NodeRef nodeRef)
    {
        return shareContent(nodeRef, null);
    }

    @Override
    public QuickShareDTO shareContent(NodeRef nodeRef, Date expiryDate) throws QuickShareDisabledException, InvalidNodeRefException
    {
        checkEnabled();

        //Check the node is the correct type
        final QName typeQName = nodeService.getType(nodeRef);
        if (isSharable(typeQName) == false)
        {
            throw new InvalidNodeRefException(nodeRef);
        }

        final String sharedId;

        // Only add the quick share aspect if it isn't already present.
        // If it is retura dto built from the existing properties.
        if (! nodeService.getAspects(nodeRef).contains(QuickShareModel.ASPECT_QSHARE))
        {
            UUID uuid = UUIDGenerator.getInstance().generateRandomBasedUUID();
            sharedId = Base64.encodeBase64URLSafeString(uuid.toByteArray()); // => 22 chars (eg. q3bEKPeDQvmJYgt4hJxOjw)

            final Map<QName, Serializable> props = new HashMap<QName, Serializable>(2);
            props.put(QuickShareModel.PROP_QSHARE_SHAREDID, sharedId);
            props.put(QuickShareModel.PROP_QSHARE_SHAREDBY, AuthenticationUtil.getRunAsUser());

            // Disable audit to preserve modifier and modified date
            // see MNT-11960
            behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
            try
            {
                // consumer/contributor should be able to add "shared" aspect (MNT-10366)
                AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
                {
                    public Void doWork()
                    {
                        nodeService.addAspect(nodeRef, QuickShareModel.ASPECT_QSHARE, props);
                        return null;
                    }
                });
            }
            finally
            {
                behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
            }

            final NodeRef tenantNodeRef = tenantService.getName(nodeRef);

            TenantUtil.runAsDefaultTenant(new TenantRunAsWork<Void>()
            {
                public Void doWork() throws Exception
                {
                    attributeService.setAttribute(tenantNodeRef, ATTR_KEY_SHAREDIDS_ROOT, sharedId);
                    return null;
                }
            });

            final StringBuffer sb = new StringBuffer();
            sb.append("{").append("\"sharedId\":\"").append(sharedId).append("\"").append("}");

            eventPublisher.publishEvent(new EventPreparator(){
                @Override
                public Event prepareEvent(String user, String networkId, String transactionId)
                {
                    return new ActivityEvent("quickshare", transactionId, networkId, user, nodeRef.getId(),
                                null, typeQName.toString(),  Client.asType(ClientType.webclient), sb.toString(),
                                null, null, 0l, null);
                }
            });

            if (logger.isInfoEnabled())
            {
                logger.info("QuickShare - shared content: "+sharedId+" ["+nodeRef+"]");
            }
        }
        else
        {
            sharedId = (String)nodeService.getProperty(nodeRef, QuickShareModel.PROP_QSHARE_SHAREDID);
            if (logger.isDebugEnabled())
            {
                logger.debug("QuickShare - content already shared: "+sharedId+" ["+nodeRef+"]");
            }
        }

        if (expiryDate != null)
        {
            AuthenticationUtil.runAsSystem((RunAsWork<Void>) () -> {
                // Create and save the expiry action
                saveSharedLinkExpiryAction(sharedId, expiryDate);
                // if we get here, it means the expiry date is validated and the action
                // is created and saved, so now set the expiryDate property.
                behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
                try
                {
                    nodeService.setProperty(nodeRef, QuickShareModel.PROP_QSHARE_EXPIRY_DATE, expiryDate);
                }
                finally
                {
                    behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
                }
                return null;
            });
        }
        return new QuickShareDTO(sharedId, expiryDate);
    }

    /**
     * Is this service enable? 
     * @throws QuickShareDisabledException if it isn't.
     */
    private void checkEnabled()
    {
        if (enabled == false) 
        {
            throw new QuickShareDisabledException("QuickShare is disabled system-wide");
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> getMetaData(NodeRef nodeRef)
    {
        // TODO This functionality MUST be available when quickshare is also disabled, therefor refactor it out from the quickshare package to a more common package.
        
        Map<QName, Serializable> nodeProps = nodeService.getProperties(nodeRef);
        ContentData contentData = (ContentData)nodeService.getProperty(nodeRef, ContentModel.PROP_CONTENT);
        
        String modifierUserName = (String)nodeProps.get(ContentModel.PROP_MODIFIER);
        Map<QName, Serializable> personProps = null;
        if (modifierUserName != null)
        {
            try
            {
                NodeRef personRef = personService.getPerson(modifierUserName);
                if (personRef != null)
                {
                    personProps = nodeService.getProperties(personRef);
                }
            }
            catch (NoSuchPersonException nspe)
            {
                // absorb this exception - eg. System (or maybe the user has been deleted)
                if (logger.isInfoEnabled())
                {
                    logger.info("MetaDataGet - no such person: "+modifierUserName);
                }
            }
        }
        
        Map<String, Object> metadata = new HashMap<String, Object>(8);
        
        metadata.put("nodeRef", nodeRef.toString());
        metadata.put("name", nodeProps.get(ContentModel.PROP_NAME));
        metadata.put("title", nodeProps.get(ContentModel.PROP_TITLE));
        
        if (contentData != null)
        {
            metadata.put("mimetype", contentData.getMimetype());
            metadata.put("size", contentData.getSize());
        }
        else
        {
            metadata.put("size", 0L);
        }
        
        metadata.put("modified", nodeProps.get(ContentModel.PROP_MODIFIED));
        
        if (personProps != null)
        {
            metadata.put("modifierFirstName", personProps.get(ContentModel.PROP_FIRSTNAME));
            metadata.put("modifierLastName", personProps.get(ContentModel.PROP_LASTNAME));
        }
        
        // thumbnail defs for this nodeRef
        List<String> thumbnailDefs = new ArrayList<String>(7);
        if (contentData != null)
        {
            // Note: thumbnail defs only appear in this list if they can produce a thumbnail for the content
            // found in the content property of this node. This will be determined by looking at the mimetype of the content
            // and the destination mimetype of the thumbnail.
            List<ThumbnailDefinition> thumbnailDefinitions = thumbnailService.getThumbnailRegistry().getThumbnailDefinitions(contentData.getMimetype(), contentData.getSize());
            for (ThumbnailDefinition thumbnailDefinition : thumbnailDefinitions)
            {
                thumbnailDefs.add(thumbnailDefinition.getName());
            }
        }
        metadata.put("thumbnailDefinitions", thumbnailDefs);
        
        // thumbnail instances for this nodeRef
        List<NodeRef> thumbnailRefs = thumbnailService.getThumbnails(nodeRef, ContentModel.PROP_CONTENT, null, null);
        List<String> thumbnailNames = new ArrayList<String>(thumbnailRefs.size());
        for (NodeRef thumbnailRef : thumbnailRefs)
        {
            thumbnailNames.add((String)nodeService.getProperty(thumbnailRef, ContentModel.PROP_NAME));
        }
        metadata.put("thumbnailNames", thumbnailNames);
        
        metadata.put("lastThumbnailModificationData", (List<String>)nodeProps.get(ContentModel.PROP_LAST_THUMBNAIL_MODIFICATION_DATA));
        
        if (nodeProps.containsKey(QuickShareModel.PROP_QSHARE_SHAREDID))
        {
            metadata.put("sharedId", nodeProps.get(QuickShareModel.PROP_QSHARE_SHAREDID));
            metadata.put("expiryDate", nodeProps.get(QuickShareModel.PROP_QSHARE_EXPIRY_DATE));

        }
        else
        {
            QName type = nodeService.getType(nodeRef);
            boolean sharable = isSharable(type);
            metadata.put("sharable", sharable);
        }

        Map<String, Object> model = new HashMap<String, Object>(2);
        model.put("item", metadata);
        return model;
    }

    @Override
    public Pair<String, NodeRef> getTenantNodeRefFromSharedId(final String sharedId)
    {
        NodeRef nodeRef = TenantUtil.runAsDefaultTenant(new TenantRunAsWork<NodeRef>()
        {
            public NodeRef doWork() throws Exception
            {
                return (NodeRef) attributeService.getAttribute(ATTR_KEY_SHAREDIDS_ROOT, sharedId);
            }
        });

        if (nodeRef == null)
        {
            /* TODO
             * Temporary fix for RA-1093 and MNT-16224. The extra lookup should be
             * removed (the same as before, just throw the 'InvalidSharedIdException' exception) when we
             * have a system wide patch to remove the 'shared' aspect of the nodes that have been archived while shared.
             */
            // TMDQ
            final String query = "+TYPE:\"cm:content\" AND +ASPECT:\"qshare:shared\" AND =qshare:sharedId:\"" + sharedId + "\"";
            SearchParameters sp = new SearchParameters();
            sp.setLanguage(SearchService.LANGUAGE_FTS_ALFRESCO);
            sp.setQuery(query);
            sp.addStore(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE);

            List<NodeRef> nodeRefs = null;
            ResultSet results = null;
            try
            {
                results = searchService.query(sp);
                nodeRefs = results.getNodeRefs();
            }
            catch (Exception ex)
            {
                throw new InvalidSharedIdException(sharedId);
            }
            finally
            {
                if (results != null)
                {
                    results.close();
                }
            }
            if (nodeRefs.size() != 1)
            {
                throw new InvalidSharedIdException(sharedId);
            }
            nodeRef = tenantService.getName(nodeRefs.get(0));
        }

        // note: relies on tenant-specific (ie. mangled) nodeRef
        String tenantDomain = tenantService.getDomain(nodeRef.getStoreRef().getIdentifier());
        return new Pair<>(tenantDomain, tenantService.getBaseName(nodeRef));
    }

    @Override
    public Map<String, Object> getMetaData(String sharedId)
    {
        checkEnabled();

        Pair<String, NodeRef> pair = getTenantNodeRefFromSharedId(sharedId);
        final String tenantDomain = pair.getFirst();
        final NodeRef nodeRef = pair.getSecond();
        
        Map<String, Object> model = TenantUtil.runAsSystemTenant(new TenantRunAsWork<Map<String, Object>>()
        {
            public Map<String, Object> doWork() throws Exception
            {
                checkQuickShareNode(nodeRef);
                
                return getMetaData(nodeRef);
            }
        }, tenantDomain);
        
        if (logger.isDebugEnabled())
        {
            logger.debug("QuickShare - retrieved metadata: "+sharedId+" ["+nodeRef+"]["+model+"]");
        }
        //model.put("nodeRef", nodeRef)
        
        return model;
    }

    private void checkQuickShareNode(final NodeRef nodeRef)
    {
        if (! nodeService.getAspects(nodeRef).contains(QuickShareModel.ASPECT_QSHARE))
        {
            throw new InvalidNodeRefException(nodeRef);
        }
    }

    // behaviour - currently registered for content only !!
    // note: will remove "share" even if node is only being archived (ie. moved to trash) => a subsequent restore will *not* restore the "share"
    public void beforeDeleteNode(final NodeRef beforeDeleteNodeRef)
    {
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                String sharedId = (String)nodeService.getProperty(beforeDeleteNodeRef, QuickShareModel.PROP_QSHARE_SHAREDID);
                if (sharedId != null)
                {
                    try
                    {
                        Pair<String, NodeRef> pair = getTenantNodeRefFromSharedId(sharedId);

                        @SuppressWarnings("unused")
                        final String tenantDomain = pair.getFirst();
                        final NodeRef nodeRef = pair.getSecond();

                        // note: deleted nodeRef might not match, eg. for upload new version -> checkin -> delete working copy
                        if (nodeRef.equals(beforeDeleteNodeRef))
                        {
                            // Disable audit to preserve modifier and modified date
                            behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
                            try
                            {
                                nodeService.removeAspect(nodeRef, QuickShareModel.ASPECT_QSHARE);
                            }
                            finally
                            {
                                behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
                            }
                            removeSharedId(sharedId);
                        }
                    }
                    catch (InvalidSharedIdException ex)
                    {
                        logger.warn("Couldn't find shareId, " + sharedId + ", attributes for node " + beforeDeleteNodeRef);
                    }
                }
                return null;
            }
        });
    }

    /* TODO
     * Temporary fix for MNT-16224. This method should be removed when we
     * have a system wide patch to remove the 'shared' aspect of the nodes that have been archived while shared.
     */
    @Override
    public void onRestoreNode(ChildAssociationRef childAssocRef)
    {
        final NodeRef childNodeRef = childAssocRef.getChildRef();
        AuthenticationUtil.runAsSystem(new RunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                if (nodeService.hasAspect(childNodeRef, QuickShareModel.ASPECT_QSHARE))
                {
                    // Disable audit to preserve modifier and modified date
                    behaviourFilter.disableBehaviour(childNodeRef, ContentModel.ASPECT_AUDITABLE);
                    try
                    {
                        nodeService.removeAspect(childNodeRef, QuickShareModel.ASPECT_QSHARE);
                    }
                    finally
                    {
                        behaviourFilter.enableBehaviour(childNodeRef, ContentModel.ASPECT_AUDITABLE);
                    }
                }
                return null;
            }
        });
    }

    private void removeSharedId(final String sharedId)
    {
        TenantUtil.runAsDefaultTenant(new TenantRunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                attributeService.removeAttribute(ATTR_KEY_SHAREDIDS_ROOT, sharedId);
                return null;
            }
        });

        try
        {
            // Remove scheduled expiry action if any
            NodeRef expiryActionNodeRef = getQuickShareLinkExpiryActionNode(sharedId);
            if (expiryActionNodeRef != null)
            {
                deleteQuickShareLinkExpiryAction(expiryActionNodeRef);
            }
        }
        catch (Exception ex)
        {
            throw new QuickShareLinkExpiryActionException("Couldn't delete the quick share link expiry action for the sharedId:" + sharedId);
        }
    }

    @Override
    public void unshareContent(final String sharedId)
    {
        Pair<String, NodeRef> pair = getTenantNodeRefFromSharedId(sharedId);
        final String tenantDomain = pair.getFirst();
        final NodeRef nodeRef = pair.getSecond();
        
        TenantUtil.runAsSystemTenant(new TenantRunAsWork<Void>()
        {
            public Void doWork() throws Exception
            {
                QName typeQName = nodeService.getType(nodeRef);
                if (! isSharable(typeQName))
                {
                    throw new InvalidNodeRefException(nodeRef);
                }
                
                String nodeSharedId = (String)nodeService.getProperty(nodeRef, QuickShareModel.PROP_QSHARE_SHAREDID);
                
                if (! EqualsHelper.nullSafeEquals(nodeSharedId, sharedId))
                {
                    logger.warn("SharedId mismatch: expected="+sharedId+",actual="+nodeSharedId);
                }

                // Disable audit to preserve modifier and modified date
                // And not to create version
                // see MNT-15654
                behaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
                try
                {
                    nodeService.removeAspect(nodeRef, QuickShareModel.ASPECT_QSHARE);
                }
                finally
                {
                    behaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
                }
                return null;
            }
        }, tenantDomain);
        
        removeSharedId(sharedId);

        if (logger.isInfoEnabled())
        {
            logger.info("QuickShare - unshared content: "+sharedId+" ["+nodeRef+"]");
        }
    }

    private boolean isSharable(QName type)
    {
        return type.equals(ContentModel.TYPE_CONTENT) || dictionaryService.isSubClass(type, ContentModel.TYPE_CONTENT);
    }
    // Prevent copying of Quick share properties on node copy.
    @Override
    public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails)
    {
        return DoNothingCopyBehaviourCallback.getInstance();
    }

    @Override
    public boolean canRead(String sharedId)
    {
        Pair<String, NodeRef> pair = getTenantNodeRefFromSharedId(sharedId);
        final String tenantDomain = pair.getFirst();
        final NodeRef nodeRef = pair.getSecond();
        
        return TenantUtil.runAsTenant(new TenantRunAsWork<Boolean>()
        {
            public Boolean doWork() throws Exception
            {
                try
                {
                    checkQuickShareNode(nodeRef);
                    return permissionService.hasPermission(nodeRef, PermissionService.READ) == AccessStatus.ALLOWED;
                }
                catch (AccessDeniedException ex)
                {
                    return false;
                }
            }
        }, tenantDomain);
        
    }

    @Override
    public void sendEmailNotification(final QuickShareEmailRequest emailRequest)
    {
        ParameterCheck.mandatory("emailRequest", emailRequest);
        emailRequest.validate();

        ClientApp clientApp = clientAppConfig.getClient(emailRequest.getClient());
        if (clientApp == null)
        {
            throw new ClientAppNotFoundException("Client was not found [" + emailRequest.getClient() + "]");
        }

        // Set the details of the person sending the email
        final String authenticatedUser = AuthenticationUtil.getFullyAuthenticatedUser();
        final NodeRef senderNodeRef = personService.getPerson(authenticatedUser, false);
        final Map<QName, Serializable> senderProps = nodeService.getProperties(senderNodeRef);
        final String senderFirstName = (String) senderProps.get(ContentModel.PROP_FIRSTNAME);
        final String senderLastName = (String) senderProps.get(ContentModel.PROP_LASTNAME);
        final String senderFullName = ((senderFirstName != null ? senderFirstName + " " : "") + (senderLastName != null ? senderLastName : "")).trim();

        // Set the default model information
        Map<String, Serializable> templateModel = new HashMap<>(6);
        templateModel.put(FTL_SENDER_FIRST_NAME, senderFirstName);
        templateModel.put(FTL_SENDER_LAST_NAME, senderLastName);
        final String sharedNodeUrl = getUrl(clientApp.getProperty(CONFIG_SHARED_LINK_BASE_URL), CONFIG_SHARED_LINK_BASE_URL)
                    + '/' + emailRequest.getSharedId();

        templateModel.put(FTL_SHARED_NODE_URL, sharedNodeUrl);
        templateModel.put(FTL_SHARED_NODE_NAME, emailRequest.getSharedNodeName());
        templateModel.put(FTL_SENDER_MESSAGE, emailRequest.getSenderMessage());
        final String templateAssetsUrl = getUrl(clientApp.getTemplateAssetsUrl(), ClientAppConfig.PROP_TEMPLATE_ASSETS_URL);
        templateModel.put(FTL_TEMPLATE_ASSETS_URL, templateAssetsUrl);

        // Set the email details
        Map<String, Serializable> actionParams = new HashMap<>();
        // Email sender. By default the current-user's email address will not be used to send this mail.
        // However, current-user's first and lastname will be used as the personal name.
        actionParams.put(MailActionExecuter.PARAM_FROM, this.defaultEmailSender);
        actionParams.put(MailActionExecuter.PARAM_FROM_PERSONAL_NAME, senderFullName);
        actionParams.put(MailActionExecuter.PARAM_SUBJECT, DEFAULT_EMAIL_SUBJECT);
        actionParams.put(MailActionExecuter.PARAM_SUBJECT_PARAMS, new Object[] { senderFirstName, senderLastName, emailRequest.getSharedNodeName() });
        actionParams.put(MailActionExecuter.PARAM_IGNORE_SEND_FAILURE, emailRequest.isIgnoreSendFailure());
        // Pick the template
        final String templatePath = emailHelper.getEmailTemplate(clientApp.getName(), getSharedLinkEmailTemplatePath(clientApp), EMAIL_TEMPLATE_REF);
        actionParams.put(MailActionExecuter.PARAM_TEMPLATE, templatePath);
        actionParams.put(MailActionExecuter.PARAM_TEMPLATE_MODEL, (Serializable) templateModel);
        actionParams.put(MailActionExecuter.PARAM_LOCALE, getDefaultIfNull(
                    emailHelper.getUserLocaleOrDefault(authenticatedUser),
                    emailRequest.getLocale()));

        for (String to : emailRequest.getToEmails())
        {
            Map<String, Serializable> params = new HashMap<>(actionParams);
            params.put(MailActionExecuter.PARAM_TO, to);
            Action mailAction = actionService.createAction(MailActionExecuter.NAME, params);
            actionService.executeAction(mailAction, null, false, true);
        }
    }

    protected String getSharedLinkEmailTemplatePath(ClientApp clientApp)
    {
        return clientApp.getProperty("sharedLinkTemplatePath");
    }

    @Override
    public boolean canDeleteSharedLink(NodeRef nodeRef, String sharedByUserId)
    {
        boolean canDeleteSharedLink = false;

        String currentUser = AuthenticationUtil.getFullyAuthenticatedUser();
        String siteName = getSiteName(nodeRef);
        boolean isSharedByCurrentUser = currentUser.equals(sharedByUserId);

        if (siteName != null)
        {
            // node belongs to a site - current user must be a manager or collaborator or someone who shared the link
            String role = siteService.getMembersRole(siteName, currentUser);
            if (isSharedByCurrentUser || (role != null && (role.equals(SiteModel.SITE_MANAGER) || role.equals(SiteModel.SITE_COLLABORATOR))))
            {
                canDeleteSharedLink = true;
            }
        }
        else if (isSharedByCurrentUser || (authorityService.isAdminAuthority(currentUser)))
        {
            // node does not belongs to a site - current user must be the person who shared the link or an admin
            canDeleteSharedLink = true;
        }

        return canDeleteSharedLink;
    }

    @Override
    public boolean isQuickShareEnabled()
    {
        return this.enabled;
    }

    private String getSiteName(NodeRef nodeRef)
    {
        NodeRef parent = nodeService.getPrimaryParent(nodeRef).getParentRef();
        while (parent != null && !nodeService.getType(parent).equals(SiteModel.TYPE_SITE))
        {
            // check that we can read parent name
            String parentName = (String) nodeService.getProperty(parent, ContentModel.PROP_NAME);

            if (nodeService.getPrimaryParent(nodeRef) != null)
            {
                parent = nodeService.getPrimaryParent(parent).getParentRef();
            }
        }

        if (parent == null)
        {
            return null;
        }

        return nodeService.getProperty(parent, ContentModel.PROP_NAME).toString();
    }

    private String getUrl(String url, String propName)
    {
        if (url == null)
        {
            logger.warn("URL for the property [" + propName + "] is not configured.");
            return "";
        }
        if (url.endsWith("/"))
        {
            url = url.substring(0, url.length() - 1);
        }
        // Replace '${shareUrl} placeholder if it does exist.
        return UrlUtil.replaceShareUrlPlaceholder(url, sysAdminParams);
    }

    private <T> T getDefaultIfNull(T defaultValue, T newValue)
    {
        return (newValue == null) ? defaultValue : newValue;
    }

    /**
     * Represents an email request to send a quick share link.
     */
    public static class QuickShareEmailRequest
    {
        /**
         * The client's name that must be registered in order to send emails.
         */
        private String client;
        /**
         * Optional Locale for subject and body text
         */
        private Locale locale;

        /**
         * The email addresses (1 or many) of the recipients.
         */
        private Set<String> toEmails;

        /**
         * The shared id.
         */
        private String sharedId;

        /**
         * The shared content name.
         */
        private String sharedNodeName;

        /**
         * Optional message from the sender.
         */
        private String senderMessage;

        /**
         * Whether to ignore throwing exception or not. The default is false.
         */
        private boolean ignoreSendFailure = false;

        public void validate()
        {
            ParameterCheck.mandatoryCollection("toEmails", toEmails);
            ParameterCheck.mandatoryString("sharedId", sharedId);
            ParameterCheck.mandatoryString("sharedNodeName", sharedNodeName);
        }

        /**
         * {@link QuickShareEmailRequest#locale}
         */
        public Locale getLocale()
        {
            return this.locale;
        }

        /**
         * {@link QuickShareEmailRequest#locale}
         */
        public void setLocale(Locale locale)
        {
            this.locale = locale;
        }

        /**
         * {@link QuickShareEmailRequest#toEmails}
         */
        public Set<String> getToEmails()
        {
            return this.toEmails;
        }

        /**
         * {@link QuickShareEmailRequest#toEmails}
         */
        public void setToEmails(Collection<String> toEmails)
        {
            if (toEmails != null)
            {
                this.toEmails = Collections.unmodifiableSet(new HashSet<>(toEmails));
            }
        }

        /**
         * {@link QuickShareEmailRequest#client}
         */
        public String getClient()
        {
            return client;
        }

        /**
         * {@link QuickShareEmailRequest#client}
         */
        public QuickShareEmailRequest setClient(String client)
        {
            this.client = client;
            return this;
        }

        /**
         * {@link QuickShareEmailRequest#sharedId}
         */
        public String getSharedId()
        {
            return sharedId;
        }

        /**
         * {@link QuickShareEmailRequest#sharedId}
         */
        public QuickShareEmailRequest setSharedId(String sharedId)
        {
            this.sharedId = sharedId;
            return this;
        }

        /**
         * {@link QuickShareEmailRequest#sharedNodeName}
         */
        public String getSharedNodeName()
        {
            return sharedNodeName;
        }

        /**
         * {@link QuickShareEmailRequest#sharedNodeName}
         */
        public void setSharedNodeName(String sharedNodeName)
        {
            this.sharedNodeName = sharedNodeName;
        }

        /**
         * {@link QuickShareEmailRequest#senderMessage}
         */
        public String getSenderMessage()
        {
            return senderMessage;
        }

        /**
         * {@link QuickShareEmailRequest#senderMessage}
         */
        public void setSenderMessage(String senderMessage)
        {
            this.senderMessage = senderMessage;
        }

        /**
         * {@link QuickShareEmailRequest#ignoreSendFailure}
         */
        public boolean isIgnoreSendFailure()
        {
            return ignoreSendFailure;
        }

        /**
         * {@link QuickShareEmailRequest#ignoreSendFailure}
         */
        public void setIgnoreSendFailure(Boolean ignoreSendFailure)
        {
            if (ignoreSendFailure != null)
            {
                this.ignoreSendFailure = ignoreSendFailure;
            }
        }
    }

    /**
     * Creates and persists the quick share link expiry action and its related schedule.
     */
    protected void saveSharedLinkExpiryAction(String sharedId, Date expiryDate)
    {
        ParameterCheck.mandatory("expiryDate", expiryDate);
        // Validate the given expiry date
        checkExpiryDate(expiryDate);

        if (logger.isDebugEnabled())
        {
            logger.debug("Creating shared link expiry action for the sharedId:" + sharedId);
        }

        final NodeRef expiryActionNodeRef = getQuickShareLinkExpiryActionNode(sharedId);
        // If an expiry action already exists for the specified shared Id, first remove it, before creating a new one.
        if (expiryActionNodeRef != null)
        {
            deleteQuickShareLinkExpiryAction(expiryActionNodeRef);
        }

        // Create the expiry action
        final QuickShareLinkExpiryAction expiryAction = new QuickShareLinkExpiryActionImpl(java.util.UUID.randomUUID().toString(), sharedId,
                    "QuickShare link expiry action");
        // Create the persisted schedule
        final ScheduledPersistedAction schedule = scheduledPersistedActionService.createSchedule(expiryAction);

        // first set the scheduledAction so we can set the other information
        expiryAction.setSchedule(schedule);
        expiryAction.setScheduleStart(expiryDate);

        try
        {
            TenantUtil.runAsDefaultTenant((TenantRunAsWork<Void>) () -> {
                quickShareLinkExpiryActionPersister.saveQuickShareLinkExpiryAction(expiryAction);
                scheduledPersistedActionService.saveSchedule(schedule);

                return null;
            });

        }
        catch (Exception ex)
        {
            throw new QuickShareLinkExpiryActionException("Couldn't create quick share link expiry action.", ex);
        }

        if (logger.isDebugEnabled())
        {
            logger.debug("Quick share link expiry action is created for sharedId[" + sharedId + "] and it's scheduled to be executed on: "
                        + expiryDate);
        }
    }

    @Override
    public void deleteQuickShareLinkExpiryAction(QuickShareLinkExpiryAction linkExpiryAction)
    {
        ParameterCheck.mandatory("linkExpiryAction", linkExpiryAction);

        NodeRef nodeRef = null;
        try
        {
            Pair<String, NodeRef> pair = getTenantNodeRefFromSharedId(linkExpiryAction.getSharedId());
            nodeRef = pair.getSecond();
        }
        catch (InvalidSharedIdException ex)
        {
            // do nothing, as the node might be already unshared
        }
        final NodeRef sharedNodeRef = nodeRef;

        TenantUtil.runAsSystemTenant(() -> {
            // Delete the expiry action and its related persisted schedule
            deleteQuickShareLinkExpiryActionImpl(linkExpiryAction);

            // As the method is called directly (ie. not via unshareContent method which removes the aspect properties),
            // then we have to remove the 'expiryDate' property as well.
            if (sharedNodeRef != null && nodeService.getProperty(sharedNodeRef, QuickShareModel.PROP_QSHARE_EXPIRY_DATE) != null)
            {
                behaviourFilter.disableBehaviour(sharedNodeRef, ContentModel.ASPECT_AUDITABLE);
                try
                {
                    nodeService.removeProperty(sharedNodeRef, QuickShareModel.PROP_QSHARE_EXPIRY_DATE);
                }
                finally
                {
                    behaviourFilter.enableBehaviour(sharedNodeRef, ContentModel.ASPECT_AUDITABLE);
                }
            }
            return null;
        }, TenantUtil.getCurrentDomain());
    }

    /**
     * Removes (hard deletes) the previously persisted {@link QuickShareLinkExpiryAction} and its related
     * schedule {@link ScheduledPersistedAction} from the repository.
     */
    protected void deleteQuickShareLinkExpiryAction(NodeRef linkExpiryActionNodeRef)
    {
        TenantUtil.runAsDefaultTenant(() -> {
            QuickShareLinkExpiryAction linkExpiryAction = quickShareLinkExpiryActionPersister.loadQuickShareLinkExpiryAction(linkExpiryActionNodeRef);
            // Delete the expiry action and its related persisted schedule
            deleteQuickShareLinkExpiryActionImpl(linkExpiryAction);
            return null;
        });
    }

    private void deleteQuickShareLinkExpiryActionImpl(QuickShareLinkExpiryAction linkExpiryAction)
    {
        // Attach the schedule if null. This could be the case when the Action is
        // loaded from the quickShareLinkExpiryActionPersister
        attachSchedule(linkExpiryAction);
        if (linkExpiryAction.getSchedule() != null)
        {
            scheduledPersistedActionService.deleteSchedule(linkExpiryAction.getSchedule());
        }
        quickShareLinkExpiryActionPersister.deleteQuickShareLinkExpiryAction(linkExpiryAction);
    }

    private QuickShareLinkExpiryAction attachSchedule(QuickShareLinkExpiryAction quickShareLinkExpiryAction)
    {
        if (quickShareLinkExpiryAction.getSchedule() == null)
        {
            ScheduledPersistedAction schedule = scheduledPersistedActionService.getSchedule(quickShareLinkExpiryAction);
            quickShareLinkExpiryAction.setSchedule(schedule);

        }
        return quickShareLinkExpiryAction;
    }

    private NodeRef getQuickShareLinkExpiryActionNode(String sharedId)
    {
        final QName expiryActionQName = QuickShareLinkExpiryActionImpl.createQName(sharedId);
        return TenantUtil.runAsDefaultTenant(() -> quickShareLinkExpiryActionPersister.getQuickShareLinkExpiryActionNode(expiryActionQName));
    }

    private void checkExpiryDate(Date expiryDate)
    {
        DateTime now = DateTime.now();
        if (now.isAfter(expiryDate.getTime()))
        {
            throw new QuickShareLinkExpiryActionException.InvalidExpiryDateException("Invalid expiry date. Expiry date can't be in the past.");
        }
        if (expiryDatePeriod.getDuration(now, new DateTime(expiryDate)) < 1)
        {
            throw new QuickShareLinkExpiryActionException.InvalidExpiryDateException(
                        "Invalid expiry date. Expiry date can't be less then 1 " + expiryDatePeriod.getMessage() + '.');
        }
    }

    /**
     * A helper enum to get the number of days/hours/minutes between two dates.
     *
     * @author Jamal Kaabi-Mofrad
     */
    private enum ExpiryDatePeriod
    {
        DAYS
        {
            @Override
            int getDuration(DateTime now, DateTime expiryDate)
            {
                Interval interval = new Interval(now.withSecondOfMinute(0).withMillisOfSecond(0), expiryDate);
                return interval.toPeriod(PeriodType.days()).getDays();
            }

            @Override
            String getMessage()
            {
                return "day (24 hours)";
            }
        },
        HOURS
        {
            @Override
            int getDuration(DateTime now, DateTime expiryDate)
            {
                Interval interval = new Interval(now.withSecondOfMinute(0).withMillisOfSecond(0), expiryDate);
                return interval.toPeriod(PeriodType.hours()).getHours();
            }

            @Override
            String getMessage()
            {
                return "hour";
            }
        },
        MINUTES
        {
            @Override
            public int getDuration(DateTime now, DateTime expiryDate)
            {
                Interval interval = new Interval(now.withMillisOfSecond(0), expiryDate);
                return interval.toPeriod(PeriodType.minutes()).getMinutes();
            }

            @Override
            String getMessage()
            {
                return "minute";
            }
        };

        abstract int getDuration(DateTime now, DateTime expiryDate);

        abstract String getMessage();
    }
}


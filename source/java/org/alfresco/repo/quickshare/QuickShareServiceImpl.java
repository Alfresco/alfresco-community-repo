/*
 * Copyright (C) 2005-2014 Alfresco Software Limited.
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
package org.alfresco.repo.quickshare;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.events.types.ActivityEvent;
import org.alfresco.events.types.Event;
import org.alfresco.model.ContentModel;
import org.alfresco.model.QuickShareModel;
import org.alfresco.repo.Client;
import org.alfresco.repo.Client.ClientType;
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
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.tenant.TenantUtil.TenantRunAsWork;
import org.alfresco.repo.thumbnail.ThumbnailDefinition;
import org.alfresco.service.cmr.attributes.AttributeService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.quickshare.InvalidSharedIdException;
import org.alfresco.service.cmr.quickshare.QuickShareDTO;
import org.alfresco.service.cmr.quickshare.QuickShareDisabledException;
import org.alfresco.service.cmr.quickshare.QuickShareService;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.NoSuchPersonException;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;
import org.alfresco.util.Pair;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.safehaus.uuid.UUID;
import org.safehaus.uuid.UUIDGenerator;

/**
 * QuickShare Service implementation.
 * 
 * In addition to the quick share service, this class also provides a BeforeDeleteNodePolicy and
 * OnCopyNodePolicy for content with the QuickShare aspect.
 *
 * @author Alex Miller, janv
 */
public class QuickShareServiceImpl implements QuickShareService, NodeServicePolicies.BeforeDeleteNodePolicy, CopyServicePolicies.OnCopyNodePolicy
{
    private static final Log logger = LogFactory.getLog(QuickShareServiceImpl.class);

    static final String ATTR_KEY_SHAREDIDS_ROOT = ".sharedIds";
    
    
    private boolean enabled;

    private AttributeService attributeService;
    private DictionaryService dictionaryService;
    private NodeService nodeService;
    private PermissionService permissionService;
    private PersonService personService;
    private PolicyComponent policyComponent;
    private TenantService tenantService;
    private ThumbnailService thumbnailService;
    private EventPublisher eventPublisher;
    
    /** Component to determine which behaviours are active and which not */
    private BehaviourFilter behaviourFilter;
    
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
     * Enable or disable this service.
     */
    public void setEnabled(boolean enabled) 
    {
        this.enabled = enabled;
    }
    
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
     * The initialise method. Register our policies.
     */
    public void init()
    {
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
    }


    @Override
    public QuickShareDTO shareContent(final NodeRef nodeRef)
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
        
        
        return new QuickShareDTO(sharedId);
     }

    /**
     * Is this service enable? 
     * @throws uickShareDisabledException if it isn't.
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
        }
        else
        {
        	QName type = nodeService.getType(nodeRef);
        	boolean sharable = isSharable(type);
        	metadata.put("sharable", sharable);        	
        }

        Map<String, Object> model = new HashMap<String, Object>(1);
        model.put("item", metadata);
        return model;
    }

    @Override
    public Pair<String, NodeRef> getTenantNodeRefFromSharedId(final String sharedId)
    {
        final NodeRef nodeRef = TenantUtil.runAsDefaultTenant(new TenantRunAsWork<NodeRef>()
        {
            public NodeRef doWork() throws Exception
            {
                return (NodeRef)attributeService.getAttribute(ATTR_KEY_SHAREDIDS_ROOT, sharedId);
            }
        });
        
        if (nodeRef == null)
        {
            throw new InvalidSharedIdException(sharedId);
        }
        
        // note: relies on tenant-specific (ie. mangled) nodeRef
        String tenantDomain = tenantService.getDomain(nodeRef.getStoreRef().getIdentifier());
        
        return new Pair<String, NodeRef>(tenantDomain, tenantService.getBaseName(nodeRef));
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
                
                nodeService.removeAspect(nodeRef, QuickShareModel.ASPECT_QSHARE);
                
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

}

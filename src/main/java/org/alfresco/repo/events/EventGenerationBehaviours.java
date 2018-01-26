/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repo.events;

import static org.alfresco.repo.site.SiteModel.TYPE_SITE;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.events.types.DataType;
import org.alfresco.events.types.NodeAddedEvent;
import org.alfresco.events.types.NodeCheckOutCancelledEvent;
import org.alfresco.events.types.NodeCheckedInEvent;
import org.alfresco.events.types.NodeCheckedOutEvent;
import org.alfresco.events.types.NodeContentGetEvent;
import org.alfresco.events.types.NodeContentPutEvent;
import org.alfresco.events.types.NodeMovedEvent;
import org.alfresco.events.types.NodeRemovedEvent;
import org.alfresco.events.types.NodeUpdatedEvent;
import org.alfresco.events.types.Property;
import org.alfresco.events.types.authority.AuthorityAddedToGroupEvent;
import org.alfresco.events.types.authority.AuthorityRemovedFromGroupEvent;
import org.alfresco.events.types.authority.GroupDeletedEvent;
import org.alfresco.events.types.permission.InheritPermissionsDisabledEvent;
import org.alfresco.events.types.permission.InheritPermissionsEnabledEvent;
import org.alfresco.events.types.permission.LocalPermissionGrantedEvent;
import org.alfresco.events.types.permission.LocalPermissionRevokedEvent;
import org.alfresco.events.types.recordsmanagement.FileClassifiedEvent;
import org.alfresco.events.types.recordsmanagement.FileUnclassifiedEvent;
import org.alfresco.events.types.recordsmanagement.RecordCreatedEvent;
import org.alfresco.events.types.recordsmanagement.RecordRejectedEvent;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;

/**
 * 
 * @author steveglover
 *
 */
public class EventGenerationBehaviours extends AbstractEventGenerationBehaviours implements
        ContentServicePolicies.OnContentPropertyUpdatePolicy,
        ContentServicePolicies.OnContentReadPolicy,
        NodeServicePolicies.OnCreateNodePolicy,
        NodeServicePolicies.BeforeDeleteNodePolicy,
        NodeServicePolicies.OnAddAspectPolicy,
        NodeServicePolicies.OnUpdatePropertiesPolicy,
        NodeServicePolicies.OnMoveNodePolicy,
        CheckOutCheckInServicePolicies.BeforeCheckOut,
        CheckOutCheckInServicePolicies.OnCheckOut,
        CheckOutCheckInServicePolicies.OnCheckIn,
        CheckOutCheckInServicePolicies.OnCancelCheckOut,
        NodeServicePolicies.OnDeleteChildAssociationPolicy,
        NodeServicePolicies.OnCreateChildAssociationPolicy
{  
    private static final QName POLICY_ON_GROUP_DELETED = QName.createQName(NamespaceService.ALFRESCO_URI, "onGroupDeleted");
    private static final QName POLICY_ON_AUTHORITY_REMOVED_FROM_GROUP = QName.createQName(NamespaceService.ALFRESCO_URI, "onAuthorityRemovedFromGroup");
    private static final QName POLICY_ON_AUTHORITY_ADDED_TO_GROUP = QName.createQName(NamespaceService.ALFRESCO_URI, "onAuthorityAddedToGroup");
    private static final QName POLICY_ON_REVOKE_LOCAL_PERMISSION = QName.createQName(NamespaceService.ALFRESCO_URI, "onRevokeLocalPermission");
    private static final QName POLICY_ON_GRANT_LOCAL_PERMISSION = QName.createQName(NamespaceService.ALFRESCO_URI, "onGrantLocalPermission");
    private static final QName POLICY_ON_INHERIT_PERMISSIONS_DISABLED = QName.createQName(NamespaceService.ALFRESCO_URI, "onInheritPermissionsDisabled");
    private static final QName POLICY_ON_INHERIT_PERMISSIONS_ENABLED = QName.createQName(NamespaceService.ALFRESCO_URI, "onInheritPermissionsEnabled");
    //Records management policies
    private static final QName POLICY_ON_UPDATE_SECURITY_MARKS = QName.createQName(NamespaceService.ALFRESCO_URI, "onUpdateContentSecurityMarks");
    private static final QName POLICY_ON_RECORD_DECLARATION = QName.createQName(NamespaceService.ALFRESCO_URI, "onRecordDeclaration");    
    private static final QName POLICY_ON_RECORD_REJECTION = QName.createQName(NamespaceService.ALFRESCO_URI, "beforeRecordRejection");
    
    protected EventsService eventsService;
    protected DictionaryService dictionaryService;
    protected NamespaceService namespaceService;
    protected NodeService nodeService;

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }

    public void setEventsService(EventsService eventsService)
    {
        this.eventsService = eventsService;
    }

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void init()
    {
        bindClassPolicy(ContentServicePolicies.OnContentPropertyUpdatePolicy.QNAME, NodeContentPutEvent.EVENT_TYPE);

        bindClassPolicy(ContentServicePolicies.OnContentReadPolicy.QNAME, NodeContentGetEvent.EVENT_TYPE);

        bindClassPolicy(NodeServicePolicies.OnCreateNodePolicy.QNAME, NodeAddedEvent.EVENT_TYPE);

        // on before delete so that we have the relevant node details available
        bindClassPolicy(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, NodeRemovedEvent.EVENT_TYPE);

        bindClassPolicy(NodeServicePolicies.OnMoveNodePolicy.QNAME, NodeMovedEvent.EVENT_TYPE);

        bindClassPolicy(CheckOutCheckInServicePolicies.BeforeCheckOut.QNAME, NodeCheckedOutEvent.EVENT_TYPE);
        
        bindClassPolicy(CheckOutCheckInServicePolicies.OnCheckOut.QNAME, NodeCheckedOutEvent.EVENT_TYPE);

        bindClassPolicy(CheckOutCheckInServicePolicies.OnCheckIn.QNAME, NodeCheckedInEvent.EVENT_TYPE);

        bindClassPolicy(CheckOutCheckInServicePolicies.OnCancelCheckOut.QNAME, NodeCheckOutCancelledEvent.EVENT_TYPE);

        bindClassPolicy(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME);
        
        bindClassPolicy(POLICY_ON_INHERIT_PERMISSIONS_ENABLED, InheritPermissionsEnabledEvent.EVENT_TYPE);
        
        bindClassPolicy(POLICY_ON_INHERIT_PERMISSIONS_DISABLED, InheritPermissionsDisabledEvent.EVENT_TYPE);
        
        bindClassPolicy(POLICY_ON_GRANT_LOCAL_PERMISSION, LocalPermissionGrantedEvent.EVENT_TYPE);
        
        bindClassPolicy(POLICY_ON_REVOKE_LOCAL_PERMISSION, LocalPermissionRevokedEvent.EVENT_TYPE);
        
        bindClassPolicy(POLICY_ON_AUTHORITY_ADDED_TO_GROUP, AuthorityAddedToGroupEvent.EVENT_TYPE);
        
        bindClassPolicy(POLICY_ON_AUTHORITY_REMOVED_FROM_GROUP, AuthorityRemovedFromGroupEvent.EVENT_TYPE);
        
        bindClassPolicy(POLICY_ON_GROUP_DELETED, GroupDeletedEvent.EVENT_TYPE);
        
        bindAssociationPolicy(NodeServicePolicies.OnDeleteChildAssociationPolicy.QNAME, NodeRemovedEvent.EVENT_TYPE);
        
        bindAssociationPolicy(NodeServicePolicies.OnCreateChildAssociationPolicy.QNAME, NodeAddedEvent.EVENT_TYPE);
        
        //Bind to Records Management policies
        bindClassPolicy(POLICY_ON_UPDATE_SECURITY_MARKS);
        bindClassPolicy(POLICY_ON_RECORD_DECLARATION, RecordCreatedEvent.EVENT_TYPE);
        bindClassPolicy(POLICY_ON_RECORD_REJECTION, RecordRejectedEvent.EVENT_TYPE);
	}

    private DataType getPropertyType(QName propertyName)
    {
        DataType dataType = null;

        PropertyDefinition def = dictionaryService.getProperty(propertyName);
        if(def != null)
        {
            DataTypeDefinition dataTypeDef = def.getDataType();

            String dataTypeDefStr = dataTypeDef.getName().getPrefixString().substring(2);
            StringBuilder dataTypeName = new StringBuilder(dataTypeDefStr.substring(0, 1).toUpperCase());
            dataTypeName.append(dataTypeDefStr.substring(1));
            dataType = DataType.valueOf(dataTypeName.toString());
        }

        return dataType;
    }

    private Set<String> getRemoves(Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        Set<QName> tmp = new HashSet<QName>(before.keySet());
        tmp.removeAll(after.keySet());

        Set<String> ret = new HashSet<String>();
        for(QName propQName : tmp)
        {
            ret.add(propQName.toPrefixString(namespaceService));
        }

        return ret;
    }

    private Map<String, Property> getAdds(Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        Set<QName> tmp = new HashSet<QName>(after.keySet());
        tmp.removeAll(before.keySet());

        Map<String, Property> ret = new HashMap<String, Property>();
        for(QName propQName : tmp)
        {
            Serializable value = after.get(propQName);
            DataType type = getPropertyType(propQName);
            String propName = propQName.toPrefixString(namespaceService);
            Property property = new Property(propName, value, type);
            ret.put(propName, property);
        }
        return ret;
    }

    private Map<String, Property> getChanges(Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        Map<String, Property> ret = new HashMap<String, Property>();
        Set<QName> intersect = new HashSet<QName>(before.keySet());
        intersect.retainAll(after.keySet());
        for(QName propQName : intersect)
        {
            Serializable valueBefore = before.get(propQName);
            Serializable valueAfter = after.get(propQName);

            Serializable value = null;
            if(valueBefore == null && valueAfter == null)
            {
                continue;
            }
            else if(valueBefore == null && valueAfter != null)
            {
                value = valueAfter;
            }
            else if(valueBefore != null && valueAfter == null)
            {
                value = valueAfter;
            }
            else if(!valueBefore.equals(valueAfter))
            {
                value = valueAfter;
            }

            DataType type = getPropertyType(propQName);
            String propName = propQName.toPrefixString(namespaceService);
            Property property = new Property(propName, value, type);
            ret.put(propName, property);
        }
        return ret;
    }

    @Override
    public void onContentRead(NodeRef nodeRef)
    {
        eventsService.contentGet(nodeRef);
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        NodeRef nodeRef = childAssocRef.getChildRef();
        eventsService.nodeCreated(nodeRef);
    }

    /*
     * Checks whether a property has changed value (not including being null before)
     */
    private <T> boolean propertyChanged(Map<QName, Serializable> before, Map<QName, Serializable> after, QName propertyQName)
    {
        boolean isChanged = false;

        T valueBefore = (T)before.get(propertyQName);
        T valueAfter = (T)after.get(propertyQName);

        if(valueBefore != null && valueAfter != null)
        {
            isChanged = !valueBefore.equals(valueAfter);
        }

        return isChanged;
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        checkNamePropertyRenamed(nodeRef, before, after);

        checkSiteTitlePropertyRenamed(nodeRef, before, after);

        checkNodeUpdatedEventIncluded(nodeRef, before, after);
    }

    private void checkSiteTitlePropertyRenamed(NodeRef nodeRef, Map<QName, Serializable> before,
            Map<QName, Serializable> after)
    {
        QName nodeRefType = nodeService.getType(nodeRef);

        if (dictionaryService.isSubClass(nodeRefType, TYPE_SITE)
                && propertyChanged(before, after, ContentModel.PROP_TITLE))
        {
            String oldName = ((MLText) before.get(ContentModel.PROP_TITLE)).getDefaultValue();
            String newName = ((MLText) after.get(ContentModel.PROP_TITLE)).getDefaultValue();

            eventsService.nodeRenamed(nodeRef, oldName, newName);
        }
    }

    private void checkNodeUpdatedEventIncluded(NodeRef nodeRef, Map<QName, Serializable> before,
            Map<QName, Serializable> after)
    {
        if(includeEventType(NodeUpdatedEvent.EVENT_TYPE))
        {
            Map<String, Property> propertiesAdded = getAdds(before, after);
            Set<String> propertiesRemoved = getRemoves(before, after);
            Map<String, Property> propertiesChanged = getChanges(before, after);

            eventsService.nodeUpdated(nodeRef, propertiesAdded, propertiesRemoved, propertiesChanged, null, null);
        }
    }

    private void checkNamePropertyRenamed(NodeRef nodeRef, Map<QName, Serializable> before,
            Map<QName, Serializable> after)
    {
        if(propertyChanged(before, after, ContentModel.PROP_NAME))
        {
            String oldName = (String)before.get(ContentModel.PROP_NAME);
            String newName = (String)after.get(ContentModel.PROP_NAME);

            eventsService.nodeRenamed(nodeRef, oldName, newName);
        }
    }

    @Override
    public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        eventsService.nodeUpdated(nodeRef, null, null, null, Collections.singleton(aspectTypeQName.toPrefixString()), null);
    }

    @Override
    public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        eventsService.nodeMoved(oldChildAssocRef, newChildAssocRef);
    }

    @Override
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        eventsService.nodeDeleted(nodeRef);
    }

    @Override
    public void onContentPropertyUpdate(NodeRef nodeRef, QName propertyQName, ContentData beforeValue, ContentData afterValue)
    {
        boolean hasContentBefore = ContentData.hasContent(beforeValue) && beforeValue.getSize() > 0;
        boolean hasContentAfter = ContentData.hasContent(afterValue) && afterValue.getSize() > 0;
        
        // There are some shortcuts here
        if (!hasContentBefore && !hasContentAfter)
        {
            // Really, nothing happened
            return;
        }
        else if (EqualsHelper.nullSafeEquals(beforeValue, afterValue))
        {
            // Still, nothing happening
            return;
        }

        eventsService.contentWrite(nodeRef, propertyQName, afterValue);
    }

    @Override
    public void onCheckOut(NodeRef workingCopy)
    {
        // APPSREPO-194: We want the event for the original node, not the working copy,
        // so we perform eventsService.nodeCheckedOut(nodeRef) inside beforeCheckOut() instead
    }

    @Override
    public void onCancelCheckOut(NodeRef nodeRef)
    {
        eventsService.nodeCheckOutCancelled(nodeRef);
    }

    @Override
    public void onCheckIn(NodeRef nodeRef)
    {
        eventsService.nodeCheckedIn(nodeRef);
    }

    @Override
    public void beforeCheckOut(
            NodeRef nodeRef,
            NodeRef destinationParentNodeRef,
            QName destinationAssocTypeQName, 
            QName destinationAssocQName)
    {
        // APPSREPO-194: We want the event for the original node, not the working copy,
        // so we perform eventsService.nodeCheckedOut(nodeRef) here:
        eventsService.nodeCheckedOut(nodeRef);
    }

    public void onAuthorityRemovedFromGroup(String parentGroup, String childAuthority)
    {
        eventsService.authorityRemovedFromGroup(parentGroup, childAuthority);
    }

    public void onAuthorityAddedToGroup(String parentGroup, String childAuthority)
    {
        eventsService.authorityAddedToGroup(parentGroup, childAuthority);
    }

    public void onInheritPermissionsEnabled(NodeRef nodeRef)
    {
        eventsService.inheritPermissionsEnabled(nodeRef);
    }

    public void onInheritPermissionsDisabled(NodeRef nodeRef, boolean async)
    {
        eventsService.inheritPermissionsDisabled(nodeRef, async);
    }

    public void onRevokeLocalPermission(NodeRef nodeRef, String authority, String permission)
    {
        eventsService.revokeLocalPermissions(nodeRef, authority, permission);
    }

    public void onGrantLocalPermission(NodeRef nodeRef, String authority, String permission)
    {
        eventsService.grantLocalPermission(nodeRef, authority, permission);
    }

    public void onGroupDeleted(String groupName, boolean cascade)
    {
        eventsService.groupDeleted(groupName, cascade);
    } 

    @Override
    public void onCreateChildAssociation(ChildAssociationRef newChildAssocRef, boolean isNewNode)
    {
        if (!newChildAssocRef.isPrimary())
        {
            eventsService.secondaryAssociationCreated(newChildAssocRef);
        }
    }

    @Override
    public void onDeleteChildAssociation(ChildAssociationRef childAssocRef)
    {
        if (!childAssocRef.isPrimary())
        {
            eventsService.secondaryAssociationDeleted(childAssocRef);
        }
    }
    
    /**
     * Called after a new content node's security marking has been updated.
     * 
     * @param nodeRef  reference to the updated node
     * @param wasMarkedBefore - indicated if before update the content was marked
     * @param isMarkedAfter - indicated if after update the content is marked
     */
    public void onUpdateContentSecurityMarks(NodeRef nodeRef, boolean wasMarkedBefore, boolean isMarkedAfter)
    {
        if(!wasMarkedBefore && isMarkedAfter && includeEventType(FileClassifiedEvent.EVENT_TYPE))
        {
            eventsService.fileClassified(nodeRef);
        }
        
        if(wasMarkedBefore && !isMarkedAfter && includeEventType(FileUnclassifiedEvent.EVENT_TYPE))
        {
            eventsService.fileUnclassified(nodeRef);
        }
    }
    
    /**
     * Called after a file has been declared as a record
     * 
     * @param nodeRef the file being declared as a record
     */
    public void onRecordDeclaration(NodeRef nodeRef)
    {
        eventsService.recordCreated(nodeRef);
    }
    
    /**
     * Called before a record is rejected
     * 
     * @param nodeRef the record about to be rejected
     */
    public void beforeRecordRejection(NodeRef nodeRef)
    {
        eventsService.recordRejected(nodeRef);
    }
}

/*
 * Copyright 2014 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.repo.events;

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
import org.alfresco.model.ContentModel;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.BehaviourDefinition;
import org.alfresco.repo.policy.ClassBehaviourBinding;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.EqualsHelper;

import static org.alfresco.repo.site.SiteModel.TYPE_SITE;

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
		NodeServicePolicies.OnRemoveAspectPolicy,
		NodeServicePolicies.OnUpdatePropertiesPolicy,
		NodeServicePolicies.OnMoveNodePolicy,
		CheckOutCheckInServicePolicies.BeforeCheckOut,
		CheckOutCheckInServicePolicies.OnCheckOut,
		CheckOutCheckInServicePolicies.OnCheckIn,
		CheckOutCheckInServicePolicies.OnCancelCheckOut
//		NodeServicePolicies.OnUpdateNodePolicy
{
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
		if(includeEventType(NodeContentPutEvent.EVENT_TYPE))
		{
			BehaviourDefinition<ClassBehaviourBinding> binding =
					this.policyComponent.bindClassBehaviour(
							ContentServicePolicies.OnContentPropertyUpdatePolicy.QNAME, 
							ContentModel.TYPE_BASE,
							new JavaBehaviour(this, "onContentPropertyUpdate"));
			addBehaviour(binding);
		}

		if(includeEventType(NodeContentGetEvent.EVENT_TYPE))
		{
			BehaviourDefinition<ClassBehaviourBinding> binding =
					this.policyComponent.bindClassBehaviour(
							ContentServicePolicies.OnContentReadPolicy.QNAME,
							ContentModel.TYPE_BASE,
							new JavaBehaviour(this, "onContentRead"));
			addBehaviour(binding);
		}

		if(includeEventType(NodeAddedEvent.EVENT_TYPE))
		{
			BehaviourDefinition<ClassBehaviourBinding> binding =
					this.policyComponent.bindClassBehaviour(
							NodeServicePolicies.OnCreateNodePolicy.QNAME,
							ContentModel.TYPE_BASE,
							new JavaBehaviour(this, "onCreateNode"));
			addBehaviour(binding);
		}

		if(includeEventType(NodeRemovedEvent.EVENT_TYPE))
		{
			// on before delete so that we have the relevant node details available
			BehaviourDefinition<ClassBehaviourBinding> binding =
					this.policyComponent.bindClassBehaviour(
							NodeServicePolicies.BeforeDeleteNodePolicy.QNAME,
							ContentModel.TYPE_BASE,
							new JavaBehaviour(this, "beforeDeleteNode"));
			addBehaviour(binding);
		}

		if(includeEventType(NodeMovedEvent.EVENT_TYPE))
		{
			BehaviourDefinition<ClassBehaviourBinding> binding =
					this.policyComponent.bindClassBehaviour(
							NodeServicePolicies.OnMoveNodePolicy.QNAME,
							ContentModel.TYPE_BASE,
							new JavaBehaviour(this, "onMoveNode"));
			addBehaviour(binding);
		}

		if(includeEventType(NodeCheckedOutEvent.EVENT_TYPE))
		{
			BehaviourDefinition<ClassBehaviourBinding> binding =
					this.policyComponent.bindClassBehaviour(
							CheckOutCheckInServicePolicies.BeforeCheckOut.QNAME,
							ContentModel.TYPE_BASE,
							new JavaBehaviour(this, "beforeCheckOut"));
			addBehaviour(binding);

			binding =
					this.policyComponent.bindClassBehaviour(
							CheckOutCheckInServicePolicies.OnCheckOut.QNAME,
							ContentModel.TYPE_BASE,
							new JavaBehaviour(this, "onCheckOut"));
			addBehaviour(binding);
		}

		if(includeEventType(NodeCheckedInEvent.EVENT_TYPE))
		{
			BehaviourDefinition<ClassBehaviourBinding> binding =
					this.policyComponent.bindClassBehaviour(
							CheckOutCheckInServicePolicies.OnCheckIn.QNAME,
							ContentModel.TYPE_BASE,
							new JavaBehaviour(this, "onCheckIn"));
			addBehaviour(binding);
		}

		if(includeEventType(NodeCheckOutCancelledEvent.EVENT_TYPE))
		{
			BehaviourDefinition<ClassBehaviourBinding> binding =
					this.policyComponent.bindClassBehaviour(
							CheckOutCheckInServicePolicies.OnCancelCheckOut.QNAME,
							ContentModel.TYPE_BASE,
							new JavaBehaviour(this, "onCancelCheckOut"));
			addBehaviour(binding);
		}

		BehaviourDefinition<ClassBehaviourBinding> binding =
				this.policyComponent.bindClassBehaviour(
						NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
						ContentModel.TYPE_BASE,
						new JavaBehaviour(this, "onUpdateProperties"));
		addBehaviour(binding);
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
	public void onRemoveAspect(NodeRef nodeRef, QName aspectTypeQName)
	{
		eventsService.nodeUpdated(nodeRef, null, null, null, null, Collections.singleton(aspectTypeQName.toPrefixString()));
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
		eventsService.nodeCheckedOut(workingCopy);
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
	}

//	@Override
//	public void onUpdateNode(NodeRef nodeRef)
//	{
//		if(includeEventType(NodeUpdatedEvent.EVENT_TYPE))
//		{
//			eventsService.nodeUpdated(nodeRef, null, null, null, Collections.singleton(aspectTypeQName.toPrefixString()), null);
//			Map<String, Property> propertiesAdded = getAdds(before, after);
//			Set<String> propertiesRemoved = getRemoves(before, after);
//			Map<String, Property> propertiesChanged = getChanges(before, after);
//			eventsService.nodeUpdated(nodeRef, propertiesAdded, propertiesRemoved, propertiesChanged, null, null);
//		}
//	}
}

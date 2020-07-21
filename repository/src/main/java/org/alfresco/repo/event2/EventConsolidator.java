/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2020 Alfresco Software Limited
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
package org.alfresco.repo.event2;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.event.v1.model.ContentInfo;
import org.alfresco.repo.event.v1.model.DataAttributes;
import org.alfresco.repo.event.v1.model.EventData;
import org.alfresco.repo.event.v1.model.NodeResource;
import org.alfresco.repo.event.v1.model.NodeResource.Builder;
import org.alfresco.repo.event.v1.model.RepoEvent;
import org.alfresco.repo.event.v1.model.UserInfo;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * Encapsulates events occurred in a single transaction.
 *
 * @author Jamal Kaabi-Mofrad
 */
public class EventConsolidator implements EventSupportedPolicies
{
    private final NodeResourceHelper helper;
    private final Deque<EventType> eventTypes;
    private final List<QName> aspectsAdded;
    private final List<QName> aspectsRemoved;

    protected NodeRef nodeRef;

    private NodeResource.Builder resourceBuilder;
    private Map<QName, Serializable> propertiesBefore;
    private Map<QName, Serializable> propertiesAfter;
    private QName nodeType;
    private QName nodeTypeBefore;
    private List<String> primaryHierarchyBefore;
    private boolean resourceBeforeAllFieldsNull = true;

    public EventConsolidator(NodeResourceHelper nodeResourceHelper)
    {
        this.helper = nodeResourceHelper;
        this.eventTypes = new ArrayDeque<>();
        this.aspectsAdded = new ArrayList<>();
        this.aspectsRemoved = new ArrayList<>();
    }

    /**
     * Builds and returns the {@link RepoEvent} instance.
     *
     * @param eventInfo the object holding the event information
     * @return the {@link RepoEvent} instance
     */
    public RepoEvent<DataAttributes<NodeResource>> getRepoEvent(EventInfo eventInfo)
    {
        NodeResource resource = buildNodeResource();
        EventType eventType = getDerivedEvent();

        DataAttributes<NodeResource> eventData = buildEventData(eventInfo, resource, eventType);

        return RepoEvent.<DataAttributes<NodeResource>>builder()
                    .setId(eventInfo.getId())
                    .setSource(eventInfo.getSource())
                    .setTime(eventInfo.getTimestamp())
                    .setType(eventType.getType())
                    .setData(eventData)
                    .setDataschema(EventJSONSchema.getSchemaV1(eventType))
                    .build();
    }

    protected DataAttributes<NodeResource> buildEventData(EventInfo eventInfo, NodeResource resource, EventType eventType)
    {
        EventData.Builder<NodeResource> eventDataBuilder = EventData.<NodeResource>builder()
                    .setEventGroupId(eventInfo.getTxnId())
                    .setResource(resource);

        if (eventType == EventType.NODE_UPDATED)
        {
            eventDataBuilder.setResourceBefore(buildNodeResourceBeforeDelta(resource));
        }

        return eventDataBuilder.build();
    }

    /**
     * Creates a builder instance if absent or {@code forceUpdate} is requested.
     * It also, sets the required fields.
     *
     * @param nodeRef     the nodeRef in the txn
     * @param forceUpdate if {@code true}, will get the latest node info and ignores
     *                    the existing builder object.
     */
    private void createBuilderIfAbsent(NodeRef nodeRef, boolean forceUpdate)
    {
        if (resourceBuilder == null || forceUpdate)
        {
            this.resourceBuilder = helper.createNodeResourceBuilder(nodeRef);
            this.nodeRef = nodeRef;
            this.nodeType = helper.getNodeType(nodeRef);
        }
    }

    /**
     * Creates a builder instance if absent, and sets the required fields.
     *
     * @param nodeRef the nodeRef in the txn
     */
    private void createBuilderIfAbsent(NodeRef nodeRef)
    {
        createBuilderIfAbsent(nodeRef, false);
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        eventTypes.add(EventType.NODE_CREATED);

        NodeRef nodeRef = childAssocRef.getChildRef();
        createBuilderIfAbsent(nodeRef);

        // Sometimes onCreateNode policy is out of order
        this.propertiesBefore = null;
        setBeforeProperties(Collections.emptyMap());
        setAfterProperties(helper.getProperties(nodeRef));
    }

    @Override
    public void onMoveNode(ChildAssociationRef oldChildAssocRef, ChildAssociationRef newChildAssocRef)
    {
        eventTypes.add(EventType.NODE_UPDATED);

        createBuilderIfAbsent(newChildAssocRef.getChildRef());
        setBeforePrimaryHierarchy(helper.getPrimaryHierarchy(oldChildAssocRef.getParentRef(), true));
    }

    @Override
    public void onSetNodeType(NodeRef nodeRef, QName before, QName after)
    {
        eventTypes.add(EventType.NODE_UPDATED);
        nodeTypeBefore = before;
        createBuilderIfAbsent(nodeRef);
    }

    @Override
    public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after)
    {
        eventTypes.add(EventType.NODE_UPDATED);

        // Sometime we don't get the 'before', so just use the latest
        if (before.isEmpty() && this.propertiesAfter != null)
        {
            before = this.propertiesAfter;
        }
        createBuilderIfAbsent(nodeRef);
        setBeforeProperties(before);
        setAfterProperties(after);
    }

    @Override
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        eventTypes.add(EventType.NODE_DELETED);
        createBuilderIfAbsent(nodeRef, false);
    }

    @Override
    public void onAddAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        eventTypes.add(EventType.NODE_UPDATED);
        addAspect(aspectTypeQName);
        createBuilderIfAbsent(nodeRef);
    }

    void addAspect(QName aspectTypeQName)
    {
        if (aspectsRemoved.contains(aspectTypeQName))
        {
            aspectsRemoved.remove(aspectTypeQName);
        }
        else
        {
            aspectsAdded.add(aspectTypeQName);
        }
    }

    @Override
    public void onRemoveAspect(NodeRef nodeRef, QName aspectTypeQName)
    {
        eventTypes.add(EventType.NODE_UPDATED);
        removeAspect(aspectTypeQName);
        createBuilderIfAbsent(nodeRef);
    }

    void removeAspect(QName aspectTypeQName)
    {
        if (aspectsAdded.contains(aspectTypeQName))
        {
            aspectsAdded.remove(aspectTypeQName);
        }
        else
        {
            aspectsRemoved.add(aspectTypeQName);
        }
    }

    private void setAfterProperties(Map<QName, Serializable> after)
    {
        propertiesAfter = after;
    }

    private void setBeforeProperties(Map<QName, Serializable> before)
    {
        // Don't overwrite the original value if there are multiple calls.
        if (propertiesBefore == null)
        {
            propertiesBefore = before;
        }
    }

    private void setBeforePrimaryHierarchy(List<String> before)
    {
        // Don't overwrite the original value if there are multiple calls.
        if (primaryHierarchyBefore == null)
        {
            primaryHierarchyBefore = before;
        }
    }

    private NodeResource buildNodeResource()
    {
        if (resourceBuilder == null)
        {
            return null;
        }

        if (eventTypes.getLast() != EventType.NODE_DELETED)
        {
            // Check the node still exists.
            // This could happen in tests where a node is deleted before the afterCommit code is
            // executed (For example, see ThumbnailServiceImplTest#testIfNodesExistsAfterCreateThumbnail).
            if (helper.nodeExists(nodeRef))
            {
                // We are setting the details at the end of the Txn by getting the latest info
                createBuilderIfAbsent(nodeRef, true);
            }
        }
        // Now create an instance of NodeResource
        return resourceBuilder.build();
    }

    protected NodeResource buildNodeResourceBeforeDelta(NodeResource after)
    {
        if (after == null)
        {
            return null;
        }

        Builder builder = NodeResource.builder();

        Map<QName, Serializable> changedPropsBefore = getBeforeMapChanges(propertiesBefore, propertiesAfter);
        if (!changedPropsBefore.isEmpty())
        {
            // Set only the changed properties
            Map<String, Serializable> mappedProps = helper.mapToNodeProperties(changedPropsBefore);
            if (!mappedProps.isEmpty())
            {
                builder.setProperties(mappedProps);
                resourceBeforeAllFieldsNull = false;
            }
            String name = (String) changedPropsBefore.get(ContentModel.PROP_NAME);
            if (name != null)
            {
                builder.setName(name);
                resourceBeforeAllFieldsNull = false;
            }
            ContentInfo contentInfo = helper.getContentInfo(changedPropsBefore);
            if (contentInfo != null)
            {
                builder.setContent(contentInfo);
                resourceBeforeAllFieldsNull = false;
            }

            UserInfo modifier = helper.getUserInfo((String) changedPropsBefore.get(ContentModel.PROP_MODIFIER));
            if (modifier != null)
            {
                builder.setModifiedByUser(modifier);
                resourceBeforeAllFieldsNull = false;
            }
            ZonedDateTime modifiedAt =
                        helper.getZonedDateTime((Date) changedPropsBefore.get(ContentModel.PROP_MODIFIED));
            if (modifiedAt != null)
            {
                builder.setModifiedAt(modifiedAt);
                resourceBeforeAllFieldsNull = false;
            }
        }

        Set<String> aspectsBefore = getMappedAspectsBefore(after.getAspectNames());
        if (!aspectsBefore.isEmpty())
        {
            builder.setAspectNames(aspectsBefore);
            resourceBeforeAllFieldsNull = false;
        }

        if (primaryHierarchyBefore != null && !primaryHierarchyBefore.isEmpty())
        {
            builder.setPrimaryHierarchy(primaryHierarchyBefore);
            resourceBeforeAllFieldsNull = false;
        }

        if (nodeTypeBefore != null)
        {
            builder.setNodeType(helper.getQNamePrefixString(nodeTypeBefore));
            resourceBeforeAllFieldsNull = false;
        }

        return builder.build();
    }

    Set<String> getMappedAspectsBefore(Set<String> currentAspects)
    {
        if (currentAspects == null)
        {
            currentAspects = Collections.emptySet();
        }
        if (hasChangedAspect())
        {
            Set<String> removed = helper.mapToNodeAspects(aspectsRemoved);
            Set<String> added = helper.mapToNodeAspects(aspectsAdded);

            Set<String> before = new HashSet<>();
            if (!removed.isEmpty() || !added.isEmpty())
            {
                before = new HashSet<>(currentAspects);
                if (!removed.isEmpty())
                {
                    // Add all the removed aspects from the current list
                    before.addAll(removed);
                }
                if (!added.isEmpty())
                {
                    // Remove all the added aspects from the current list
                    before.removeAll(added);
                }
            }
            return before;
        }
        return Collections.emptySet();
    }

    private boolean hasChangedAspect()
    {
        if ((aspectsRemoved.isEmpty() && aspectsAdded.isEmpty()) ||
                org.apache.commons.collections.CollectionUtils.isEqualCollection(aspectsAdded, aspectsRemoved))
        {
            return false;
        }
        return true;
    }

    private <K, V> Map<K, V> getBeforeMapChanges(Map<K, V> before, Map<K, V> after)
    {
        if (before == null)
        {
            return Collections.emptyMap();
        }
        if (after == null)
        {
            after = Collections.emptyMap();
        }
        // Get before values that changed
        Map<K, V> beforeDelta = new HashMap<>(before);
        beforeDelta.entrySet().removeAll(after.entrySet());

        return beforeDelta;
    }

    /**
     * @return a derived event for a transaction.
     */
    private EventType getDerivedEvent()
    {
        if (isTemporaryNode())
        {
            // This event will be filtered out, but we set the correct
            // event type anyway for debugging purposes
            return EventType.NODE_DELETED;
        }
        else if (eventTypes.contains(EventType.NODE_CREATED))
        {
            return EventType.NODE_CREATED;
        }
        else if (eventTypes.getLast() == EventType.NODE_DELETED)
        {
            return EventType.NODE_DELETED;
        }
        else
        {
            // Default to first event
            return eventTypes.getFirst();
        }
    }

    /**
     * Whether or not the node has been created and then deleted, i.e. a temporary node.
     *
     * @return {@code true} if the node has been created and then deleted, otherwise false
     */
    public boolean isTemporaryNode()
    {
        return eventTypes.contains(EventType.NODE_CREATED) && eventTypes.getLast() == EventType.NODE_DELETED;
    }

    public QName getNodeType()
    {
        return nodeType;
    }

    public Deque<EventType> getEventTypes()
    {
        return eventTypes;
    }


    public List<QName> getAspectsAdded()
    {
        return aspectsAdded;
    }

    public List<QName> getAspectsRemoved()
    {
        return aspectsRemoved;
    }

    public boolean isResourceBeforeAllFieldsNull()
    {
        return resourceBeforeAllFieldsNull;
    }
}

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
package org.alfresco.repo.audit.access;

import static org.alfresco.repo.audit.model.AuditApplication.AUDIT_PATH_SEPARATOR;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.alfresco.repo.audit.model.AuditApplication;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies.OnCancelCheckOut;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies.OnCheckIn;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies.OnCheckOut;
import org.alfresco.repo.content.ContentServicePolicies.OnContentReadPolicy;
import org.alfresco.repo.content.ContentServicePolicies.OnContentUpdatePolicy;
import org.alfresco.repo.copy.CopyServicePolicies.OnCopyCompletePolicy;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnAddAspectPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnMoveNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnRemoveAspectPolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy;
import org.alfresco.repo.policy.PolicyScope;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.version.VersionServicePolicies.OnCreateVersionPolicy;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

/**
 * Changes made to a {@code Node} in a single transaction. For example the creation of
 * a Node also involves updating properties, but the main action remains create node.
 * 
 * @author Alan Davis
 */
/*package*/ class NodeChange implements

        BeforeDeleteNodePolicy, OnAddAspectPolicy, OnCreateNodePolicy, OnMoveNodePolicy,
        OnRemoveAspectPolicy, OnUpdatePropertiesPolicy,

        OnContentReadPolicy, OnContentUpdatePolicy,

        OnCreateVersionPolicy,

        OnCopyCompletePolicy,
        
        OnCheckOut, OnCheckIn, OnCancelCheckOut
{
    private static final String USER = "user";
    private static final String ACTION = "action";
    private static final String SUB_ACTIONS = "sub-actions";
    private static final String NODE = "node";
    private static final String PATH = "path";
    private static final String TYPE = "type";
    private static final String FROM = "from";
    private static final String TO = "to";
    private static final String ADD = "add";
    private static final String DELETE = "delete";

    private static final String COPY = "copy";
    private static final String MOVE = "move";
    private static final String PROPERTIES = "properties";
    private static final String ASPECTS = "aspects";
    private static final String VERSION_PROPERTIES = "version-properties";
    private static final String SUB_ACTION = "sub-action";

    private static final String DELETE_NODE = "deleteNode";
    private static final String CREATE_NODE = "createNode";
    private static final String MOVE_NODE = "moveNode";
    private static final String UPDATE_NODE_PROPERTIES = "updateNodeProperties";
    private static final String DELETE_NODE_ASPECT = "deleteNodeAspect";
    private static final String ADD_NODE_ASPECT = "addNodeAspect";
    private static final String CREATE_CONTENT = "createContent";
    private static final String UPDATE_CONTENT = "updateContent";
    private static final String READ_CONTENT = "readContent";
    private static final String CREATE_VERSION = "createVersion";
    private static final String COPY_NODE = "copyNode";
    private static final String CHECK_IN = "checkIn";
    private static final String CHECK_OUT = "checkOut";
    private static final String CANCEL_CHECK_OUT = "cancelCheckOut";

    private static final String INVALID_PATH_CHAR_REPLACEMENT = "-";
    private static final Pattern INVALID_PATH_COMP_CHAR_PATTERN =
        Pattern.compile(AuditApplication.AUDIT_INVALID_PATH_COMP_CHAR_REGEX);
    
    private final NodeInfoFactory nodeInfoFactory;
    private final NamespaceService namespaceService;
    private final NodeInfo nodeInfo;

    private String action;
    
    private boolean auditSubActions = false;
    private Set<String> subActions = new LinkedHashSet<String>();
    private List<Map<String, Serializable>> subActionAuditMaps;
    
    private NodeInfo copyFrom;
    
    private NodeInfo moveFrom;
    private NodeInfo moveTo;

    private Map<QName, Serializable> fromProperties;
    private Map<QName, Serializable> toProperties;
    
    private HashSet<QName> addedAspects;
    private HashSet<QName> deletedAspects;
    
    private HashMap<String, Serializable> versionProperties;
    
    /*package*/ NodeChange(NodeInfoFactory nodeInfoFactory, NamespaceService namespaceService, NodeRef nodeRef)
    {
        this.nodeInfoFactory = nodeInfoFactory;
        this.nodeInfo = nodeInfoFactory.newNodeInfo(nodeRef);
        this.namespaceService = namespaceService;
    }
    
    /**
     * @return a derived action for a transaction based on the sub-actions that have taken place.
     */
    public String getDerivedAction()
    {
        // Derive higher level action
        String action;
        if (subActions.contains(CHECK_OUT))
        {
            action = "CHECK OUT";
        }
        else if (subActions.contains(CHECK_IN))
        {
            action = "CHECK IN";
        }
        else if (subActions.contains(CANCEL_CHECK_OUT))
        {
            action = "CANCEL CHECK OUT";
        }
        else if (subActions.contains(COPY_NODE))
        {
            action = "COPY";
        }
        else if (subActions.contains(CREATE_NODE))
        {
            action = "CREATE";
        }
        else if (subActions.size() == 1 && subActions.contains(READ_CONTENT))
        {
            // Reads in combinations with other actions tend to only facilitate the other action.
            action = "READ";
        }
        else if (subActions.contains(DELETE_NODE))
        {
            action = "DELETE";
        }
        else if (subActions.contains(CREATE_VERSION)) // && !subActions.contains(CREATE_NODE)
        {
            action = "CREATE VERSION";
        }
        else if (subActions.contains(UPDATE_CONTENT)) // && !subActions.contains(CREATE_NODE)
        {
            action = "UPDATE CONTENT";
        }
        else if (subActions.contains(MOVE_NODE))
        {
            action = "MOVE";
        }
        else
        {
            // Default to first sub-action
            action = this.action;
        }
            
        return action;
    }
    
    /**
     * @return {@code true} if the node has been created and then deleted.
     */
    public boolean isTemporaryNode()
    {
        // No need to check the order as a new node would be given a ned node ref.
        return subActions.contains(CREATE_NODE) && subActions.contains(DELETE_NODE);
    }

    private NodeChange setAction(String action)
    {
        this.action = action;
        return this;
    }

    private void appendSubAction(NodeChange subNodeChange)
    {
        // Remember sub-actions so we can check them later to derive the high level action
        subActions.add(subNodeChange.action);
        
        // Default the action to the first sub-action;
        if (action == null)
        {
            action = subNodeChange.action;
        }
        
        // Audit sub actions if required. 
        if (auditSubActions)
        {
            if (subActionAuditMaps == null)
            {
                subActionAuditMaps = new ArrayList<Map<String, Serializable>>();
            }
            subActionAuditMaps.add(subNodeChange.getAuditData(true));
        }
    }

    public NodeChange setAuditSubActions(boolean auditSubActions)
    {
        this.auditSubActions = auditSubActions;
        return this;
    }

    private NodeChange setCopyFrom(NodeRef copyFrom)
    {
        this.copyFrom = nodeInfoFactory.newNodeInfo(copyFrom);
        return this;
    }

    private NodeChange setMoveFrom(ChildAssociationRef childAssocRef)
    {
        // Don't overwrite original value if multiple calls.
        if (this.moveFrom == null)
        {
            this.moveFrom = nodeInfoFactory.newNodeInfo(childAssocRef);
        }
        return this;
    }

    private NodeChange setMoveTo(ChildAssociationRef childAssocRef)
    {
        this.moveTo = nodeInfoFactory.newNodeInfo(childAssocRef);
        
        // Clear values if we are back to where we started.
        if (this.moveTo.equals(moveFrom))
        {
            this.moveTo = null;
            moveFrom = null;
        }
        return this;
    }
    
    private NodeChange setFromProperties(Map<QName, Serializable> fromProperties)
    {
        // Don't overwrite original value if multiple calls.
        if (this.fromProperties == null)
        {
            this.fromProperties = fromProperties;
        }
        return this;
    }

    private NodeChange setToProperties(Map<QName, Serializable> toProperties)
    {
        this.toProperties = toProperties;
        return this;
    }
    
    /**
     * Add an aspect - if just deleted, remove the delete, otherwise record the add. 
     */
    private NodeChange addAspect(QName aspect)
    {
        if (addedAspects == null)
        {
            addedAspects = new HashSet<QName>();
        }

        // Consider sequences
        //   add           = add
        //   del add       = ---
        //   add del add   = add
        //   add add       = add
        if (deletedAspects != null && deletedAspects.contains(aspect))
        {
            deletedAspects.remove(aspect);
        }
        else
        {
            addedAspects.add(aspect);
        }
        
        return this;
    }
    
    /**
     * Delete an aspect - if just added, remove the add, otherwise record the delete.
     */
    private NodeChange deleteAspect(QName aspect)
    {
        if (deletedAspects == null)
        {
            deletedAspects = new HashSet<QName>();
        }

        // Consider sequences
        //   del           = del
        //   add del       = ---
        //   del add del   = del
        //   del del       = del
        if (addedAspects != null && addedAspects.contains(aspect))
        {
            addedAspects.remove(aspect);
        }
        else
        {
            deletedAspects.add(aspect);
        }
        
        return this;
    }
    
    private NodeChange setVersionProperties(
            HashMap<String, Serializable> versionProperties)
    {
        if (this.versionProperties == null)
        {
            this.versionProperties = new HashMap<String, Serializable>();
        }
        this.versionProperties.putAll(versionProperties);
        return this;
    }
    
    @Override
    public void beforeDeleteNode(NodeRef nodeRef)
    {
        appendSubAction(new NodeChange(nodeInfoFactory, namespaceService, nodeRef).
                setAction(DELETE_NODE));
    }

    @Override
    public void onCreateNode(ChildAssociationRef childAssocRef)
    {
        appendSubAction(new NodeChange(nodeInfoFactory, namespaceService, childAssocRef.getChildRef()).
                setAction(CREATE_NODE));
    }

    @Override
    public void onMoveNode(ChildAssociationRef fromChildAssocRef, ChildAssociationRef toChildAssocRef)
    {
        setMoveFrom(fromChildAssocRef);
        setMoveTo(toChildAssocRef);
        
        // Note: A change of the child node name will be picked up as a property name change.
        
        appendSubAction(new NodeChange(nodeInfoFactory, namespaceService, toChildAssocRef.getChildRef()).
                setAction(MOVE_NODE).
                setMoveFrom(fromChildAssocRef).
                setMoveTo(toChildAssocRef));
    }  

    @Override
    public void onUpdateProperties(NodeRef nodeRef,
            Map<QName, Serializable> fromProperties, Map<QName, Serializable> toProperties)
    {
        setFromProperties(fromProperties);
        setToProperties(toProperties);
        
        appendSubAction(new NodeChange(nodeInfoFactory, namespaceService, nodeRef).
                setAction(UPDATE_NODE_PROPERTIES).
                setFromProperties(fromProperties).
                setToProperties(toProperties));
    }

    @Override
    public void onRemoveAspect(NodeRef nodeRef, QName aspect)
    {
        deleteAspect(aspect);
        
        appendSubAction(new NodeChange(nodeInfoFactory, namespaceService, nodeRef).
                setAction(DELETE_NODE_ASPECT).
                deleteAspect(aspect));
    }

    @Override
    public void onAddAspect(NodeRef nodeRef, QName aspect)
    {
        addAspect(aspect);
        
        appendSubAction(new NodeChange(nodeInfoFactory, namespaceService, nodeRef).
                setAction(ADD_NODE_ASPECT).
                addAspect(aspect));
    }
    
    @Override
    public void onContentUpdate(NodeRef nodeRef, boolean newContent)
    {
        if (newContent)
        {
            appendSubAction(new NodeChange(nodeInfoFactory, namespaceService, nodeRef).
                    setAction(CREATE_CONTENT));
        }
        else
        {
            appendSubAction(new NodeChange(nodeInfoFactory, namespaceService, nodeRef).
                    setAction(UPDATE_CONTENT));
        }
    }

    @Override
    public void onContentRead(NodeRef nodeRef)
    {
        appendSubAction(new NodeChange(nodeInfoFactory, namespaceService, nodeRef).
                setAction(READ_CONTENT));
    }

    @Override
    public void onCreateVersion(QName classRef, NodeRef nodeRef,
            Map<String, Serializable> versionProperties, PolicyScope nodeDetails)
    {
        setVersionProperties((HashMap<String, Serializable>)versionProperties);
        
        appendSubAction(new NodeChange(nodeInfoFactory, namespaceService, nodeRef).
                setAction(CREATE_VERSION).
                setVersionProperties((HashMap<String, Serializable>)versionProperties));
        // Note nodeDetails are not used
    }
    
    public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, NodeRef targetNodeRef,
            boolean copyToNewNode, Map<NodeRef, NodeRef> copyMap)
    {
        setCopyFrom(sourceNodeRef);
        
        appendSubAction(new NodeChange(nodeInfoFactory, namespaceService, targetNodeRef).
                setAction(COPY_NODE).
                setCopyFrom(sourceNodeRef));
    }
    
    public void onCheckOut(NodeRef workingCopy)
    {
        appendSubAction(new NodeChange(nodeInfoFactory, namespaceService, workingCopy).
                setAction(CHECK_OUT));
    }
    
    public void onCheckIn(NodeRef nodeRef)
    {
        appendSubAction(new NodeChange(nodeInfoFactory, namespaceService, nodeRef).
                setAction(CHECK_IN));
    }
    
    public void onCancelCheckOut(NodeRef nodeRef)
    {
        appendSubAction(new NodeChange(nodeInfoFactory, namespaceService, nodeRef).
                setAction(CANCEL_CHECK_OUT));
    }
    
    public Map<String, Serializable> getAuditData(boolean subAction)
    {
        Map<String, Serializable> auditMap = new HashMap<String, Serializable>(
                2 *
                (1 +               // action
                 1 +               // user
                 1 +               // sub actions
                 3 +               // node, path, type
                 3 +               // copy source's node, path, type 
                 3 +               // move source's node, path, type
                 (fromProperties != null ? fromProperties.size() + toProperties.size() + 4 : 0) +
                                   // individual property changes
                                   // grouped from, to, add and delete changes
                 (addedAspects != null ? addedAspects.size() : 0) +
                 (deletedAspects != null ? deletedAspects.size() : 0) +
                 (versionProperties != null ? versionProperties.size()+1 : 0) +
                 getSubAuditDataSize()));

        // For a transaction, set the action
        if (!subAction)
        {
            setAction(getDerivedAction());
        }
        auditMap.put(ACTION, action);
        
        if (!subAction) // no need to repeat for sub actions
        {
            auditMap.put(USER, AuthenticationUtil.getFullyAuthenticatedUser());
            addSubActionsToAuditMap(auditMap);
        
            auditMap.put(NODE, nodeInfo.getNodeRef());
            auditMap.put(PATH, nodeInfo.getPrefixPath());
            auditMap.put(TYPE, nodeInfo.getPrefixType());
        }
        
        if (copyFrom != null)
        {
            auditMap.put(buildPath(COPY, FROM, NODE), copyFrom.getNodeRef());
            auditMap.put(buildPath(COPY, FROM, PATH), copyFrom.getPrefixPath());
            auditMap.put(buildPath(COPY, FROM, TYPE), copyFrom.getPrefixType());
        }
        
        if (moveFrom != null)
        {
            auditMap.put(buildPath(MOVE, FROM, NODE), moveFrom.getNodeRef());
            auditMap.put(buildPath(MOVE, FROM, PATH), moveFrom.getPrefixPath());
            auditMap.put(buildPath(MOVE, FROM, TYPE), moveFrom.getPrefixType());
        }
        
        if (fromProperties != null)
        {
            addPropertyChangesToAuditMap(auditMap, subAction);
        }
        
        if (addedAspects != null && !addedAspects.isEmpty())
        {
            addAspectChangesToAuditMap(auditMap, ADD, addedAspects, subAction);
        }
        
        if (deletedAspects != null && !deletedAspects.isEmpty())
        {
            addAspectChangesToAuditMap(auditMap, DELETE, deletedAspects, subAction);
        }
        
        if (versionProperties != null && !versionProperties.isEmpty())
        {
            addVersionPropertiesToAuditMap(auditMap, versionProperties, subAction);
        }
        
        addSubActionAuditMapsToAuditMap(auditMap);
        
        return auditMap;
    }
    
    private void addSubActionsToAuditMap(Map<String, Serializable> auditMap)
    {
        StringBuilder sb = new StringBuilder();
        for (String subAction: subActions)
        {
            if (sb.length() > 0)
            {
                sb.append(' ');
            }
            sb.append(subAction);                
        }

        auditMap.put(SUB_ACTIONS, sb.toString());
    }

    private void addPropertyChangesToAuditMap(Map<String, Serializable> auditMap, boolean subAction)
    {
        HashMap<QName, Serializable> from = new HashMap<QName, Serializable>(fromProperties.size());
        HashMap<QName, Serializable> to = new HashMap<QName, Serializable>(toProperties.size());
        HashMap<QName, Serializable> add = new HashMap<QName, Serializable>(toProperties.size());
        HashMap<QName, Serializable> delete = new HashMap<QName, Serializable>(fromProperties.size());
        
        // Initially check for changes to existing keys and values.
        // Record individual value changes and group (from, to, delete) changes in their own maps.
        for (Map.Entry<QName, Serializable> entry : fromProperties.entrySet())
        {
            // Audit QNames with the prefix set. The key can be used in original Set and
            // Map operations as only the namesapace and local name are used in equals and
            // hashcode methods.
            QName key = entry.getKey().getPrefixedQName(namespaceService);
            
            String name = replaceInvalidPathChars(key.toPrefixString());
            Serializable beforeValue = entry.getValue();
            Serializable afterValue = null;
            
            boolean exists = toProperties.containsKey(key); 
            boolean same = false;
            if (exists)
            {
                // Audit nothing if both values are null or equal. 
                afterValue = toProperties.get(key);
                if ((beforeValue == afterValue) ||
                    (beforeValue != null && beforeValue.equals(afterValue)))
                    same = true;
            }
            
            if (!same)
            {
                if (exists)
                {
                    auditMap.put(buildPath(PROPERTIES, FROM, name), beforeValue);
                    auditMap.put(buildPath(PROPERTIES, TO, name), afterValue);
                    from.put(key, beforeValue);
                    to.put(key, afterValue);
                }
                else
                {
                    auditMap.put(buildPath(PROPERTIES, DELETE, name), beforeValue);
                    delete.put(key, beforeValue);
                }
            }
        }

        // Check for new values. Record individual values and group as a single map.
        Set<QName> newKeys = new HashSet<QName>(toProperties.keySet());
        newKeys.removeAll(fromProperties.keySet());
        for (QName key: newKeys)
        {
            key = key.getPrefixedQName(namespaceService); // Audit QNames with the prefix set.
            Serializable afterValue = toProperties.get(key);
            String name = replaceInvalidPathChars(key.toPrefixString());
            auditMap.put(buildPath(PROPERTIES, ADD, name), afterValue);
            add.put(key, afterValue);
        }
        
        // Record maps of additions, deletes and paired from and to values.
        if (!subAction)
        {
            if (!add.isEmpty())
            {
                auditMap.put(buildPath(PROPERTIES, ADD), add);
            }
            if (!delete.isEmpty())
            {
                auditMap.put(buildPath(PROPERTIES, DELETE), delete);
            }
            if (!from.isEmpty())
            {
                auditMap.put(buildPath(PROPERTIES, FROM), from);
            }
            if (!to.isEmpty())
            {
                auditMap.put(buildPath(PROPERTIES, TO), to);
            }
        }
    }
    
    private void addAspectChangesToAuditMap(Map<String, Serializable> auditMap,
            String addOrDelete, HashSet<QName> aspects, boolean subAction)
    {
        // Audit Set<QName> where the QName has the prefix set.
        HashSet<QName> prefixedAspects = new HashSet<QName>(aspects.size());
        for (QName aspect: aspects)
        {
            aspect = aspect.getPrefixedQName(namespaceService); // Audit QNames with the prefix set.
            prefixedAspects.add(aspect);
            String name = replaceInvalidPathChars(aspect.toPrefixString());
            auditMap.put(buildPath(ASPECTS, addOrDelete, name), null);
        }
        if (!subAction)
        {
            auditMap.put(buildPath(ASPECTS, addOrDelete), prefixedAspects);
        }
    }
    
    private void addVersionPropertiesToAuditMap(Map<String, Serializable> auditMap,
            HashMap<String, Serializable> properties, boolean subAction)
    {
        for (Map.Entry<String, Serializable> entry: properties.entrySet())
        {
            auditMap.put(buildPath(VERSION_PROPERTIES, entry.getKey()), entry.getValue());
        }
        if (!subAction)
        {
            auditMap.put(VERSION_PROPERTIES, properties);
        }
    }

    private int getSubAuditDataSize()
    {
        int size = 0;
        // No point doing sub actions if only one!
        if (subActionAuditMaps != null && subActionAuditMaps.size() > 1)
        {
            for (Map<String, Serializable> subActionAuditMap: subActionAuditMaps)
            {
                size += subActionAuditMap.size();
            }
        }
        return size;
    }
    
    private void addSubActionAuditMapsToAuditMap(Map<String, Serializable> auditMap)
    {
        // No point doing sub actions if only one!
        if (subActionAuditMaps != null && subActionAuditMaps.size() > 1)
        {
            String format = "%0"+Integer.toString(auditMap.size()).length()+"d";
            int i = 0;
            for (Map<String, Serializable> subActionAuditMap : subActionAuditMaps)
            {
                String seq = String.format(format, i);
                for (Map.Entry<String, Serializable> entry : subActionAuditMap.entrySet())
                {
                    auditMap.put(buildPath(SUB_ACTION, seq, entry.getKey()), entry.getValue());
                }
                i++;
            }
        }
    }
    
    /**
     * Returns a path to be used in an audit map. Unlike {@link AuditApplication#buildPath(String...)}
     * the returned value is relative (no leading slash).
     * @param components String.. components of the path.
     * @return a component path of the supplied values.
     */
    private String buildPath(String... components)
    {
        StringBuilder sb = new StringBuilder();
        for (String component: components)
        {
            if (sb.length() > 0)
            {
                sb.append(AUDIT_PATH_SEPARATOR);
            }
            sb.append(component);
        }
        return sb.toString();
    }
    
    /**
     * @return a String where all invalid audit path characters are replaced by a '-'.
     */
    private String replaceInvalidPathChars(String path)
    {
        return INVALID_PATH_COMP_CHAR_PATTERN.matcher(path).replaceAll(INVALID_PATH_CHAR_REPLACEMENT);
    }
}
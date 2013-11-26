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
package org.alfresco.module.org_alfresco_module_rm.model.rma.aspect;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.module.org_alfresco_module_rm.model.BaseBehaviourBean;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.annotation.Behaviour;
import org.alfresco.repo.policy.annotation.BehaviourBean;
import org.alfresco.repo.policy.annotation.BehaviourKind;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyMap;

/**
 * rma:filePlanComponent behaviour bean
 * 
 * @author Roy Wetherall
 * @since 2.2
 */
@BehaviourBean
(
   defaultType = "rma:filePlanComponent"
)
public class FilePlanComponentAspect extends    BaseBehaviourBean
                                     implements NodeServicePolicies.OnUpdatePropertiesPolicy
                               
{
    /** Well-known location of the scripts folder. */
    private NodeRef scriptsFolderNodeRef = new NodeRef("workspace", "SpacesStore", "rm_behavior_scripts");
    
    /** script service */
    private ScriptService scriptService;
    
    /** namespace service */
    private NamespaceService namespaceService;
    
    /**
     * @param scriptService set script service
     */
    public void setScriptService(ScriptService scriptService)
    {
        this.scriptService = scriptService;
    }
    
    /**
     * @param namespaceService  namespace service
     */
    public void setNamespaceService(NamespaceService namespaceService)
    {
        this.namespaceService = namespaceService;
    }    
    
    /**
     * @see org.alfresco.repo.node.NodeServicePolicies.OnUpdatePropertiesPolicy#onUpdateProperties(org.alfresco.service.cmr.repository.NodeRef, java.util.Map, java.util.Map)
     */
    @Override
    @Behaviour
    (
       kind = BehaviourKind.CLASS,
       notificationFrequency = NotificationFrequency.TRANSACTION_COMMIT
    )
    public void onUpdateProperties(final NodeRef nodeRef, final Map<QName, Serializable> before, final Map<QName, Serializable> after)
    {
        AuthenticationUtil.runAs(new RunAsWork<Void>()
        {
            @Override
            public Void doWork() throws Exception
            {
                if (nodeService.exists(nodeRef) == true)
                {
                    lookupAndExecuteScripts(nodeRef, before, after);
                }

                return null;
            }
        }, AuthenticationUtil.getAdminUserName());
    }
    
    /**
     * This method examines the old and new property sets and for those properties which
     * have changed, looks for script resources corresponding to those properties.
     * Those scripts are then called via the ScriptService.
     *
     * @param nodeWithChangedProperties the node whose properties have changed.
     * @param oldProps the old properties and their values.
     * @param newProps the new properties and their values.
     *
     * @see #lookupScripts(Map<QName, Serializable>, Map<QName, Serializable>)
     */
    private void lookupAndExecuteScripts(NodeRef nodeWithChangedProperties,
                                         Map<QName, Serializable> oldProps, 
                                         Map<QName, Serializable> newProps)
    {
        List<NodeRef> scriptRefs = lookupScripts(oldProps, newProps);

        Map<String, Object> objectModel = new HashMap<String, Object>(1);
        objectModel.put("node", nodeWithChangedProperties);
        objectModel.put("oldProperties", oldProps);
        objectModel.put("newProperties", newProps);
;
        for (NodeRef scriptRef : scriptRefs)
        {
            scriptService.executeScript(scriptRef, null, objectModel);
        }
    }
    
    /**
     * This method determines which properties have changed and for each such property
     * looks for a script resource in a well-known location.
     *
     * @param oldProps the old properties and their values.
     * @param newProps the new properties and their values.
     * @return A list of nodeRefs corresponding to the Script resources.
     *
     * @see #determineChangedProps(Map<QName, Serializable>, Map<QName, Serializable>)
     */
    private List<NodeRef> lookupScripts(Map<QName, Serializable> oldProps, Map<QName, Serializable> newProps)
    {
        List<NodeRef> result = new ArrayList<NodeRef>();

        Map<QName, Serializable> changedProps = PropertyMap.getChangedProperties(oldProps, newProps);
        for (QName propQName : changedProps.keySet())
        {
            QName prefixedQName = propQName.getPrefixedQName(namespaceService);

            String [] splitQName = QName.splitPrefixedQName(prefixedQName.toPrefixString());
            final String shortPrefix = splitQName[0];
            final String localName = splitQName[1];

            // This is the filename pattern which is assumed.
            // e.g. a script file cm_name.js would be called for changed to cm:name
            String expectedScriptName = shortPrefix + "_" + localName + ".js";

            NodeRef nextElement = nodeService.getChildByName(scriptsFolderNodeRef, ContentModel.ASSOC_CONTAINS, expectedScriptName);
            if (nextElement != null) result.add(nextElement);
        }

        return result;
    }
    
}

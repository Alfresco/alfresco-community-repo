/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2026 Alfresco Software Limited
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
package org.alfresco.repo.action.executer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.ActionModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.rule.RuleModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.RegexQNamePattern;

/**
 * Helpers shared by {@link FolderUploadMoveHandler} and {@link FolderUploadCopyHandler}.
 */
public class FolderUploadRuleSupport
{
    private NodeService nodeService;
    private DictionaryService dictionaryService;
    private BehaviourFilter behaviourFilter;
    private long windowMillis = 30 * 60_000L;

    public void setNodeService(NodeService nodeService)
    {
        this.nodeService = nodeService;
    }

    public void setDictionaryService(DictionaryService dictionaryService)
    {
        this.dictionaryService = dictionaryService;
    }

    public void setBehaviourFilter(BehaviourFilter behaviourFilter)
    {
        this.behaviourFilter = behaviourFilter;
    }

    public void setWindowMinutes(long windowMinutes)
    {
        this.windowMillis = windowMinutes * 60_000L;
    }

    /**
     * Only rules that do not run in the background or apply to subfolders. The action may be inside a composite action.
     */
    public boolean isSupportedRule(Action action)
    {
        NodeRef actionNode = action.getNodeRef();
        return actionNode != null && AuthenticationUtil.runAsSystem(() -> {
            NodeRef parent = getParent(actionNode);
            while (ActionModel.TYPE_COMPOSITE_ACTION.equals(nodeService.getType(parent)))
            {
                parent = getParent(parent);
            }
            return RuleModel.TYPE_RULE.equals(nodeService.getType(parent))
                    && !Boolean.TRUE.equals(nodeService.getProperty(parent, RuleModel.PROP_APPLY_TO_CHILDREN))
                    && !Boolean.TRUE.equals(nodeService.getProperty(parent, RuleModel.PROP_EXECUTE_ASYNC));
        });
    }

    /**
     * Any uploader can update a mark, and the folder's modified date stays the same.
     */
    public void writeMark(NodeRef folder, Runnable write)
    {
        AuthenticationUtil.runAsSystem(() -> {
            behaviourFilter.disableBehaviour(folder);
            try
            {
                write.run();
            }
            finally
            {
                behaviourFilter.enableBehaviour(folder);
            }
            return null;
        });
    }

    /**
     * Tracking stops when no file has arrived for the configured number of minutes.
     */
    public boolean isRecent(Date lastActivity)
    {
        return lastActivity != null && System.currentTimeMillis() - lastActivity.getTime() <= windowMillis;
    }

    public List<NodeRef> getChildren(NodeRef folder)
    {
        List<NodeRef> children = new ArrayList<>();
        for (ChildAssociationRef child : nodeService.getChildAssocs(folder, ContentModel.ASSOC_CONTAINS, RegexQNamePattern.MATCH_ALL))
        {
            if (child.isPrimary())
            {
                children.add(child.getChildRef());
            }
        }
        return children;
    }

    public NodeRef getParent(NodeRef node)
    {
        return nodeService.getPrimaryParent(node).getParentRef();
    }

    public String getName(NodeRef node)
    {
        return (String) nodeService.getProperty(node, ContentModel.PROP_NAME);
    }

    public boolean isFolder(NodeRef node)
    {
        return dictionaryService.isSubClass(nodeService.getType(node), ContentModel.TYPE_FOLDER);
    }
}

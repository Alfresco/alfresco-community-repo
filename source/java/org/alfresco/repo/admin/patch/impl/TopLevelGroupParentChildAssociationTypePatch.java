/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
package org.alfresco.repo.admin.patch.impl;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.springframework.extensions.surf.util.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

public class TopLevelGroupParentChildAssociationTypePatch extends AbstractPatch
{
    private static final String MSG_RESULT = "patch.topLevelGroupParentChildAssociationTypePatch.result";
    private static final String ERR_SYS_PATH_NOT_FOUND = "patch.topLevelGroupParentChildAssociationTypePatch.err.sys_path_not_found";
    private static final String ERR_AUTH_PATH_NOT_FOUND = "patch.topLevelGroupParentChildAssociationTypePatch.err.auth_path_not_found";

    public TopLevelGroupParentChildAssociationTypePatch()
    {
        super();
    }

    @Override
    protected String applyInternal() throws Exception
    {
        NodeRef nodeRef = getAuthorityContainer();
        List<ChildAssociationRef> results = nodeService.getChildAssocs(nodeRef);
        for (ChildAssociationRef car : results)
        {
            if (!car.getTypeQName().equals(ContentModel.ASSOC_CHILDREN))
            {
                nodeService.moveNode(
                        car.getChildRef(),
                        car.getParentRef(),
                        ContentModel.ASSOC_CHILDREN,
                        car.getQName());
            }
        }

        return I18NUtil.getMessage(MSG_RESULT, results.size());
    }

    private NodeRef getAuthorityContainer()
    {
        QName qnameAssocSystem = QName.createQName("sys", "system", namespaceService);
        QName qnameAssocAuthorities = QName.createQName("sys", "authorities", this.namespaceService);

       
        NodeRef rootNodeRef = nodeService.getRootNode(new StoreRef("user", "alfrescoUserStore"));
        List<ChildAssociationRef> results = nodeService.getChildAssocs(rootNodeRef, RegexQNamePattern.MATCH_ALL,
                qnameAssocSystem);
        NodeRef sysNodeRef = null;
        if (results.size() == 0)
        {
            throw new AlfrescoRuntimeException(ERR_SYS_PATH_NOT_FOUND, new Object[] {qnameAssocSystem});
        }
        else
        {
            sysNodeRef = results.get(0).getChildRef();
        }
        results = nodeService.getChildAssocs(sysNodeRef, RegexQNamePattern.MATCH_ALL, qnameAssocAuthorities);
        NodeRef authNodeRef = null;
        if (results.size() == 0)
        {
            throw new AlfrescoRuntimeException(ERR_AUTH_PATH_NOT_FOUND, new Object[] {qnameAssocAuthorities});
        }
        else
        {
            authNodeRef = results.get(0).getChildRef();
        }
        return authNodeRef;
    }
}

/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.repo.admin.patch.impl;

import java.util.List;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.i18n.I18NUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.security.authority.AuthorityDAOImpl;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
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

       
        NodeRef rootNodeRef = nodeService.getRootNode(AuthorityDAOImpl.STOREREF_USERS);
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

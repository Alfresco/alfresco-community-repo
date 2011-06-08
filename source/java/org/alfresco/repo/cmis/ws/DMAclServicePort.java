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
package org.alfresco.repo.cmis.ws;

import org.alfresco.cmis.CMISAccessControlReport;
import org.alfresco.cmis.CMISServiceException;
import org.alfresco.opencmis.CMISAccessControlFormatEnum;
import org.alfresco.repo.cmis.ws.utils.ExceptionUtil;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author Dmitry Velichkevich
 */
@javax.jws.WebService(name = "ACLServicePort", serviceName = "ACLService", portName = "ACLServicePort", targetNamespace = "http://docs.oasis-open.org/ns/cmis/ws/200908/", endpointInterface = "org.alfresco.repo.cmis.ws.ACLServicePort")
public class DMAclServicePort extends DMAbstractServicePort implements ACLServicePort
{
    /**
     * 
     */
    public CmisACLType applyACL(String repositoryId, String objectId, CmisAccessControlListType addACEs, CmisAccessControlListType removeACEs, EnumACLPropagation aclPropagation,
            CmisExtensionType extension) throws CmisException
    {
        checkRepositoryId(repositoryId);
        NodeRef object;
        try
        {
            object = cmisService.getObject(objectId, NodeRef.class, true, false, false);
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
        return applyAclCarefully(object, addACEs, removeACEs, aclPropagation, null);
    }

    /**
     * 
     */
    public CmisACLType getACL(String repositoryId, String objectId, Boolean onlyBasicPermissions, CmisExtensionType extension) throws CmisException
    {
        checkRepositoryId(repositoryId);
        NodeRef nodeRef;
        try
        {
            nodeRef = cmisService.getReadableObject(objectId, NodeRef.class);
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
        CMISAccessControlFormatEnum permissionsKind = ((null == onlyBasicPermissions) || onlyBasicPermissions) ? (CMISAccessControlFormatEnum.CMIS_BASIC_PERMISSIONS)
                : (CMISAccessControlFormatEnum.REPOSITORY_SPECIFIC_PERMISSIONS);
        CMISAccessControlReport aclReport = cmisAclService.getAcl(nodeRef, permissionsKind);
        return convertAclReportToCmisAclType(aclReport);
    }
}

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

import java.util.Collections;
import java.util.List;

import javax.xml.ws.Holder;

import org.alfresco.cmis.CMISServiceException;
import org.alfresco.repo.cmis.ws.utils.ExceptionUtil;

@javax.jws.WebService(name = "PolicyServicePort", serviceName = "PolicyService", portName = "PolicyServicePort", targetNamespace = "http://docs.oasis-open.org/ns/cmis/ws/200908/", endpointInterface = "org.alfresco.repo.cmis.ws.PolicyServicePort")
public class DMPolicyServicePort extends DMAbstractServicePort implements PolicyServicePort
{
    /**
     * Applies a policy object to a target object.
     * 
     * @param repositoryId repository Id
     * @param policyId policy Id
     * @param objectId target object Id
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, CONSTRAINT)
     */
    public void applyPolicy(String repositoryId, String policyId, String objectId, Holder<CmisExtensionType> extension) throws CmisException
    {
        checkRepositoryId(repositoryId);
        try
        {
            cmisService.applyPolicy(policyId, objectId);
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
    }

    /**
     * Gets the list of policy objects currently applied to a target object.
     * 
     * @param parameters repositoryId: repository Id; objectId: target object Id; filter: filter specifying which properties to return
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, FILTER_NOT_VALID)
     */
    public List<CmisObjectType> getAppliedPolicies(String repositoryId, String objectId, String filter, CmisExtensionType extension) throws CmisException
    {
        checkRepositoryId(repositoryId);
        try
        {
            cmisService.getAppliedPolicies(objectId, filter);
            return Collections.emptyList();
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
    }

    /**
     * Removes a previously applied policy from a target object. The policy object is not deleted, and may still be applied to other objects.
     * 
     * @param repositoryId repository Id
     * @param policyId policy Id
     * @param objectId target object Id.
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, CONSTRAINT)
     */
    public void removePolicy(String repositoryId, String policyId, String objectId, Holder<CmisExtensionType> extension) throws CmisException
    {
        checkRepositoryId(repositoryId);
        try
        {
            cmisService.removePolicy(policyId, objectId);
        }
        catch (CMISServiceException e)
        {
            throw ExceptionUtil.createCmisException(e);
        }
    }
}

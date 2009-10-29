/*
 * Copyright (C) 2005-2008 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of
 * the GPL, you may redistribute this Program in connection with Free/Libre
 * and Open Source Software ("FLOSS") applications as described in Alfresco's
 * FLOSS exception.  You should have recieved a copy of the text describing
 * the FLOSS exception, and it is also available here:
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.repo.cmis.ws;

import java.util.List;

import javax.xml.ws.Holder;

@javax.jws.WebService(name = "PolicyServicePort", serviceName = "PolicyServicePort", portName = "PolicyServicePort", targetNamespace = "http://docs.oasis-open.org/ns/cmis/ws/200908/", endpointInterface = "org.alfresco.repo.cmis.ws.PolicyServicePort")
public class DMPolicyServicePort extends DMAbstractServicePort implements PolicyServicePort
{
    private static final String POLICY_NOT_SUPPORTED_MESSAGE = "PolicyService not implemented";

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
        throw cmisObjectsUtils.createCmisException(POLICY_NOT_SUPPORTED_MESSAGE, EnumServiceException.RUNTIME);
    }

    /**
     * Gets the list of policy objects currently applied to a target object.
     * 
     * @param parameters repositoryId: repository Id; objectId: target object Id; filter: filter specifying which properties to return
     * @throws CmisException (with following {@link EnumServiceException} : INVALID_ARGUMENT, OBJECT_NOT_FOUND, NOT_SUPPORTED, PERMISSION_DENIED, RUNTIME, FILTER_NOT_VALID)
     */
    public List<CmisObjectType> getAppliedPolicies(String repositoryId, String objectId, String filter, CmisExtensionType extension) throws CmisException
    {
        throw cmisObjectsUtils.createCmisException(POLICY_NOT_SUPPORTED_MESSAGE, EnumServiceException.RUNTIME);
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
        throw cmisObjectsUtils.createCmisException(POLICY_NOT_SUPPORTED_MESSAGE, EnumServiceException.RUNTIME);
    }
}

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

@javax.jws.WebService(name = "PolicyServicePort", serviceName = "PolicyServicePort", portName = "PolicyServicePort", targetNamespace = "http://www.cmis.org/ns/1.0", endpointInterface = "org.alfresco.repo.cmis.ws.PolicyServicePort")
public class DMPolicyServicePort extends DMAbstractServicePort implements PolicyServicePort
{

    /**
     * Applies a policy object to a target object.
     * 
     * @param repositoryId repository Id
     * @param policyId policy Id
     * @param objectId target object Id
     * @throws PermissionDeniedException
     * @throws UpdateConflictException
     * @throws ObjectNotFoundException
     * @throws OperationNotSupportedException
     * @throws InvalidArgumentException
     * @throws RuntimeException
     * @throws ConstraintViolationException
     */
    public void applyPolicy(String repositoryId, String policyId, String objectId) throws PermissionDeniedException, UpdateConflictException, ObjectNotFoundException,
            OperationNotSupportedException, InvalidArgumentException, RuntimeException, ConstraintViolationException
    {
        // TODO Auto-generated method stub

    }

    /**
     * Gets the list of policy objects currently applied to a target object.
     * 
     * @param parameters repositoryId: repository Id; objectId: target object Id; filter: filter specifying which properties to return
     * @throws PermissionDeniedException
     * @throws UpdateConflictException
     * @throws FilterNotValidException
     * @throws ObjectNotFoundException
     * @throws OperationNotSupportedException
     * @throws InvalidArgumentException
     * @throws RuntimeException
     * @throws ConstraintViolationException
     */
    public GetAppliedPoliciesResponse getAppliedPolicies(GetAppliedPolicies parameters) throws PermissionDeniedException, UpdateConflictException, FilterNotValidException,
            ObjectNotFoundException, OperationNotSupportedException, InvalidArgumentException, RuntimeException, ConstraintViolationException
    {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Removes a previously applied policy from a target object. The policy object is not deleted, and may still be applied to other objects.
     * 
     * @param repositoryId repository Id
     * @param policyId policy Id
     * @param objectId target object Id.
     * @throws PermissionDeniedException
     * @throws UpdateConflictException
     * @throws ObjectNotFoundException
     * @throws OperationNotSupportedException
     * @throws InvalidArgumentException
     * @throws RuntimeException
     * @throws ConstraintViolationException
     */
    public void removePolicy(String repositoryId, String policyId, String objectId) throws PermissionDeniedException, UpdateConflictException, ObjectNotFoundException,
            OperationNotSupportedException, InvalidArgumentException, RuntimeException, ConstraintViolationException
    {
        // TODO Auto-generated method stub

    }

}

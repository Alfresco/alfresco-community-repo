/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2021 Alfresco Software Limited
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
package org.alfresco.repo.security.permissions.impl.acegi;

import java.util.Set;

import net.sf.acegisecurity.vote.AccessDecisionVoter;
import org.alfresco.repo.security.permissions.impl.SimplePermissionReference;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.QName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utility methods extracted from AclEntryVoter
 *
 * @author Lev Belava
 */
final class ACLEntryVoterUtils
{
    private static final Logger LOG = LoggerFactory.getLogger(ACLEntryVoterUtils.class);
    private static final String ABSTAIN = "Abstain";

    private ACLEntryVoterUtils()
    {
    }


    /**
     * Gets NodeRef for testObject based on inferred type
     *
     * @param testObject                Tested object to work on
     * @param nodeService               Node service to perform checks on refs
     * @return                          NodeRef for testObject or null if (testObject is null or StoreRef from testObject does not exist in the provided NodeService)
     * @throws ACLEntryVoterException   if testObject is not null and not one of a NodeRef or ChildAssociationRef types
     */
    static NodeRef getNodeRef(Object testObject, NodeService nodeService)
    {
        if (testObject == null)
        {
            return null;
        }

        if (StoreRef.class.isAssignableFrom(testObject.getClass()))
        {
            LOG.debug("Permission test against the store - using permissions on the root node");
            StoreRef storeRef = (StoreRef) testObject;
            if (nodeService.exists(storeRef))
            {
                return nodeService.getRootNode(storeRef);
            }
            else
            {
                LOG.debug("StoreRef does not exist");
                return null;
            }
        }

        if (NodeRef.class.isAssignableFrom(testObject.getClass()))
        {
            NodeRef result = (NodeRef) testObject;
            if (LOG.isDebugEnabled())
            {
                if (nodeService.exists(result))
                {
                    LOG.debug("Permission test on node {}", nodeService.getPath(result));
                }
                else
                {
                    LOG.debug("Permission test on non-existing node {}", result);
                }
            }
            return result;
        }

        if (ChildAssociationRef.class.isAssignableFrom(testObject.getClass()))
        {
            ChildAssociationRef testChildRef = (ChildAssociationRef) testObject;
            NodeRef result = testChildRef.getChildRef();
            if (LOG.isDebugEnabled())
            {
                if (nodeService.exists(result))
                {
                    LOG.debug("Permission test on node {}", nodeService.getPath(result));
                }
                else
                {
                    LOG.debug("Permission test on non-existing node {}", result);
                }
            }
            return result;
        }

        throw new ACLEntryVoterException("The specified parameter is not a NodeRef or ChildAssociationRef");
    }


    /**
     * Checks if tested NodeRef instance is abstained or denied based on set of QNames to abstain and
     *
     * @param requiredPermissionReference           Required permissions
     * @param testNodeRef                           NodeRef to be verified
     * @param abstainForClassQNames                 Set of QNames to abstain
     * @param nodeService                           Node service to perform checks on tested NodeRef
     * @param permissionService                     Permission service to check for required permissions
     * @return                                      null if testNodeRef is not abstained or denied, otherwise returns appropriate status.
     */
    static Integer shouldAbstainOrDeny(SimplePermissionReference requiredPermissionReference, NodeRef testNodeRef, Set<QName> abstainForClassQNames,
                                        NodeService nodeService, PermissionService permissionService)
    {
        if (testNodeRef == null)
        {
            return null;
        }

        LOG.debug("Node ref is not null");

        if (abstainForClassQNames.size() > 0 && nodeService.exists(testNodeRef))
        {
            if (abstainForClassQNames.contains(nodeService.getType(testNodeRef)))
            {
                LOG.debug(ABSTAIN);
                return AccessDecisionVoter.ACCESS_ABSTAIN;
            }
            Set<QName> testNodeRefAspects = nodeService.getAspects(testNodeRef);
            for (QName abstain : abstainForClassQNames)
            {
                if (testNodeRefAspects.contains(abstain))
                {
                    LOG.debug(ABSTAIN);
                    return AccessDecisionVoter.ACCESS_ABSTAIN;
                }
            }
        }

        if (AccessStatus.DENIED == permissionService.hasPermission(testNodeRef, requiredPermissionReference.toString()))
        {
            if (LOG.isDebugEnabled())
            {
                LOG.debug("Permission is denied");
                Thread.dumpStack();
            }
            return AccessDecisionVoter.ACCESS_DENIED;
        }

        return null;
    }

}

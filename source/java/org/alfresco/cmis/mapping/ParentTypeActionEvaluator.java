/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
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
package org.alfresco.cmis.mapping;

import java.util.List;

import org.alfresco.cmis.CMISAllowedActionEnum;
import org.alfresco.cmis.CMISServices;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.chemistry.abdera.ext.CMISAllowableActions;

/**
 * This evaluator determines an action availability in accordance with parent(s) of object. The rules are:<br />
 * - is it expected that object has parent of {@link ParentTypeEnum} or not.<br />
 * <br />
 * This evaluator is generic, because it is used in the scope of {@link CompositeActionEvaluator}
 * 
 * @author Dmitry Velichkevich
 */
public class ParentTypeActionEvaluator<ObjectType> extends AbstractActionEvaluator<ObjectType>
{
    private CMISServices cmisServices;

    private NodeService nodeService;

    private ParentTypeEnum parentType;

    private boolean expected;

    /**
     * Constructor
     * 
     * @param serviceRegistry - {@link ServiceRegistry} instance
     * @param action - {@link CMISAllowableActions} enumeration value, which determines the action to check
     * @param parentType - {@link ParentTypeEnum} enumeration value, which determines type of parent, which should be validated
     * @param expected - {@link Boolean} value, which determines: <code>true</code> - object should have <code>parentType</code> parent, <code>false</code> - object should NOT have
     *        <code>parentType</code> parent
     */
    protected ParentTypeActionEvaluator(ServiceRegistry serviceRegistry, CMISAllowedActionEnum action, ParentTypeEnum parentType, boolean expected)
    {
        super(serviceRegistry, action);
        this.parentType = parentType;
        this.expected = expected;

        cmisServices = serviceRegistry.getCMISService();
        nodeService = serviceRegistry.getNodeService();
    }

    /* (non-Javadoc)
     * @see org.alfresco.cmis.CMISActionEvaluator#isAllowed(java.lang.Object)
     */
    @Override
    public boolean isAllowed(ObjectType object)
    {
        NodeRef nodeRef = (object instanceof NodeRef) ? ((NodeRef) object) : (null);

        NodeRef rootNodeRef = cmisServices.getDefaultRootNodeRef();
        if ((null == nodeRef) || rootNodeRef.equals(nodeRef))
        {
            return false;
        }

        List<ChildAssociationRef> parentAssocs = nodeService.getParentAssocs(nodeRef);
        if ((ParentTypeEnum.REPOSITORY_ROOT == parentType) || (ParentTypeEnum.PRIMARY_REPOSITORY_ROOT == parentType))
        {
            ChildAssociationRef root = null;
            for (ChildAssociationRef ref : parentAssocs)
            {
                root = (rootNodeRef.equals(ref.getParentRef())) ? (ref) : (null);
                if (null != root)
                {
                    if ((ParentTypeEnum.PRIMARY_REPOSITORY_ROOT == parentType) && !ref.isPrimary())
                    {
                        root = null;
                    }

                    break;
                }
            }

            return (expected) ? (null != root) : (null == root);
        }

        return (expected) ? (parentAssocs.size() > 1) : (1 == parentAssocs.size());
    }

    /**
     * Enumeration of type of parents, which may be required to check availability of some action for an object
     * 
     * @author Dmitry Velichkevich
     * @see MultiFilingServicePort
     */
    public static enum ParentTypeEnum
    {
        /**
         * One or more parents, which are not primary (see {@link MultiFilingServicePort#addObjectToFolder(String, String, String, Boolean, javax.xml.ws.Holder)} and
         * {@link MultiFilingServicePort#removeObjectFromFolder(String, String, String, javax.xml.ws.Holder)})
         */
        MULTI_FILED,

        /**
         * Default repository root as a primary or one of the multi filed parents
         */
        REPOSITORY_ROOT,

        /**
         * Default repository root only as primary parent
         */
        PRIMARY_REPOSITORY_ROOT;
    }
}

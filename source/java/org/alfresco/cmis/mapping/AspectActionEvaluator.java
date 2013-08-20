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

import org.alfresco.cmis.CMISAllowedActionEnum;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.chemistry.abdera.ext.CMISAllowableActions;

/**
 * This evaluator determines an action availability in accordance with collection of aspects and rules of checking application of the aspects. The rules are:<br />
 * - is it expected that aspect(s) had been applied;<br />
 * - should all aspects in the collection satisfying to the specified condition or at least 1 aspect is enough<br />
 * <br />
 * This evaluator is generic, because it is used in the scope of {@link CompositeActionEvaluator}
 * 
 * @author Dmitry Velichkevich
 */
public class AspectActionEvaluator<ObjectType> extends AbstractActionEvaluator<ObjectType>
{
    private NodeService nodeService;

    private boolean expected;

    private boolean allAspectsMustConcur;

    private boolean defaultAllowing;

    private QName[] aspects;

    /**
     * Constructor
     * 
     * @param serviceRegistry - {@link ServiceRegistry} instance
     * @param action - {@link CMISAllowableActions} enumeration value, which determines the action to check
     * @param expected - {@link Boolean} value, which determines: <code>true</code> - aspects application is expected, <code>false</code> - aspects absence is expected
     * @param allAspectsMustConcur - {@link Boolean} value, which determines: <code>true</code> - all aspects should satisfy <code>expected</code> condition, <code>false</code> -
     *        at least 1 aspect should satisfy the <code>expected</code> condition
     * @param defaultAllowing - {@link Boolean} value, which determines availability of action for several special cases (invalid object id, empty collection of the aspects etc.)
     * @param aspects {@link QName}... collection, which specifies all aspects, required for validation
     */
    public AspectActionEvaluator(ServiceRegistry serviceRegistry, CMISAllowedActionEnum action, boolean expected, boolean allAspectsMustConcur, boolean defaultAllowing,
            QName... aspects)
    {
        super(serviceRegistry, action);
        this.expected = expected;
        this.allAspectsMustConcur = allAspectsMustConcur;
        this.aspects = aspects;

        nodeService = serviceRegistry.getNodeService();
    }

    public boolean isAllowed(ObjectType id)
    {
        NodeRef nodeRef = (id instanceof NodeRef) ? ((NodeRef) id) : (null);

        if ((null != nodeRef) && (null != aspects))
        {
            for (QName aspectId : aspects)
            {
                boolean aspect = nodeService.hasAspect(nodeRef, aspectId);

                if (!expected)
                {
                    aspect = !aspect;
                }

                if (!allAspectsMustConcur && aspect)
                {
                    return true;
                }
                else
                {
                    if (allAspectsMustConcur && !aspect)
                    {
                        return false;
                    }
                }
            }

            return allAspectsMustConcur;
        }

        return defaultAllowing;
    }
}
/* 
 * Copyright (C) 2005-2015 Alfresco Software Limited.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see http://www.gnu.org/licenses/.
 */

package org.alfresco.repo.virtual.bundle;

import org.alfresco.repo.domain.permissions.Acl;
import org.alfresco.repo.domain.permissions.traitextender.AccessControlListDAOExtension;
import org.alfresco.repo.domain.permissions.traitextender.AccessControlListDAOTrait;
import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.ref.GetActualNodeRefMethod;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.traitextender.SpringBeanExtension;

public class VirtualAccessControlListDAOExtension
            extends SpringBeanExtension<AccessControlListDAOExtension, AccessControlListDAOTrait>
            implements AccessControlListDAOExtension
{
    private ActualEnvironment environment;

    public VirtualAccessControlListDAOExtension()
    {
        super(AccessControlListDAOTrait.class);
    }

    public void setEnvironment(ActualEnvironment environment)
    {
        this.environment = environment;
    }

    @Override
    public Acl getAccessControlList(NodeRef nodeRef)
    {

        if (Reference.isReference(nodeRef))
        {
            Reference vRef = Reference.fromNodeRef(nodeRef);
            NodeRef actual = vRef.execute(new GetActualNodeRefMethod(environment));
            return getTrait().getAccessControlList(actual);
        }
        return getTrait().getAccessControlList(nodeRef);

    }
}

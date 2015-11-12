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

import org.alfresco.repo.lock.mem.LockState;
import org.alfresco.repo.lock.traitextender.LockableAspectInterceptorExtension;
import org.alfresco.repo.lock.traitextender.LockableAspectInterceptorTrait;
import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.ref.GetActualNodeRefMethod;
import org.alfresco.repo.virtual.ref.Reference;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.traitextender.SpringBeanExtension;

public class VirtualLockableAspectInterceptorExtension extends
            SpringBeanExtension<LockableAspectInterceptorExtension, LockableAspectInterceptorTrait> implements
            LockableAspectInterceptorExtension
{

    private ActualEnvironment environment;

    public VirtualLockableAspectInterceptorExtension()
    {
        super(LockableAspectInterceptorTrait.class);
    }

    public void setEnvironment(ActualEnvironment environment)
    {
        this.environment = environment;
    }

    @Override
    public LockState getLockState(NodeRef nodeRef)
    {
        if(Reference.isReference(nodeRef)){
            Reference reference = Reference.fromNodeRef(nodeRef);
            NodeRef actualNodeRef = reference.execute(new GetActualNodeRefMethod(environment));
            return getTrait().traitImplOf_getLockState(actualNodeRef);
        }
        return getTrait().traitImplOf_getLockState(nodeRef);
    }

}

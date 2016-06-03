/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
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
package org.alfresco.repo.virtual.bundle;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.traitextender.DefaultCopyBehaviourCallbackExtension;
import org.alfresco.repo.copy.traitextender.DefaultCopyBehaviourCallbackTrait;
import org.alfresco.repo.virtual.ActualEnvironment;
import org.alfresco.repo.virtual.VirtualContentModel;
import org.alfresco.service.namespace.QName;
import org.alfresco.traitextender.SpringBeanExtension;

public class VirtualDefaultCopyBehaviourCallbackExtension extends
SpringBeanExtension<DefaultCopyBehaviourCallbackExtension, DefaultCopyBehaviourCallbackTrait> implements
DefaultCopyBehaviourCallbackExtension
{
    private ActualEnvironment environment;

    public VirtualDefaultCopyBehaviourCallbackExtension()
    {
        super(DefaultCopyBehaviourCallbackTrait.class);
    }

    public void setEnvironment(ActualEnvironment environment)
    {
        this.environment = environment;
    }

    @Override
    public boolean getMustCopy(QName classQName, CopyDetails copyDetails)
    {
        if(environment.isSubClass(classQName, ContentModel.TYPE_FOLDER)){
            if(copyDetails.getSourceNodeAspectQNames().contains(VirtualContentModel.ASPECT_VIRTUAL)){
                return false;
            }
        }
        return getTrait().getMustCopy(classQName,
                                      copyDetails);
    }

}

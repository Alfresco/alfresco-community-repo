/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2022 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * -
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 * -
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * -
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * -
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package org.alfresco.module.org_alfresco_module_rm.capability.declarative.condition;

import org.alfresco.module.org_alfresco_module_rm.capability.declarative.AbstractCapabilityCondition;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Movable capability condition.
 *
 * @author Roxana Dina
 * @since 2.4.1
 *
 */
public class MovableRecordFolderCapabilityCondition extends AbstractCapabilityCondition
{

    /**
     * A record folder should not be moved when it is cut off, but it should be possible to move it when it is destroyed.
     */
    @Override
    public boolean evaluateImpl(NodeRef nodeRef)
    {
        if (nodeService.hasAspect(nodeRef, ASPECT_GHOSTED) && dispositionService.isDisposableItemCutoff(nodeRef))
            return true;
        return !(dispositionService.isDisposableItemCutoff(nodeRef) || recordFolderService.isRecordFolderClosed(nodeRef));
    }
}

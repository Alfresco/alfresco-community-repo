/*
 * #%L
 * Alfresco Records Management Module
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.module.org_alfresco_module_rm.patch.v35;

import org.alfresco.module.org_alfresco_module_rm.patch.AbstractModulePatch;

/**
 * Patch to update disposition properties in all those folders which are moved from one category to another category
 * and missing disposition properties
 */
public class RMv35UpdateDispositionPropertiesPatch extends AbstractModulePatch
{
    /** The batch size for processing frozen nodes. */
    private int batchSize = 1000;

    /**
     * Setter for maximum batch size
     * @param batchSize The max amount of associations to be created between the frozen nodes and the hold in a transaction
     */
    public void setBatchSize(int batchSize)
    {
        this.batchSize = batchSize;
    }

    @Override
    public void applyInternal()
    {

    }
}

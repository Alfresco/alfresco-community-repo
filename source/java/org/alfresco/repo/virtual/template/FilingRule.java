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

package org.alfresco.repo.virtual.template;

import org.alfresco.repo.virtual.VirtualizationException;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * A rule for filing (storing in a physical location) content that is created in
 * a virtual folder.
 *
 * @author Bogdan Horje
 */
public interface FilingRule
{
    FilingData createFilingData(FilingParameters parameters) throws VirtualizationException;

    NodeRef filingNodeRefFor(FilingParameters parameters) throws VirtualizationException;

    boolean isNullFilingRule();
}

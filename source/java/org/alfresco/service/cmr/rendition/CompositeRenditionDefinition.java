/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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

package org.alfresco.service.cmr.rendition;

import org.alfresco.service.cmr.action.ActionList;

/**
 * This is a special {@link RenditionDefinition} which allows sequential
 * execution of a list of other {@link RenditionDefinition}s. For example, it
 * might be used to transform a PDF file to a JPEG imaged and then resize that
 * image. This would be achieved by creating a
 * {@link CompositeRenditionDefinition} that has two sub-definitions, one to
 * reformat the PDF to a JPEG image and the second to resize the JPEG image.
 * 
 * @author Nick Smith
 */
public interface CompositeRenditionDefinition extends RenditionDefinition, ActionList<RenditionDefinition>
{
    // Intentionally empty!
}

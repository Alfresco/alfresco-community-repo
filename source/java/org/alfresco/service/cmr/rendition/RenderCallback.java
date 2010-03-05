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

import org.alfresco.service.cmr.repository.ChildAssociationRef;

/**
 * This interface defines a callback object which can be used to handle the ultimate
 * result of asynchronous renditions.
 * 
 * @author Neil McErlean
 * @see RenditionService#render(org.alfresco.service.cmr.repository.NodeRef, RenditionDefinition, RenderCallback)
 */
public interface RenderCallback
{
    /**
     * This callback method will be called upon successful completion of an asynchronous
     * rendition.
     * @param primaryParentOfNewRendition a ChildAssociationRef linking the new rendition
     *            object to its primary parent.
     */
    void handleSuccessfulRendition(ChildAssociationRef primaryParentOfNewRendition);
    
    /**
     * This callback method will be called upon unsuccessful completion of an
     * asynchronous rendition.
     * @param t the Throwable giving the cause of the rendition failure.
     */
    void handleFailedRendition(Throwable t);
}

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

import java.io.Serializable;

import org.alfresco.api.AlfrescoPublicApi;    
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * This class is used to fully specify a type of rendition. It specifies which
 * rendering engine will be used as well as the parameters that will be given to
 * that engine.
 * <P/>
 * Every RenditionDefinition has a <code>renditionName</code> attribute which
 * uniquely identifies it.
 * 
 * @author Nick Smith
 * @author Neil McErlean
 */
@AlfrescoPublicApi
public interface RenditionDefinition extends Action, Serializable
{
    /**
     * @return the name which uniquely identifies this rendition definition.
     */
    QName getRenditionName();

    /**
     * Returns the node to which the rendition is linked when it is first
     * created. Typically this location is only temporary temporary as the
     * rendition will be moved to a different location by the
     * {@link RenditionService} shortly after its creation.
     * 
     * @return the renditionParent
     */
    NodeRef getRenditionParent();

    /**
     * Sets the node to which the rendition is linked when it is first created.
     * Typically this location is only temporary temporary as the rendition will
     * be moved to a different location by the {@link RenditionService} shortly
     * after its creation.
     * 
     * @param renditionParent the renditionParent to set
     */
    void setRenditionParent(NodeRef renditionParent);

    /**
     * Returns the association type used to link the rendition to its parent
     * node after it has been newly created. Typically this association is only
     * temporary as the rendition will be moved to a different location by the
     * {@link RenditionService} shortly after its creation.
     * 
     * @return the renditionAssociationType
     */
    QName getRenditionAssociationType();

    /**
     * Sets the association type used to link the rendition to its parent node
     * after it has been newly created. Typically this association is only
     * temporary as the rendition will be moved to a different location by the
     * {@link RenditionService} shortly after its creation.
     * 
     * @param renditionAssociationType the renditionAssociationType to set
     */
    void setRenditionAssociationType(QName renditionAssociationType);
    
    /**
     * This method sets a callback object for use in asynchronous renditions. It is
     * this object that will be notified of the successful or unsuccessful completion
     * of these renditions.
     * 
     * @param callback a callback object, which may be null.
     */
    void setCallback(RenderCallback callback);
    
    /**
     * This method gets the registered callback object for use with asynchronous
     * renditions.
     * 
     * @return the callback object
     */
    RenderCallback getCallback();
}

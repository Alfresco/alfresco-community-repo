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

package org.alfresco.repo.rendition;

import java.io.Serializable;

import org.alfresco.repo.action.ActionImpl;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.rendition.RenderCallback;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 * @author Neil McErlean
 * @since 3.3
 */
public class RenditionDefinitionImpl extends ActionImpl implements RenditionDefinition
{
    /**
     * Serial version UID
     */
    private static final long serialVersionUID = 4336392868488634875L;

    public static final String RENDITION_DEFINITION_NAME = "renderingActionName";

    public NodeRef renditionParent;
    public QName renditionAssociationType;
    private RenderCallback renderCallback;

    /**
     * @param id
     *            the action id
     * @param renditionName
     *            a unique name for the rendering action.
     * @param renderingEngineName
     *            the name of the rendering action definition
     */
    public RenditionDefinitionImpl(String id, QName renditionName, String renderingEngineName)
    {
        super(null, id, renderingEngineName);
        setParameterValue(RENDITION_DEFINITION_NAME, renditionName);
    }

    public RenditionDefinitionImpl(Action action)
    {
        super(action);
    }

    public RenditionDefinitionImpl(Action action, String renderingEngineName)
    {
        super(action, renderingEngineName);
    }

    /*
     * @see
     * org.alfresco.service.cmr.rendition.RenditionDefinition#getRenditionName()
     */
    public QName getRenditionName()
    {
        Serializable parameterValue = getParameterValue(RENDITION_DEFINITION_NAME);
		return (QName) parameterValue;
    }

    /*
     * @see
     * org.alfresco.service.cmr.rendition.RenditionDefinition#getRenditionParent
     * ()
     */
    public NodeRef getRenditionParent()
    {
        return this.renditionParent;
    }

    /*
     * @see
     * org.alfresco.service.cmr.rendition.RenditionDefinition#setRenditionParent
     * (org.alfresco.service.cmr.repository.NodeRef)
     */
    public void setRenditionParent(NodeRef renditionParent)
    {
        this.renditionParent = renditionParent;
    }

    /*
     * @seeorg.alfresco.service.cmr.rendition.RenditionDefinition#
     * getRenditionAssociationType()
     */
    public QName getRenditionAssociationType()
    {
        return this.renditionAssociationType;
    }

    /*
     * @seeorg.alfresco.service.cmr.rendition.RenditionDefinition#
     * setRenditionAssociationType(org.alfresco.service.namespace.QName)
     */
    public void setRenditionAssociationType(QName renditionAssociationType)
    {
        this.renditionAssociationType = renditionAssociationType;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.service.cmr.rendition.RenditionDefinition#setCallback(org
     * .alfresco.service.cmr.rendition.RenderCallback)
     */
    public void setCallback(RenderCallback callback)
    {
        this.renderCallback = callback;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.alfresco.service.cmr.rendition.RenditionDefinition#setCallback(org
     * .alfresco.service.cmr.rendition.RenderCallback)
     */
    public RenderCallback getCallback()
    {
        return this.renderCallback;
    }
}

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

package org.alfresco.repo.rendition;

import java.util.List;

import org.alfresco.repo.action.ActionListImpl;
import org.alfresco.repo.rendition.executer.CompositeRenderingEngine;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionList;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.rendition.CompositeRenditionDefinition;
import org.alfresco.service.cmr.rendition.RenditionDefinition;
import org.alfresco.service.namespace.QName;

/**
 * @author Nick Smith
 *
 * @deprecated The RenditionService is being replace by the simpler async RenditionService2.
 */
@Deprecated
public class CompositeRenditionDefinitionImpl extends RenditionDefinitionImpl implements CompositeRenditionDefinition
{
    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -770880495976834168L;

    private final ActionList<RenditionDefinition> actions = new ActionListImpl<RenditionDefinition>();

    /**
     * @param id
     *            - the action id
     * @param renditionName
     *            - a unique name for the rendering action
     */
    public CompositeRenditionDefinitionImpl(String id, QName renditionName)
    {
        super(id, renditionName, CompositeRenderingEngine.NAME);
    }

    public CompositeRenditionDefinitionImpl(CompositeAction compositeAction)
    {
        super(compositeAction, CompositeRenderingEngine.NAME);
        for (Action action : compositeAction.getActions())
        {
            RenditionDefinition subDefinition;
            if (action instanceof CompositeAction)
            {
                CompositeAction compAction = (CompositeAction) action;
                subDefinition = new CompositeRenditionDefinitionImpl(compAction);
            }
            else
            {
                subDefinition = new RenditionDefinitionImpl(action);
            }
            addAction(subDefinition);
        }
    }

    /**
     * @param index
     *            int
     * @param action
     *            RenditionDefinition
     * @see org.alfresco.service.cmr.action.ActionList#addAction(int, org.alfresco.service.cmr.action.Action)
     */
    public void addAction(int index, RenditionDefinition action)
    {
        this.actions.addAction(index, action);
    }

    /**
     * @param action
     *            RenditionDefinition
     * @see org.alfresco.service.cmr.action.ActionList#addAction(org.alfresco.service.cmr.action.Action)
     */
    public void addAction(RenditionDefinition action)
    {
        this.actions.addAction(action);
    }

    /**
     * @param index
     *            int
     * @return RenditionDefinition
     * @see org.alfresco.service.cmr.action.ActionList#getAction(int)
     */
    public RenditionDefinition getAction(int index)
    {
        return this.actions.getAction(index);
    }

    /**
     * @see org.alfresco.service.cmr.action.ActionList#getActions()
     */
    public List<RenditionDefinition> getActions()
    {
        return this.actions.getActions();
    }

    /**
     * @return boolean
     * @see org.alfresco.service.cmr.action.ActionList#hasActions()
     */
    public boolean hasActions()
    {
        return this.actions.hasActions();
    }

    /**
     * @param action
     *            RenditionDefinition
     * @return int
     * @see org.alfresco.service.cmr.action.ActionList#indexOfAction(org.alfresco.service.cmr.action.Action)
     */
    public int indexOfAction(RenditionDefinition action)
    {
        return this.actions.indexOfAction(action);
    }

    /**
     * @param action
     *            RenditionDefinition
     * @see org.alfresco.service.cmr.action.ActionList#removeAction(org.alfresco.service.cmr.action.Action)
     */
    public void removeAction(RenditionDefinition action)
    {
        this.actions.removeAction(action);
    }

    /**
     * @see org.alfresco.service.cmr.action.ActionList#removeAllActions()
     */
    public void removeAllActions()
    {
        this.actions.removeAllActions();
    }

    /**
     * @param index
     *            int
     * @param action
     *            RenditionDefinition
     * @see org.alfresco.service.cmr.action.ActionList#setAction(int, org.alfresco.service.cmr.action.Action)
     */
    public void setAction(int index, RenditionDefinition action)
    {
        this.actions.setAction(index, action);
    }

}

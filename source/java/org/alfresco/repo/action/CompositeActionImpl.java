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

package org.alfresco.repo.action;

import java.util.List;

import org.alfresco.repo.action.executer.CompositeActionExecuter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionList;
import org.alfresco.service.cmr.action.CompositeAction;
import org.alfresco.service.cmr.repository.NodeRef;

/**
 * Composite action implementation
 * 
 * @author Roy Wetherall
 */
public class CompositeActionImpl extends ActionImpl implements CompositeAction
{
    /**
     * Serial version UID
     */
    private static final long serialVersionUID = -5348203599304776812L;

    private final ActionList<Action> actions = new ActionListImpl<Action>();

    /**
     * Constructor
     * 
     * @param id the action id
     */
    public CompositeActionImpl(NodeRef nodeRef, String id)
    {
        super(nodeRef, id, CompositeActionExecuter.NAME);
    }

    /**
     * @param action
     * @see org.alfresco.service.cmr.action.ActionList#addAction(org.alfresco.service.cmr.action.Action)
     */
    public void addAction(Action action)
    {
        this.actions.addAction(action);
    }

    /**
     * @param index
     * @param action
     * @see org.alfresco.service.cmr.action.ActionList#addAction(int,
     *      org.alfresco.service.cmr.action.Action)
     */
    public void addAction(int index, Action action)
    {
        this.actions.addAction(index, action);
    }

    /**
     * @param index
     * @return
     * @see org.alfresco.service.cmr.action.ActionList#getAction(int)
     */
    public Action getAction(int index)
    {
        return this.actions.getAction(index);
    }

    /**
     * @return
     * @see org.alfresco.service.cmr.action.ActionList#getActions()
     */
    public List<Action> getActions()
    {
        return this.actions.getActions();
    }

    /**
     * @return
     * @see org.alfresco.service.cmr.action.ActionList#hasActions()
     */
    public boolean hasActions()
    {
        return this.actions.hasActions();
    }

    /**
     * @param action
     * @return
     * @see org.alfresco.service.cmr.action.ActionList#indexOfAction(org.alfresco.service.cmr.action.Action)
     */
    public int indexOfAction(Action action)
    {
        return this.actions.indexOfAction(action);
    }

    /**
     * @param action
     * @see org.alfresco.service.cmr.action.ActionList#removeAction(org.alfresco.service.cmr.action.Action)
     */
    public void removeAction(Action action)
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
     * @param action
     * @see org.alfresco.service.cmr.action.ActionList#setAction(int,
     *      org.alfresco.service.cmr.action.Action)
     */
    public void setAction(int index, Action action)
    {
        this.actions.setAction(index, action);
    }
}

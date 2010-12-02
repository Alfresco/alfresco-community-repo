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

import java.util.LinkedList;
import java.util.List;

import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionList;

/**
 * @author Nick Smith
 */
public class ActionListImpl<A extends Action> implements ActionList<A>
{
    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -1578631012627795870L;

    /**
     * The action list
     */
    private final List<A> actions;

    public ActionListImpl()
    {
        this.actions = new LinkedList<A>();
    }

    /**
     * @see org.alfresco.service.cmr.action.CompositeAction#hasActions()
     */
    public boolean hasActions()
    {
        return (this.actions.isEmpty() == false);
    }

    /**
     * @see org.alfresco.service.cmr.action.CompositeAction#addAction(org.alfresco.service.cmr.action.Action)
     */
    public void addAction(A action)
    {
        this.actions.add(action);
    }

    /**
     * @see org.alfresco.service.cmr.action.CompositeAction#addAction(int,
     *      org.alfresco.service.cmr.action.Action)
     */
    public void addAction(int index, A action)
    {
        this.actions.add(index, action);
    }

    /**
     * @see org.alfresco.service.cmr.action.CompositeAction#setAction(int,
     *      org.alfresco.service.cmr.action.Action)
     */
    public void setAction(int index, A action)
    {
        this.actions.set(index, action);
    }

    /**
     * @see org.alfresco.service.cmr.action.CompositeAction#indexOfAction(org.alfresco.service.cmr.action.Action)
     */
    public int indexOfAction(A action)
    {
        return this.actions.indexOf(action);
    }

    /**
     * @see org.alfresco.service.cmr.action.CompositeAction#getActions()
     */
    public List<A> getActions()
    {
        return this.actions;
    }

    /**
     * @see org.alfresco.service.cmr.action.CompositeAction#getAction(int)
     */
    public A getAction(int index)
    {
        return this.actions.get(index);
    }

    /**
     * @see org.alfresco.service.cmr.action.CompositeAction#removeAction(org.alfresco.service.cmr.action.Action)
     */
    public void removeAction(A action)
    {
        this.actions.remove(action);
    }

    /**
     * @see org.alfresco.service.cmr.action.CompositeAction#removeAllActions()
     */
    public void removeAllActions()
    {
        this.actions.clear();
    }

}

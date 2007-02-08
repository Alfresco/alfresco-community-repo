/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.alfresco.repo.action;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.action.executer.CompositeActionExecuter;
import org.alfresco.service.cmr.action.Action;
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
	
	/**
	 * The action list
	 */
	private List<Action> actions = new ArrayList<Action>();
	
	/**
	 * Constructor
	 * 
	 * @param id  the action id
	 */
	public CompositeActionImpl(NodeRef nodeRef, String id)
	{
		super(nodeRef, id, CompositeActionExecuter.NAME);
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
	public void addAction(Action action)
	{
		this.actions.add(action);
	}

	/**
	 * @see org.alfresco.service.cmr.action.CompositeAction#addAction(int, org.alfresco.service.cmr.action.Action)
	 */
	public void addAction(int index, Action action)
	{
		this.actions.add(index, action);
	}

	/**
	 * @see org.alfresco.service.cmr.action.CompositeAction#setAction(int, org.alfresco.service.cmr.action.Action)
	 */
	public void setAction(int index, Action action)
	{
		this.actions.set(index, action);
	}

	/**
	 * @see org.alfresco.service.cmr.action.CompositeAction#indexOfAction(org.alfresco.service.cmr.action.Action)
	 */
	public int indexOfAction(Action action)
	{
		return this.actions.indexOf(action);
	}

	/**
	 * @see org.alfresco.service.cmr.action.CompositeAction#getActions()
	 */
	public List<Action> getActions()
	{
		return this.actions;
	}

	/**
	 * @see org.alfresco.service.cmr.action.CompositeAction#getAction(int)
	 */
	public Action getAction(int index)
	{
		return this.actions.get(index);
	}

	/**
	 * @see org.alfresco.service.cmr.action.CompositeAction#removeAction(org.alfresco.service.cmr.action.Action)
	 */
	public void removeAction(Action action)
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

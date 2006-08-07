/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the Mozilla Public License version 1.1 
 * with a permitted attribution clause. You may obtain a
 * copy of the License at
 *
 *   http://www.alfresco.org/legal/license.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
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

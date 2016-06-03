
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
     * @param action Action
     * @see org.alfresco.service.cmr.action.ActionList#addAction(org.alfresco.service.cmr.action.Action)
     */
    public void addAction(Action action)
    {
        this.actions.addAction(action);
    }

    /**
     * @param index int
     * @param action Action
     * @see org.alfresco.service.cmr.action.ActionList#addAction(int,
     *      org.alfresco.service.cmr.action.Action)
     */
    public void addAction(int index, Action action)
    {
        this.actions.addAction(index, action);
    }

    /**
     * @param index int
     * @return Action
     * @see org.alfresco.service.cmr.action.ActionList#getAction(int)
     */
    public Action getAction(int index)
    {
        return this.actions.getAction(index);
    }

    /**
     * @return List<Action>
     * @see org.alfresco.service.cmr.action.ActionList#getActions()
     */
    public List<Action> getActions()
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
     * @param action Action
     * @return int
     * @see org.alfresco.service.cmr.action.ActionList#indexOfAction(org.alfresco.service.cmr.action.Action)
     */
    public int indexOfAction(Action action)
    {
        return this.actions.indexOfAction(action);
    }

    /**
     * @param action Action
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
     * @param index int
     * @param action Action
     * @see org.alfresco.service.cmr.action.ActionList#setAction(int,
     *      org.alfresco.service.cmr.action.Action)
     */
    public void setAction(int index, Action action)
    {
        this.actions.setAction(index, action);
    }
}

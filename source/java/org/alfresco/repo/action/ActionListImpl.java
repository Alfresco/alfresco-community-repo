
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

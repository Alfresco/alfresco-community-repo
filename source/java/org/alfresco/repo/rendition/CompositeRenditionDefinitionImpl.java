
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
 */
public class CompositeRenditionDefinitionImpl extends RenditionDefinitionImpl implements CompositeRenditionDefinition
{
    /**
     * Serial Version UID
     */
    private static final long serialVersionUID = -770880495976834168L;

    private final ActionList<RenditionDefinition> actions = new ActionListImpl<RenditionDefinition>();

    /**
     * @param id - the action id
     * @param renditionName - a unique name for the rendering action
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
     * @param index int
     * @param action RenditionDefinition
     * @see org.alfresco.service.cmr.action.ActionList#addAction(int,
     *      org.alfresco.service.cmr.action.Action)
     */
    public void addAction(int index, RenditionDefinition action)
    {
        this.actions.addAction(index, action);
    }

    /**
     * @param action RenditionDefinition
     * @see org.alfresco.service.cmr.action.ActionList#addAction(org.alfresco.service.cmr.action.Action)
     */
    public void addAction(RenditionDefinition action)
    {
        this.actions.addAction(action);
    }

    /**
     * @param index int
     * @return RenditionDefinition
     * @see org.alfresco.service.cmr.action.ActionList#getAction(int)
     */
    public RenditionDefinition getAction(int index)
    {
        return this.actions.getAction(index);
    }

    /**
     * @return List<RenditionDefinition>
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
     * @param action RenditionDefinition
     * @return int
     * @see org.alfresco.service.cmr.action.ActionList#indexOfAction(org.alfresco.service.cmr.action.Action)
     */
    public int indexOfAction(RenditionDefinition action)
    {
        return this.actions.indexOfAction(action);
    }

    /**
     * @param action RenditionDefinition
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
     * @param index int
     * @param action RenditionDefinition
     * @see org.alfresco.service.cmr.action.ActionList#setAction(int,
     *      org.alfresco.service.cmr.action.Action)
     */
    public void setAction(int index, RenditionDefinition action)
    {
        this.actions.setAction(index, action);
    }

}

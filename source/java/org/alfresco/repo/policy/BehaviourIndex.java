package org.alfresco.repo.policy;

import java.util.Collection;

import org.alfresco.api.AlfrescoPublicApi;

/**
 * Index of Bound Behaviours.
 * 
 * @author David Caruana
 *
 * @param <B>  the type of Binding.
 */
@AlfrescoPublicApi
public interface BehaviourIndex<B extends BehaviourBinding>
{
    /**
     * Gets all bound behaviours
     * 
     * @return  the bound behaviours
     */
    public Collection<BehaviourDefinition> getAll();
    
    /**
     * Gets all bound behaviours for the specified binding.
     * 
     * Note: The index may use any algorithm for determining which behaviours
     *       are returned for the binding e.g. based on hierarchical binding
     *       
     * @param binding  the binding
     * @return  the associated behaviours
     */
    public Collection<BehaviourDefinition> find(B binding);

    /**
     * Add a Behaviour Change Observer.
     * 
     * @param observer  the observer
     */
    public void addChangeObserver(BehaviourChangeObserver<B> observer);
    
    /**
     * Gets the behaviour filter
     *  
     * @return  the behaviour filter
     */
    public BehaviourFilter getFilter();
}

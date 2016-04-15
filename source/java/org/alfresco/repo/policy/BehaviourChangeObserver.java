package org.alfresco.repo.policy;


/**
 * An Observer interface for listening to changes in behaviour bindings.
 * 
 * @author David Caruana
 *
 * @param <B>  The specific type of Behaviour Binding to listen out for.
 */
/*package*/ interface BehaviourChangeObserver<B extends BehaviourBinding>
{
    /**
     * A new binding has been made.
     * 
     * @param binding  the binding
     * @param behaviour  the behaviour attached to the binding
     */
    void addition(B binding, Behaviour behaviour);

    void removal(B binding, Behaviour behaviour);
}

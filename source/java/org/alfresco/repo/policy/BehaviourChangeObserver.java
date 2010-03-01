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
    public void addition(B binding, Behaviour behaviour);
}

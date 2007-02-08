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

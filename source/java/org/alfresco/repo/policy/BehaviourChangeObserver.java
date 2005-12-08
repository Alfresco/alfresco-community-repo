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

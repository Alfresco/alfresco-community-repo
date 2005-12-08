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
 * A Behaviour Binding represents the way in which a Behaviour is bound
 * to a Policy i.e. the key.
 * 
 * @author David Caruana
 *
 */
/*package*/ interface BehaviourBinding
{
    /**
     * Gets a generalised form of the Binding.
     * 
     * For example, if the binding key is hierarchical, return the parent
     * key.
     * 
     * @return  the generalised form (or null, if there isn't one)
     */
    BehaviourBinding generaliseBinding();
}

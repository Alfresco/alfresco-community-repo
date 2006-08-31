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
package org.alfresco.repo.audit.model;

/**
 * This enum defines the type of restriction to apply to filter based on the key node ref.
 * 
 * This restriction can be based upon:
 * 
 * <ol>
 *   <li> The path to the node
 *   <li> The type of the node
 *   <li> The presence of an aspect
 *   <li> The NodeRef of the node
 *   <li> An XPATH expression evaluated in the context of the node with the return tested for the node.
 *        e.g. ".[@cm:content = 'woof']"
 *   <li> A simple value for equality tests given a non node argument
 *   <li> The protocol of the store containing the node
 *   <li> The identifier of the store containing the node
 *   <li> Or no restriction 
 * </ol>
 * 
 * @author Andy Hind
 */
public enum KeyFilterMode
{
    PATH, TYPE, ASPECT, NODE_REF, ALL, XPATH, VALUE, STORE_PROTOCOL, STORE_IDENTIFIER;
    
    public static KeyFilterMode getKeyFilterMode(String value)
    {
        if(value.equalsIgnoreCase("path"))
        {
            return KeyFilterMode.PATH;
        }
        else if(value.equalsIgnoreCase("type"))
        {
            return KeyFilterMode.TYPE;
        }
        else if(value.equalsIgnoreCase("aspect"))
        {
            return KeyFilterMode.ASPECT;
        }
        else if(value.equalsIgnoreCase("node_ref"))
        {
            return KeyFilterMode.NODE_REF;
        }
        else if(value.equalsIgnoreCase("all"))
        {
            return KeyFilterMode.ALL;
        }
        else if(value.equalsIgnoreCase("xpath"))
        {
            return KeyFilterMode.XPATH;
        }
        else if(value.equalsIgnoreCase("value"))
        {
            return KeyFilterMode.VALUE;   
        }
        else if(value.equalsIgnoreCase("store_protocol"))
        {
            return KeyFilterMode.STORE_PROTOCOL;
        }
        else if(value.equalsIgnoreCase("store_identifier"))
        {
            return KeyFilterMode.STORE_IDENTIFIER;
        }
        else
        {
            throw new AuditModelException("Unknown KeyFilterMode: "+value);
        }
    }
}

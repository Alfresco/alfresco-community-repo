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

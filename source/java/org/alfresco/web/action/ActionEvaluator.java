/*
 * Copyright (C) 2005-2007 Alfresco Software Limited.
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have recieved a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */
package org.alfresco.web.action;

import java.io.Serializable;

import org.alfresco.web.bean.repository.Node;

/**
 * Contract supported by all classes that provide dynamic evaluation for a UI action.
 * <p>
 * Evaluators are supplied with a Node instance context object.
 * <p>
 * The evaluator should decide if the action precondition is valid based on the appropriate
 * logic and the properties etc. of the Node context and return the result.
 * 
 * @author Kevin Roast
 */
public interface ActionEvaluator extends Serializable
{
   /**
    * The evaluator should decide if the action precondition is valid based on the appropriate
    * logic and the properties etc. of the Node context and return the result.
    * 
    * @param node    Node context for the action
    * 
    * @return result of whether the action can proceed.
    */
   public boolean evaluate(Node node);
   
   /**
    * The evaluator should decide if the action precondition is valid based on the appropriate
    * logic and the state etc. of the given object and return the result.
    * 
    * @param obj     The object the action is for
    * 
    * @return result of whether the action can proceed.
    */
   public boolean evaluate(Object obj);
}

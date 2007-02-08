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
package org.alfresco.web.action;

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
public interface ActionEvaluator
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
}

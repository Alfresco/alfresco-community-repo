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

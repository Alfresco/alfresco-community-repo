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
package org.alfresco.web.action.evaluator;

import javax.faces.context.FacesContext;

import org.alfresco.web.bean.ml.MultilingualUtils;
import org.alfresco.web.bean.repository.Node;

/**
 * Evaluates whether the new edition wizard action should be visible.
 *
 * The creation of a new edtion implies the deletion of each translation and the creation
 * of new content in the space
 *
 * @author Yanick Pignot
 */
public class NewEditionEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = -7511956951071280506L;

   public boolean evaluate(Node node)
   {
       FacesContext fc = FacesContext.getCurrentInstance();
       return MultilingualUtils.canStartNewEditon(node, fc);
   }
}
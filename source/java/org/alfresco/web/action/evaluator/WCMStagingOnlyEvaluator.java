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

import org.alfresco.repo.avm.AVMNodeConverter;
import org.alfresco.web.bean.repository.Node;
import org.alfresco.web.bean.wcm.AVMUtil;

/**
 * Evaluator to return if an item path is within a staging area sandbox.
 * 
 * @author Gavin Cornwell
 */
public class WCMStagingOnlyEvaluator extends BaseActionEvaluator
{
   private static final long serialVersionUID = -130286568044703852L;

   /**
    * @return true if the item is not locked by another user
    */
   public boolean evaluate(final Node node)
   {
      String path = AVMNodeConverter.ToAVMVersionPath(node.getNodeRef()).getSecond();
      return AVMUtil.isMainStore(AVMUtil.getStoreName(path));
   }
}

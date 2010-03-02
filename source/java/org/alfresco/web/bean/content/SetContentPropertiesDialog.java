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
package org.alfresco.web.bean.content;

import org.alfresco.web.app.AlfrescoNavigationHandler;

/**
 * Bean implementation of the "Set Content Properties" dialog.
 * 
 * @author gavinc
 */
public class SetContentPropertiesDialog extends EditContentPropertiesDialog
{
   private static final long serialVersionUID = -7705362669371767349L;

   @Override
   protected String getDefaultCancelOutcome()
   {
      return super.getDefaultCancelOutcome() + 
             AlfrescoNavigationHandler.OUTCOME_SEPARATOR + 
             "browse";
   }

   @Override
   protected String getDefaultFinishOutcome()
   {
      return super.getDefaultFinishOutcome() + 
             AlfrescoNavigationHandler.OUTCOME_SEPARATOR + 
             "browse";
   }
}

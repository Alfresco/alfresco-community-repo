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
package org.alfresco.web.ui.wcm.component;

import org.alfresco.web.ui.common.component.data.UIRichList;
import org.alfresco.web.ui.common.renderer.data.IRichListRenderer;
import org.alfresco.web.ui.wcm.renderer.AVMListRenderer;

/**
 * @author Kevin Roast
 */
public class UIAVMList extends UIRichList
{
   /**
    * Default constructor
    */
   public UIAVMList()
   {
      setRendererType("org.alfresco.faces.AVMListRenderer");
      
      // instantiate each renderer and add to the list
      IRichListRenderer renderer = new AVMListRenderer.DetailsViewRenderer();
      viewRenderers.put(renderer.getViewModeID(), renderer);
   }
}
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
package org.alfresco.web.ui.common.renderer.data;

import java.io.IOException;

import javax.faces.context.FacesContext;

import org.alfresco.web.ui.common.component.data.UIColumn;
import org.alfresco.web.ui.common.component.data.UIRichList;

/**
 * Contract for implementations capable of rendering the columns for a Rich List
 * component.
 * 
 * @author kevinr
 */
public interface IRichListRenderer
{
   /**
    * Callback executed by the RichList component to render any adornments before
    * the main list rows are rendered. This is generally used to output header items.
    * 
    * @param context       FacesContext
    * @param richList      The parent RichList component
    * @param columns       Array of columns to be shown
    * 
    * @throws IOException
    */
   public void renderListBefore(FacesContext context, UIRichList richList, UIColumn[] columns)
      throws IOException;
   
   /**
    * Callback executed by the RichList component once per row of data to be rendered.
    * The bean used as the current row data is provided, but generally rendering of the
    * column data will be performed by recursively encoding Column child components. 
    * 
    * @param context       FacesContext
    * @param richList      The parent RichList component
    * @param columns       Array of columns to be shown
    * @param row           The data bean for the current row
    * 
    * @throws IOException
    */
   public void renderListRow(FacesContext context, UIRichList richList, UIColumn[] columns, Object row)
      throws IOException;
   
   /**
    * Callback executed by the RichList component to render any adornments after
    * the main list rows are rendered. This is generally used to output footer items.
    * 
    * @param context       FacesContext
    * @param richList      The parent RichList component
    * @param columns       Array of columns to be shown
    * 
    * @throws IOException
    */
   public void renderListAfter(FacesContext context, UIRichList richList, UIColumn[] columns)
      throws IOException;
   
   /**
    * Return the unique view mode identifier that this renderer is responsible for. 
    * 
    * @return Unique view mode identifier for this renderer e.g. "icons" or "details"
    */
   public String getViewModeID();
}

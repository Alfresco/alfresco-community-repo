/*
 * Copyright (C) 2005 Alfresco, Inc.
 *
 * Licensed under the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version
 * 2.1 of the License, or (at your option) any later version.
 * You may obtain a copy of the License at
 *
 *     http://www.gnu.org/licenses/lgpl.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.alfresco.web.bean.wizard;

import javax.faces.context.FacesContext;

import org.alfresco.model.ForumModel;
import org.alfresco.util.GUID;
import org.alfresco.web.bean.repository.Repository;

/**
 * Backing bean for posting forum articles.
 * 
 * @author gavinc
 */
public class NewPostWizard extends CreateContentWizard
{
   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#init()
    */
   @Override
   public void init()
   {
      super.init();
      
      // set up for creating a post instead of HTML
      this.createType = CONTENT_TEXT;
      this.objectType = ForumModel.TYPE_POST.toString();
   }

   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#finish()
    */
   @Override
   public String finish()
   {
      // create appropriate values for filename, title and content type
      this.fileName = GUID.generate() + ".txt";
      this.contentType = Repository.getMimeTypeForFileName(
                  FacesContext.getCurrentInstance(), this.fileName);
      this.title = this.fileName;
      
      return super.finish();
   }
   
   
}

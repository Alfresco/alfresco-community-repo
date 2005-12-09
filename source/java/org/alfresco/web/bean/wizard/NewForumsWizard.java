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

import java.util.List;

import org.alfresco.model.ForumModel;
import org.alfresco.web.ui.common.component.UIListItem;

/**
 * Wizard bean used for creating and editing forums spaces
 * 
 * @author gavinc
 */
public class NewForumsWizard extends NewSpaceWizard
{
   protected List<UIListItem> forumsIcons;

   /**
    * @see org.alfresco.web.bean.wizard.AbstractWizardBean#init()
    */
   public void init()
   {
      super.init();
      
      this.spaceType = ForumModel.TYPE_FORUMS.toString();
   }
}

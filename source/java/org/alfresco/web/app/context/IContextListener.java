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
package org.alfresco.web.app.context;

/**
 * Interface used to allow Beans to register themselves as interested in UI context events.
 * <p>
 * Beans supporting this interface should be register against the UIContextService. Then Beans
 * which wish to indicate that the UI should refresh itself i.e. dump all cached data and settings,
 * call the UIContextService.notifyBeans() to inform all registered instances of the change.
 * <p>
 * Registered beans will also be informed of changes in location, for example when the current
 * space changes or when the user has changed area i.e. from company home to my home.
 * 
 * @author Kevin Roast
 */
public interface IContextListener
{
   /**
    * Method called by UIContextService.notifyBeans() to inform all registered beans that
    * all UI Beans should refresh dump all cached data and settings. 
    */
   public void contextUpdated();
   
   /**
    * Method called by UIContextService.spaceChanged() to inform all registered beans that
    * the current space has changed.
    */
   public void spaceChanged();
   
   /**
    * Method called by UIContextService.areaChanged() to inform all registered beans that
    * the user has changed area i.e. from company home to my home.
    */
   public void areaChanged();
}

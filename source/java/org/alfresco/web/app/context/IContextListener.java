package org.alfresco.web.app.context;

import java.io.Serializable;

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
public interface IContextListener extends Serializable
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

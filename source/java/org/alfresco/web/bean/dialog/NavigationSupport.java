package org.alfresco.web.bean.dialog;

import javax.faces.event.ActionEvent;

/**
 * Interface definition for dialog beans that wish to use the next
 * previous buttons for quick navigation.
 * 
 * @author gavinc
 */
public interface NavigationSupport
{
   public String getCurrentItemId();
   
   public void nextItem(ActionEvent event);
   
   public void previousItem(ActionEvent event);
   
   public String getOutcome();
}

package org.alfresco.web.bean.dialog;

import java.util.List;
import javax.faces.event.ActionEvent;
import org.alfresco.web.ui.common.component.UIListItem;

/**
 * Interface definition for dialog beans that wish to use the view
 * drop down to change the layout of the dialog.
 * 
 * @author gavinc
 */
public interface ChangeViewSupport
{
   public List<UIListItem> getViewItems();
   
   public String getViewMode();
   
   public void setViewMode(String viewMode);
   
   public void viewModeChanged(ActionEvent event);
}

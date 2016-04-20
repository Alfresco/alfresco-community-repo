package org.alfresco.web.bean.dialog;

import java.util.List;
import javax.faces.event.ActionEvent;
import org.alfresco.web.ui.common.component.UIListItem;

/**
 * Interface definition for dialog beans that wish to use the filter
 * drop down to change the contents of the dialog.
 * 
 * @author gavinc
 */
public interface FilterViewSupport
{
   public List<UIListItem> getFilterItems();
   
   public String getFilterMode();
   
   public void setFilterMode(String filterMode);
   
   public void filterModeChanged(ActionEvent event);
}

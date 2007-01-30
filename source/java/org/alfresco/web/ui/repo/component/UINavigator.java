package org.alfresco.web.ui.repo.component;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.FacesEvent;

import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.BrowseBean;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.ajax.NavigatorPluginBean;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.SelfRenderingComponent;
import org.alfresco.web.ui.repo.component.UITree.TreeNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Navigator component that consists of 4 panels representing
 * the main areas of the repository i.e. company home, my home,
 * guest home and my alfresco.
 * <p>
 * Each panel (apart from my alfresco) uses the tree component
 * to allow navigation around that area of the repository.
 * </p>
 * 
 * @author gavinc
 */
public class UINavigator extends SelfRenderingComponent
{
   public static final String COMPONENT_TYPE = "org.alfresco.faces.Navigator";
   
   protected String activeArea;
   
   private static final Log logger = LogFactory.getLog(UINavigator.class);
   private static final String AJAX_URL_START = "/ajax/invoke/" + NavigatorPluginBean.BEAN_NAME;
   private static final String PANEL_ACTION = "panel:";
   private static final int PANEL_SELECTED = 1;
   private static final int NODE_SELECTED = 2;
   
   // ------------------------------------------------------------------------------
   // Component Impl 

   @Override
   public String getFamily()
   {
      return COMPONENT_TYPE;
   }
   
   @Override
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.activeArea = (String)values[1];
   }
   
   @Override
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[2];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.activeArea;
      return values;
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#decode(javax.faces.context.FacesContext)
    */
   public void decode(FacesContext context)
   {
      Map requestMap = context.getExternalContext().getRequestParameterMap();
      String fieldId = getClientId(context);
      String value = (String)requestMap.get(fieldId);

      if (value != null && value.length() != 0)
      {
         if (logger.isDebugEnabled())
            logger.debug("Received post back: " + value);
         
         // work out whether a panel or a node was selected 
         int mode = NODE_SELECTED;
         String item = value;
         if (value.startsWith(PANEL_ACTION))
         {
            mode = PANEL_SELECTED;
            item = value.substring(PANEL_ACTION.length());
         }
         
         // queue an event to be handled later
         NavigatorEvent event = new NavigatorEvent(this, mode, item); 
         this.queueEvent(event);
      }
   }
   
   /**
    * @see javax.faces.component.UIInput#broadcast(javax.faces.event.FacesEvent)
    */
   public void broadcast(FacesEvent event) throws AbortProcessingException
   {
      if (event instanceof NavigatorEvent)
      {
         FacesContext context = FacesContext.getCurrentInstance();
         NavigatorEvent navEvent = (NavigatorEvent)event;
         
         // node or panel selected?
         switch (navEvent.getMode())
         {
            case PANEL_SELECTED:
            {
               String panelSelected = navEvent.getItem();
               
               // a panel was selected, setup the context to make the panel
               // the focus
               NavigationBean nb = (NavigationBean)FacesHelper.getManagedBean(
                     context, NavigationBean.BEAN_NAME);
               if (nb != null)
               {
                  try
                  {
                     if (logger.isDebugEnabled())
                        logger.debug("Selecting panel: " + panelSelected);
                     
                     nb.processToolbarLocation(panelSelected, true);
                  }
                  catch (InvalidNodeRefException refErr)
                  {
                     Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                           FacesContext.getCurrentInstance(), Repository.ERROR_NOHOME), 
                           Application.getCurrentUser(context).getHomeSpaceId()), refErr );
                  }
                  catch (Exception err)
                  {
                     Utils.addErrorMessage(MessageFormat.format(Application.getMessage(
                           FacesContext.getCurrentInstance(), Repository.ERROR_GENERIC), 
                           err.getMessage()), err);
                  }
               }
               
               break;
            }
            case NODE_SELECTED:
            {
               // a node was clicked in the tree
               NodeRef nodeClicked = new NodeRef(navEvent.getItem());
               
               // setup the context to make the node the current node
               BrowseBean bb = (BrowseBean)FacesHelper.getManagedBean(
                     context, BrowseBean.BEAN_NAME);
               if (bb != null)
               {
                  if (logger.isDebugEnabled())
                     logger.debug("Selected node: " + nodeClicked);
                  
                  bb.clickSpace(nodeClicked);
               }
               
               break;
            }
         }
      }
      else
      {
         super.broadcast(event);
      }
   }
   
   @Override
   @SuppressWarnings("unchecked")
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (!isRendered()) return;
      
      // TODO: pull width and height from user preferences and/or the main config,
      //       if present override below using the style attribute
      
      ResponseWriter out = context.getResponseWriter();
      NavigationBean navBean = (NavigationBean)FacesHelper.getManagedBean(
            context, NavigationBean.BEAN_NAME);
      NavigatorPluginBean navPluginBean = (NavigatorPluginBean)FacesHelper.getManagedBean(
            context, NavigatorPluginBean.BEAN_NAME);
      
      List<TreeNode> rootNodesForArea = null;
      String area = this.getActiveArea();
      String areaTitle = null;
      boolean treePanel = true;
      if (NavigationBean.LOCATION_COMPANY.equals(area))
      {
         rootNodesForArea = navPluginBean.getCompanyHomeRootNodes();
         areaTitle = Application.getMessage(context, NavigationBean.MSG_COMPANYHOME);
      }
      else if (NavigationBean.LOCATION_HOME.equals(area))
      {
         rootNodesForArea = navPluginBean.getMyHomeRootNodes();
         areaTitle = Application.getMessage(context, NavigationBean.MSG_MYHOME);
      }
      else if (NavigationBean.LOCATION_GUEST.equals(area))
      {
         rootNodesForArea = navPluginBean.getGuestHomeRootNodes();
         areaTitle = Application.getMessage(context, NavigationBean.MSG_GUESTHOME);
      }
      else
      {
         treePanel = false;
         areaTitle = Application.getMessage(context, NavigationBean.MSG_MYALFRESCO);
      }
      
      // main container div
      out.write("<div id=\"navigator\" class=\"navigator\">");
      
      // generate the active panel title
      String cxPath = context.getExternalContext().getRequestContextPath();
      out.write("<div class=\"sidebarButtonSelected\" style=\"background-image: url(" + cxPath + "/images/parts/navigator_blue_gradient_bg.gif)\">");
      out.write("<a class='sidebarButtonSelectedLink' onclick=\"");
      out.write(Utils.generateFormSubmit(context, this, getClientId(context), PANEL_ACTION + area));
      out.write("\" href=\"#\">");
      out.write(areaTitle);
      out.write("</a></div>");
      
      // generate the javascript method to capture the tree node click events
      if (treePanel)
      {
         out.write("<script type=\"text/javascript\">");      
         out.write("function treeNodeSelected(nodeRef) {");
         out.write(Utils.generateFormSubmit(context, this, getClientId(context), 
               "nodeRef", true, null));
         out.write("}</script>");
         
         // generate the active panel containing the tree
         out.write("<div class=\"navigatorPanelBody\">");
         UITree tree = (UITree)context.getApplication().createComponent(UITree.COMPONENT_TYPE);
         tree.setId("tree");
         tree.setRootNodes(rootNodesForArea);
         tree.setRetrieveChildrenUrl(AJAX_URL_START + ".retrieveChildren?area=" + area);
         tree.setNodeCollapsedUrl(AJAX_URL_START + ".nodeCollapsed?area=" + area);
         tree.setNodeSelectedCallback("treeNodeSelected");
         tree.setNodeCollapsedCallback("informOfCollapse");
         Utils.encodeRecursive(context, tree);
         out.write("</div>");
      }
      
      // generate the closed panel title areas
      String sideBarStyle = "style=\"background-image: url(" + cxPath + "/images/parts/navigator_grey_gradient_bg.gif)\"";
      if (NavigationBean.LOCATION_COMPANY.equals(area) == false &&
          navBean.getCompanyHomeVisible())
      {
         encodeSidebarButton(context, out, sideBarStyle, NavigationBean.LOCATION_COMPANY, NavigationBean.MSG_COMPANYHOME);
      }
      
      if (NavigationBean.LOCATION_HOME.equals(area) == false)
      {
         encodeSidebarButton(context, out, sideBarStyle, NavigationBean.LOCATION_HOME, NavigationBean.MSG_MYHOME);
      }
      
      if (NavigationBean.LOCATION_GUEST.equals(area) == false &&
          navBean.getIsGuest() == false && navBean.getGuestHomeVisible())
      {
         encodeSidebarButton(context, out, sideBarStyle, NavigationBean.LOCATION_GUEST, NavigationBean.MSG_GUESTHOME);
      }
      
      if (NavigationBean.LOCATION_MYALFRESCO.equals(area) == false)
      {
         encodeSidebarButton(context, out, sideBarStyle, NavigationBean.LOCATION_MYALFRESCO, NavigationBean.MSG_MYALFRESCO);
      }
      
      out.write("</div>");
   }

   /**
    * Encode a Sidebar Button DIV with selectable button link 
    * 
    * @param context       FacesContext
    * @param out           ResponseWriter
    * @param sideBarStyle  Inline CSS style to apply to the sidebar button
    * @param location      Toolbar location id
    * @param labelId       Label I18N message id
    */
   private void encodeSidebarButton(FacesContext context, ResponseWriter out, String sideBarStyle,
         String location, String labelId)
      throws IOException
   {
      out.write("<div class=\"sidebarButton\" ");
      out.write(sideBarStyle);
      out.write("><a class='sidebarButtonLink' onclick=\"");
      out.write(Utils.generateFormSubmit(context, this, getClientId(context), PANEL_ACTION + location));
      out.write("\" href=\"#\">");
      out.write(Application.getMessage(context, labelId));
      out.write("</a></div>");
   }
   
   @Override
   public void encodeChildren(FacesContext context) throws IOException
   {
      if (!isRendered()) return;
      
      for (Iterator i=this.getChildren().iterator(); i.hasNext(); /**/)
      {
         UIComponent child = (UIComponent)i.next();
         Utils.encodeRecursive(context, child);
      }
   }
   
   @Override
   public boolean getRendersChildren()
   {
      return true;
   }
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors
   
   /**
    * Returns the active area the navigator component is showing
    * 
    * @return The active area
    */
   public String getActiveArea()
   {
      ValueBinding vb = getValueBinding("activeArea");
      if (vb != null)
      {
         this.activeArea = (String)vb.getValue(getFacesContext());
      }
      
      if (this.activeArea == null)
      {
         this.activeArea = NavigationBean.LOCATION_HOME;
      }
      
      return this.activeArea;
   }
   
   /**
    * Sets the active area for the navigator panel
    * 
    * @param activeArea
    */
   public void setActiveArea(String activeArea)
   {
      this.activeArea = activeArea;
   }
   
   // ------------------------------------------------------------------------------
   // Helper methods
   
   /**
    * Class representing the clicking of a tree node.
    */
   @SuppressWarnings("serial")
   public static class NavigatorEvent extends ActionEvent
   {
      private int mode;
      private String item;
      
      public NavigatorEvent(UIComponent component, int mode, String item)
      {
         super(component);
       
         this.mode = mode;
         this.item = item;
      }
      
      public String getItem()
      {
         return item;
      }
      
      public int getMode()
      {
         return mode;
      }
   }
}

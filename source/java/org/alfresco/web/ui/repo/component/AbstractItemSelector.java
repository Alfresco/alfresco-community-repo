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
package org.alfresco.web.ui.repo.component;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

import javax.faces.component.EditableValueHolder;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.FacesEvent;
import javax.transaction.UserTransaction;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.WebResources;

/**
 * Abstract component to allow the selection of a hierarchical item
 * 
 * @author gavinc
 */
public abstract class AbstractItemSelector extends UIInput
{
   private static final String MSG_GO_UP = "go_up";
   private static final String MSG_OK = "ok";
   private static final String MSG_CANCEL = "cancel";

   private final static String OK_BUTTON = "_ok";
   protected final static String OPTION = "_option";
   
   protected final static int MODE_BEFORE_SELECTION = 0;
   protected final static int MODE_INITIAL_SELECTION = 1;
   protected final static int MODE_DRILLDOWN_SELECTION = 2;
   protected final static int MODE_CONFIRM_SELECTION = 3;
   protected final static int MODE_CANCEL_SELECTION = 4;
   
   /** label to be displayed before a space is selected */
   protected String label = null;
   
   /** cellspacing between options */
   protected Integer spacing = null;
   
   /** what mode the component is in */
   protected int mode = MODE_BEFORE_SELECTION;
   
   /** currently browsing node id */
   protected String navigationId = null;
   
   /** id of the initially selected item, if value is not set */
   protected String initialSelectionId = null;
   
   /** Flag to show whether the component is disabled */
   protected Boolean disabled;
   
   
   // ------------------------------------------------------------------------------
   // Component Impl 
   
   /**
    * Default constructor
    */
   public AbstractItemSelector()
   {
      // set the default renderer
      setRendererType(null);
   }
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public abstract String getFamily();
   
   /**
    * Retrieves the default label to show if none has been defined and nothing has been selected
    * 
    * @return Default label
    */
   public abstract String getDefaultLabel();
   
   /**
    * Retrieves the id of the parent node of the current navigation node
    * 
    * @param context The Faces context
    * @return Id of the parent node or null if the parent is the root
    */
   public abstract String getParentNodeId(FacesContext context);
   
   /**
    * Returns a collection of child associations for the current navigation node 
    * 
    * @param context The Faces context
    * @return The children
    */
   public abstract Collection<NodeRef> getChildrenForNode(FacesContext context);
   
   /**
    * Returns a collection of child associations of the root
    * 
    * @param context The Faces context
    * @return The root options
    */
   public abstract Collection<NodeRef> getRootChildren(FacesContext context);
   
   /**
    * @return The icon image to display next to the item links, or null for no icon 
    */
   public abstract String getItemIcon();
   
   /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.label = (String)values[1];
      this.spacing = (Integer)values[2];
      this.mode = ((Integer)values[3]).intValue();
      this.navigationId = (String)values[4];
      this.initialSelectionId = (String)values[5];
      this.disabled = (Boolean)values[6];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[7];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.label;
      values[2] = this.spacing;
      values[3] = this.mode;
      values[4] = this.navigationId;
      values[5] = this.initialSelectionId;
      values[6] = this.disabled;
      return (values);
   }
   
   /**
    * @see javax.faces.component.UIComponentBase#decode(javax.faces.context.FacesContext)
    */
   public void decode(FacesContext context)
   {
      Map requestMap = context.getExternalContext().getRequestParameterMap();
      String fieldId = getHiddenFieldName();
      String value = (String)requestMap.get(fieldId);
      
      int mode = this.mode;
      if (value != null && value.length() != 0)
      {
         // break up the submitted value into it's parts
         
         // first part is the mode the component is in
         // followed by the id of the selection if we are drilling down
         String id = null;
         int sepIndex = value.indexOf(NamingContainer.SEPARATOR_CHAR);
         if (sepIndex != -1)
         {
            mode = Integer.parseInt(value.substring(0, sepIndex));
            if (value.length() > sepIndex + 1)
            {
               id = value.substring(sepIndex + 1);
            }
         }
         else
         {
            mode = Integer.parseInt(value);
         }
         
         // raise an event so we can pick the changed values up later
         ItemSelectorEvent event = new ItemSelectorEvent(this, mode, id); 
         this.queueEvent(event);
      }
      
      if (mode == MODE_CONFIRM_SELECTION)
      {
         // only bother to check the selection if the mode is set to MODE_AFTER_SELECTION
         // see if a selection has been submitted
         String selection = (String)requestMap.get(getClientId(context) + OPTION);
         if (selection != null && selection.length() != 0)
         {
            ((EditableValueHolder)this).setSubmittedValue(new NodeRef(Repository.getStoreRef(), selection));
         }
      }
   }
   
   /**
    * @see javax.faces.component.UIInput#broadcast(javax.faces.event.FacesEvent)
    */
   public void broadcast(FacesEvent event) throws AbortProcessingException
   {
      if (event instanceof ItemSelectorEvent)
      {
         ItemSelectorEvent spaceEvent = (ItemSelectorEvent)event;
         this.mode = spaceEvent.Mode;
         this.navigationId = spaceEvent.Id;
      }
      else
      {
         super.broadcast(event);
      }
   }

   /**
    * @see javax.faces.component.UIComponentBase#encodeBegin(javax.faces.context.FacesContext)
    */
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (isRendered() == false)
      {
         return;
      }
      
      NodeService service = getNodeService(context);
      
      if (isDisabled())
      {
         // render a read-only view of the selected category (if any)
         ResponseWriter out = context.getResponseWriter();

         // see if there is a current value for the category
         NodeRef nodeRef = (NodeRef)getSubmittedValue();
         if (nodeRef == null)
         {
            Object val = getValue();
            if (val instanceof NodeRef) 
            {
               nodeRef = (NodeRef)val;
            }
            else if (val instanceof String && ((String)val).length() != 0)
            {
               nodeRef = new NodeRef((String)val);
            }
         }
         
         // if there is a value show it's name
         if (nodeRef != null)
         {
            out.write(Repository.getNameForNode(service, nodeRef));
         }
      }
      else
      {
         // render an editable control for selecting items
         String clientId = getClientId(context);
         
         StringBuilder buf = new StringBuilder(512);
         Map attrs = this.getAttributes();
         boolean showValueInHiddenField = false;
         NodeRef value = null;
         
         String image = null;
         if (getItemIcon() != null)
         {
            image = "<span style='padding-right:4px'>" + Utils.buildImageTag(context, getItemIcon(), null, "absmiddle") + "</span>";
         }
         
         switch (this.mode)
         {
            case MODE_BEFORE_SELECTION:
            case MODE_CONFIRM_SELECTION:
            case MODE_CANCEL_SELECTION:
            {
               UserTransaction tx = null;
               try
               {
                  tx = Repository.getUserTransaction(context, true);
                  tx.begin();
                  
                  NodeRef submittedValue = (NodeRef)getSubmittedValue();
                  if (submittedValue != null)
                  {
                     value = submittedValue;
                  }
                  else
                  {
                     Object val = getValue();
                     if (val instanceof NodeRef) 
                     {
                        value = (NodeRef)val;
                     }
                     else if (val instanceof String && ((String)val).length() != 0)
                     {
                        value = new NodeRef((String)val);
                     }
                  }
                  
                  // show just the initial or current selection link
                  String label;
                  if (value == null)
                  {
                     label = getLabel();
                     
                     // if the label is still null get the default from the message bundle
                     if (label == null)
                     {
                        label = getDefaultLabel();
                     }
                  }
                  else
                  {
                     label = Repository.getNameForNode(service, value);
                     showValueInHiddenField = true;
                  }
                  
                  // output surrounding span for style purposes
                  buf.append("<span");
                  if (attrs.get("style") != null)
                  {
                     buf.append(" style=\"")
                        .append(attrs.get("style"))
                        .append('"');
                  }
                  if (attrs.get("styleClass") != null)
                  {
                     buf.append(" class=")
                        .append(attrs.get("styleClass"));
                  }
                  buf.append(">");
                  
                  // rendering as initial selection mode means the sibilings of the selected
                  // item are shown instead of the children on first click in.
                  int theMode = MODE_INITIAL_SELECTION;
                  
                  // if we have an initial selection and no value set the initial one up
                  if (value == null && this.getInitialSelection() != null)
                  {
                     value = new NodeRef(Repository.getStoreRef(), this.getInitialSelection());
                  }
                  
                  // field value is whether we are picking and the current or parent Id value
                  String fieldValue;
                  if (value != null)
                  {
                     fieldValue = encodeFieldValues(theMode, value.getId());
                  }
                  else
                  {
                     fieldValue = encodeFieldValues(theMode, null);
                  }
                  buf.append("<a href='#' onclick=\"");
                  buf.append(Utils.generateFormSubmit(context, this, getHiddenFieldName(), fieldValue));
                  buf.append('"');
                  if (attrs.get("nodeStyle") != null)
                  {
                     buf.append(" style=\"")
                        .append(attrs.get("nodeStyle"))
                        .append('"');
                  }
                  if (attrs.get("nodeStyleClass") != null)
                  {
                     buf.append(" class=")
                        .append(attrs.get("nodeStyleClass"));
                  }
                  buf.append(">")
                     .append(label)
                     .append("</a></span>");
                  
                  tx.commit();
               }
               catch (Throwable err)
               {
                  try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
                  Utils.addErrorMessage(err.getMessage(), err);
               }
               
               break;
            }
            
            case MODE_DRILLDOWN_SELECTION:
            case MODE_INITIAL_SELECTION:
            {
               // show the picker list
               // get the children of the node ref to show
               buf.append("<table border=0 cellspacing=1 cellpadding=1");
               if (attrs.get("style") != null)
               {
                  buf.append(" style=\"")
                     .append(attrs.get("style"))
                     .append('"');
               }
               if (attrs.get("styleClass") != null)
               {
                  buf.append(" class=")
                     .append(attrs.get("styleClass"));
               }
               buf.append(">");
               
               // if we are setting up the initial selection we need to get the
               // parent id of the initial selection so the user can actually see
               // the item when the list is rendered
               if (this.mode == MODE_INITIAL_SELECTION)
               {
                  this.navigationId = getParentNodeId(context);
               }
               
               // render "Go Up" link if not at the root level
               if (this.navigationId != null)
               {
                  // get the id of the parent node of the current navigation node,
                  // null indicates we are at the root level
                  String id = getParentNodeId(context);
                  
                  buf.append("<tr><td></td><td>");
                  
                  String upImage = Utils.buildImageTag(context, WebResources.IMAGE_GO_UP, null, "absmiddle");
                  
                  // render a link to the parent node
                  renderNodeLink(context, id, Application.getMessage(context, MSG_GO_UP), upImage, buf);
                  buf.append("</td></tr>");
               }
               
               String okButtonId = clientId + OK_BUTTON;
               boolean okButtonEnabled = false;
               
               // display the children of the specified navigation node ID
               Collection<NodeRef> childRefs;
               if (this.navigationId != null)
               {
                  // get a list of children for the current navigation node
                  childRefs = getChildrenForNode(context);
               }
               else
               {
                  // no node set - special case to show the initial root items
                  childRefs = getRootChildren(context);
               }
               
               UserTransaction tx = null;
               try
               {
                  tx = Repository.getUserTransaction(context, true);
                  tx.begin();
                  
                  for (NodeRef childRef : childRefs)
                  {
                     // render each child found
                     String childId = childRef.getId();
                     buf.append("<tr><td><input type='radio' name='")
                     .append(clientId).append(OPTION).append("' value='")
                     .append(childId).append("'");
                     if (childId.equals(this.initialSelectionId))
                     {
                        buf.append(" checked");
                        
                        // if any radio buttons are checked, the OK button must start enabled
                        okButtonEnabled = true;
                        
                        // now remove the initial selection as we only need it the first time
                        this.initialSelectionId = null;
                     }
                     buf.append(" onclick=\"javascript:document.getElementById('")
                        .append(okButtonId)
                        .append("').disabled=false;\"");
                     buf.append("/></td><td>");
                     
                     // get the name for the child and output as link
                     NodeRef childNodeRef = new NodeRef(Repository.getStoreRef(), childId);
                     String name = Repository.getNameForNode(service, childNodeRef);
                     renderNodeLink(context, childId, name, image, buf);
                     buf.append("</td></tr>");
                  }
                  
                  // render OK button
                  String fieldValue = encodeFieldValues(MODE_CONFIRM_SELECTION, null);
                  buf.append("<tr style='padding-top:4px'><td></td><td align=center>")
                     .append("<input type='button' ")
                     .append(okButtonEnabled == false ? "disabled" : "") 
                     .append(" onclick=\"")
                     .append(Utils.generateFormSubmit(context, this, getHiddenFieldName(), fieldValue))
                     .append("\" value='")
                     .append(Application.getMessage(context, MSG_OK))
                     .append("' id='")
                     .append(okButtonId)
                     .append("'>&nbsp;");
                  
                  // render Cancel button
                  fieldValue = encodeFieldValues(MODE_CANCEL_SELECTION, null);
                  buf.append("<input type='button' onclick=\"")
                     .append(Utils.generateFormSubmit(context, this, getHiddenFieldName(), fieldValue))
                     .append("\" value='")
                     .append(Application.getMessage(context, MSG_CANCEL))
                     .append("'></td></tr>");
                  
                  buf.append("</table>");
                  
                  tx.commit();
               }
               catch (Throwable err)
               {
                  try { if (tx != null) {tx.rollback();} } catch (Exception tex) {}
                  throw new RuntimeException(err);
               }
               
               break;
            }
         }
         
         // output a hidden field containing the currently selected NodeRef so that JavaScript
         // can be used to check the state of the component
         buf.append("<input type='hidden' name='");
         buf.append(clientId);
         buf.append("_selected' id='");
         buf.append(clientId);
         buf.append("_selected' value='");
         if (showValueInHiddenField)
         {
            buf.append(value);
         }
         buf.append("'/>");
         
         context.getResponseWriter().write(buf.toString());
      }
   }
   
   
   // ------------------------------------------------------------------------------
   // Strongly typed component property accessors
   
   /**
    * @return Returns the label.
    */
   public String getLabel()
   {
      ValueBinding vb = getValueBinding("label");
      if (vb != null)
      {
         this.label = (String)vb.getValue(getFacesContext());
      }
      
      return this.label;
   }
   
   /**
    * @param label The label to set.
    */
   public void setLabel(String label)
   {
      this.label = label;
   }

   /**
    * @return Returns the cell spacing value between space options. Default is 2.
    */
   public Integer getSpacing()
   {
      ValueBinding vb = getValueBinding("spacing");
      if (vb != null)
      {
         this.spacing = (Integer)vb.getValue(getFacesContext());
      }
      
      if (this.spacing != null)
      {
         return this.spacing.intValue();
      }
      else
      {
         // return default
         return 2;
      }
   }
   
   /**
    * @param spacing The spacing to set.
    */
   public void setSpacing(Integer spacing)
   {
      this.spacing = spacing;
   }
   
   /**
    * @return Returns the initial selecttion.
    */
   public String getInitialSelection()
   {
      ValueBinding vb = getValueBinding("initialSelection");
      if (vb != null)
      {
         this.initialSelectionId = (String)vb.getValue(getFacesContext());
      }
      
      return this.initialSelectionId;
   }
   
   /**
    * @param initialSelection The initial selection to set.
    */
   public void setInitialSelection(String initialSelection)
   {
      this.initialSelectionId = initialSelection;
   }
   
   /**
    * Determines whether the component should be rendered in a disabled state
    * 
    * @return Returns whether the component is disabled
    */
   public boolean isDisabled()
   {
      if (this.disabled == null)
      {
         ValueBinding vb = getValueBinding("disabled");
         if (vb != null)
         {
            this.disabled = (Boolean)vb.getValue(getFacesContext());
         }
      }
      
      if (this.disabled == null)
      {
         this.disabled = Boolean.FALSE;
      }
      
      return this.disabled;
   }

   /**
    * Determines whether the component should be rendered in a disabled state
    * 
    * @param disabled true to disable the component
    */
   public void setDisabled(boolean disabled)
   {
      this.disabled = disabled;
   }
   
   
   // ------------------------------------------------------------------------------
   // Protected helpers
   
   /**
    * We use a unique hidden field name based on our client Id.
    * This is on the assumption that there won't be many selectors on screen at once!
    * Also means we have less values to decode on submit.
    * 
    * @return hidden field name
    */
   protected String getHiddenFieldName()
   {
      return this.getClientId(getFacesContext());
   }
   
   protected String encodeFieldValues(int mode, String id)
   {
      if (id != null)
      {
         return Integer.toString(mode) + NamingContainer.SEPARATOR_CHAR + id;
      }
      else
      {
         return Integer.toString(mode);
      }
   }
   
   /**
    * Render a node descendant as a clickable link
    * 
    * @param context    FacesContext
    * @param childRef   The ChildAssocRef of the child to render an HTML link for
    *  
    * @return HTML for a descendant link
    */
   protected String renderNodeLink(FacesContext context, String id, String name, String prefix, StringBuilder buf)
   {
      buf.append("<a href='#' onclick=\"");
      String fieldValue = encodeFieldValues(MODE_DRILLDOWN_SELECTION, id);
      buf.append(Utils.generateFormSubmit(context, this, getHiddenFieldName(), fieldValue));
      buf.append('"');
      Map attrs = this.getAttributes();
      if (attrs.get("nodeStyle") != null)
      {
         buf.append(" style=\"")
            .append(attrs.get("nodeStyle"))
            .append('"');
      }
      if (attrs.get("nodeStyleClass") != null)
      {
         buf.append(" class=")
            .append(attrs.get("nodeStyleClass"));
      }
      buf.append('>');
      
      if (prefix != null)
      {
         buf.append(prefix);
      }
      buf.append(Utils.encode(name));
      
      buf.append("</a>");
      
      return buf.toString();
   }
   
   /**
    * Use Spring JSF integration to return the Node Service bean instance
    * 
    * @param context    FacesContext
    * 
    * @return Node Service bean instance or throws exception if not found
    */
   protected static NodeService getNodeService(FacesContext context)
   {
      NodeService service = Repository.getServiceRegistry(context).getNodeService();
      if (service == null)
      {
         throw new IllegalStateException("Unable to obtain NodeService bean reference.");
      }
      
      return service;
   }
   
   /**
    * Use Spring JSF integration to return the Dictionary Service bean instance
    * 
    * @param context    FacesContext
    * 
    * @return Dictionary Service bean instance or throws exception if not found
    */
   protected static DictionaryService getDictionaryService(FacesContext context)
   {
      DictionaryService service = Repository.getServiceRegistry(context).getDictionaryService();
      if (service == null)
      {
         throw new IllegalStateException("Unable to obtain DictionaryService bean reference.");
      }
      
      return service;
   }
   
   /**
    * Class representing the clicking of a breadcrumb element.
    */
   public static class ItemSelectorEvent extends ActionEvent
   {
      public ItemSelectorEvent(UIComponent component, int mode, String id)
      {
         super(component);
         Mode = mode;
         Id = id;
      }
      
      public int Mode;
      private String Id;
   }
}

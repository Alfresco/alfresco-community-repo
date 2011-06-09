/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package org.alfresco.web.ui.repo.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.faces.component.UIInput;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.el.ValueBinding;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.search.impl.lucene.AbstractLuceneQueryParser;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.LimitBy;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchParameters;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.web.app.Application;
import org.alfresco.web.bean.repository.Repository;
import org.alfresco.web.ui.common.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Component for selecting content from the repository
 * 
 * @author gavinc
 */
public class UIContentSelector extends UIInput
{
   private final static Log logger = LogFactory.getLog(UIContentSelector.class);
   
//   private final static String ACTION_SEPARATOR = ";";
   private final static String ACTION_SEARCH = "0";
   
   private final static String FIELD_CONTAINS = "_contains";
   private final static String FIELD_AVAILABLE = "_available";   
   private final static String MSG_SEARCH = "search";
   
   protected String availableOptionsSize;
   protected Boolean disabled;
   private Boolean multiSelect;
      
   /** List containing the currently available options */
   protected List<NodeRef> availableOptions;
   
   // ------------------------------------------------------------------------------
   // Component implementation
   
   /**
    * @see javax.faces.component.UIComponent#getFamily()
    */
   public String getFamily()
   {
      return "org.alfresco.faces.ContentSelector";
   }
   
   /**
    * Default constructor
    */
   public UIContentSelector()
   {
      setRendererType(null);
   }
   
   /**
    * @see javax.faces.component.StateHolder#restoreState(javax.faces.context.FacesContext, java.lang.Object)
    */
   @SuppressWarnings("unchecked")
   public void restoreState(FacesContext context, Object state)
   {
      Object values[] = (Object[])state;
      // standard component attributes are restored by the super class
      super.restoreState(context, values[0]);
      this.availableOptions = (List<NodeRef>)values[1];
      this.availableOptionsSize = (String)values[2];
      this.disabled = (Boolean)values[3];
      this.multiSelect = (Boolean)values[4];
   }
   
   /**
    * @see javax.faces.component.StateHolder#saveState(javax.faces.context.FacesContext)
    */
   public Object saveState(FacesContext context)
   {
      Object values[] = new Object[10];
      // standard component attributes are saved by the super class
      values[0] = super.saveState(context);
      values[1] = this.availableOptions;
      values[2] = this.availableOptionsSize;
      values[3] = this.disabled;
      values[4] = this.multiSelect;
      
      return (values);
   }

   /**
    * @see javax.faces.component.UIComponent#decode(javax.faces.context.FacesContext)
    */
   public void decode(FacesContext context)
   {
      Map<?, ?> requestMap = context.getExternalContext().getRequestParameterMap();
      Map<?, ?> valuesMap = context.getExternalContext().getRequestParameterValuesMap();
      String fieldId = getHiddenFieldName();
      String value = (String)requestMap.get(fieldId);
      
      if (value != null && value.equals(ACTION_SEARCH))
      {
         // user has issued a search action, fill the list with options
         String contains = (String)requestMap.get(fieldId + FIELD_CONTAINS);
         this.availableOptions = new ArrayList<NodeRef>();
         getAvailableOptions(FacesContext.getCurrentInstance(), contains);
      }
      else
      {
         // set the submitted value (if there is one)
         String[] addedItems = (String[])valuesMap.get(fieldId + FIELD_AVAILABLE);
         this.setSubmittedValue(addedItems);
      }
   }
   
   /**
    * @see javax.faces.component.UIComponent#encodeBegin(javax.faces.context.FacesContext)
    */
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (isRendered() == false)
      {
         return;
      }
      
      ResponseWriter out = context.getResponseWriter();

      // get the child associations currently on the node and any that have been added
      NodeService nodeService = Repository.getServiceRegistry(context).getNodeService();
         
      if (isDisabled())
      {
         // TODO: if the component is disabled just show the current value as text
      }
      else
      {
         // start outer table
         out.write("<table border='0' cellspacing='4' cellpadding='0' class='");
         if (this.getAttributes().get("styleClass") != null)
         {
            out.write((String)this.getAttributes().get("styleClass"));
         }
         else
         {
            out.write("selector");
         }
         out.write("'");
         
         if (this.getAttributes().get("style") != null)
         {
            out.write(" style='");
            out.write((String)this.getAttributes().get("style"));
            out.write("'");
         }
         out.write(">");
      
         // show the search field
         out.write("<tr><td colspan='2'><input type='text' maxlength='1024' size='32' name='");
         out.write(getClientId(context) + FIELD_CONTAINS);
         out.write("'/>&nbsp;&nbsp;<input type='submit' value='");
         out.write(Application.getMessage(context, MSG_SEARCH));
         out.write("' onclick=\"");
         out.write(generateFormSubmit(context, ACTION_SEARCH));
         out.write("\"/></td></tr>");
         
         // show available options i.e. all content in repository 
         renderAvailableOptions(context, out, nodeService);
         
         // close table
         out.write("</table>");
      }
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
   
   /**
    * Returns the size of the select control when multiple items
    * can be selected 
    * 
    * @return The size of the select control
    */
   public String getAvailableOptionsSize()
   {
      if (this.availableOptionsSize == null)
      {
         this.availableOptionsSize = "6";
      }
      
      return this.availableOptionsSize;
   }
   
   /**
    * Sets the size of the select control used when multiple items can
    * be selected 
    * 
    * @param availableOptionsSize The size
    */
   public void setAvailableOptionsSize(String availableOptionsSize)
   {
      this.availableOptionsSize = availableOptionsSize;
   }
   
   /**
    * @return true if multi select should be enabled.
    */
   public boolean getMultiSelect()
   {
      ValueBinding vb = getValueBinding("multiSelect");
      if (vb != null)
      {
         this.multiSelect = (Boolean)vb.getValue(getFacesContext());
      }
      
      return multiSelect != null ? multiSelect.booleanValue() : true;
   }

   /**
    * @param multiSelect Flag to determine whether multi select is enabled
    */
   public void setMultiSelect(boolean multiSelect)
   {
      this.multiSelect = Boolean.valueOf(multiSelect);
   }
   
   /**
    * Renders the list of available options
    * 
    * @param context FacesContext
    * @param out Writer to write output to
    * @param nodeService The NodeService
    * @throws IOException
    */
   protected void renderAvailableOptions(FacesContext context, ResponseWriter out, NodeService nodeService) 
      throws IOException
   {
      boolean itemsPresent = (this.availableOptions != null && this.availableOptions.size() > 0);
      
      out.write("<tr><td colspan='2'><select ");
      if (itemsPresent == false)
      {
         // rather than having a very slim select box set the width if there are no results
         out.write("style='width:240px;' ");
      }
      out.write("name='");
      out.write(getClientId(context) + FIELD_AVAILABLE);
      out.write("' size='");
      if (getMultiSelect())
      {
         out.write(getAvailableOptionsSize());
         out.write("' multiple");
      }
      else
      {
         out.write("1'");
      }
      out.write(">");
      
      if (itemsPresent)
      {
         for (NodeRef item : this.availableOptions)
         {
            out.write("<option value='");
            out.write(item.toString());
            out.write("'>");
            out.write(Utils.encode(Repository.getDisplayPath(nodeService.getPath(item))));
            out.write("/");
            out.write(Utils.encode(Repository.getNameForNode(nodeService, item)));
            out.write("</option>");
         }
      }
      
      out.write("</select></td></tr>");
   }   
   
   /**
    * Retrieves the available options for the current association
    * 
    * @param context Faces Context
    * @param contains The contains part of the query
    */
   protected void getAvailableOptions(FacesContext context, String contains)
   {
      // query for all content in the current repository
      StringBuilder query = new StringBuilder("+TYPE:\"");
      query.append(ContentModel.TYPE_CONTENT);
      query.append("\"");
      
      if (contains != null && contains.length() > 0)
      {
    	 String safeContains = AbstractLuceneQueryParser.escape(contains.trim());
         query.append(" AND +@");
         
         String nameAttr = Repository.escapeQName(QName.createQName(
                  NamespaceService.CONTENT_MODEL_1_0_URI, "name"));
         query.append(nameAttr);
         
         query.append(":\"*" + safeContains + "\"*");
      }

      int maxResults = Application.getClientConfig(context).getSelectorsSearchMaxResults();
      
      if (logger.isDebugEnabled())
      {
         logger.debug("Query: " + query.toString());
         logger.debug("Max results size: " + maxResults);
      }
      
      // setup search parameters, including limiting the results
      SearchParameters searchParams = new SearchParameters();
      searchParams.addStore(Repository.getStoreRef());
      searchParams.setLanguage(SearchService.LANGUAGE_LUCENE);
      searchParams.setQuery(query.toString());
      if (maxResults > 0)
      {
         searchParams.setLimit(maxResults);
         searchParams.setLimitBy(LimitBy.FINAL_SIZE);
      }
      
      ResultSet results = null;
      try
      {
         results = Repository.getServiceRegistry(context).getSearchService().query(searchParams);
         this.availableOptions = results.getNodeRefs();
      }
      finally
      {
         if (results != null)
         {
            results.close();
         }
      }
      
      if (logger.isDebugEnabled())
         logger.debug("Found " + this.availableOptions.size() + " available options");
   }
   
   /**
    * We use a hidden field per picker instance on the page.
    * 
    * @return hidden field name
    */
   private String getHiddenFieldName()
   {
      return getClientId(getFacesContext());
   }
   
   /**
    * Generate FORM submit JavaScript for the specified action
    *  
    * @param context    FacesContext
    * @param action     Action string
    * 
    * @return FORM submit JavaScript
    */
   private String generateFormSubmit(FacesContext context, String action)
   {
      return Utils.generateFormSubmit(context, this, getHiddenFieldName(), action);
   }
}

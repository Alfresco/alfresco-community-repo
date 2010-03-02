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
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.web.scripts.bean.SearchProxy;
import org.alfresco.repo.web.scripts.config.OpenSearchConfigElement;
import org.alfresco.repo.web.scripts.config.OpenSearchConfigElement.EngineConfig;
import org.alfresco.web.app.Application;
import org.alfresco.web.ui.common.Utils;
import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigService;
import org.springframework.extensions.webscripts.ui.common.component.SelfRenderingComponent;
import org.springframework.web.jsf.FacesContextUtils;

/**
 * JSF component that provides an OpenSearch client, the engines
 * searched are configured via the web api config.
 * 
 * @author gavinc
 */
public class UIOpenSearch extends SelfRenderingComponent
{
   protected final static String SCRIPTS_WRITTEN = "_alfOpenSearchScripts";
   protected final static String ENGINE_ID_PREFIX = "eng";
   
   // ------------------------------------------------------------------------------
   // Component Impl 

   @Override
   public String getFamily()
   {
      return "org.alfresco.faces.OpenSearch";
   }
   
   @Override
   @SuppressWarnings("unchecked")
   public void encodeBegin(FacesContext context) throws IOException
   {
      if (!isRendered()) return;
      
      ResponseWriter out = context.getResponseWriter();
      
      List<OpenSearchEngine> engines = getRegisteredEngines(context);
      if (engines != null && engines.size() == 0)
      {
         out.write(Application.getMessage(context, "no_engines_registered"));
         return;
      }
      
      String clientId = this.getId();
      
      // write out the JavaScript specific to the OpenSearch component,
      // make sure it's only done once
      Object present = context.getExternalContext().getRequestMap().get(SCRIPTS_WRITTEN);
      if (present == null)
      {
         out.write("<link rel=\"stylesheet\" href=\"");
         out.write(context.getExternalContext().getRequestContextPath());
         out.write("/css/opensearch.css\" type=\"text/css\">");         
         out.write("<script type=\"text/javascript\" src=\"");
         out.write(context.getExternalContext().getRequestContextPath());
         out.write("/scripts/ajax/opensearch.js\"></script>");
         
         context.getExternalContext().getRequestMap().put(SCRIPTS_WRITTEN, Boolean.TRUE);
      }
      
      // we use summary info panel pop-ups so need scripts for that object
      UINodeInfo.outputNodeInfoScripts(context, out);
      
      // write out the javascript initialisation required
      out.write("<script type='text/javascript'>\n");
      out.write("var ");
      out.write(clientId);
      out.write(" = new Alfresco.OpenSearchClient('");
      out.write(clientId);
      out.write("');\n");
      // ADB-133: Synchronizing lengths of search fields
      final Integer searchMinimum = Application.getClientConfig(FacesContext.getCurrentInstance()).getSearchMinimum();
      // register the engines on the client
      for (OpenSearchEngine engine : engines)
      {
         out.write(clientId);
         out.write(".registerOpenSearchEngine('");
         out.write(engine.getId());
         out.write("', '");
         out.write(engine.getLabel());
         out.write("', '");
         out.write(engine.getUrl());
         out.write("', ");
         out.write(searchMinimum.toString());
         out.write(");\n");
      }
      
      // pass in NLS strings
      out.write(clientId);
      out.write(".setMsgNoResults(\"");
      out.write(Application.getMessage(context, "no_results"));
      out.write("\");\n");
      
      out.write(clientId);
      out.write(".setMsgOf(\"");
      out.write(Application.getMessage(context, "of"));
      out.write("\");\n");
      
      out.write(clientId);
      out.write(".setMsgFailedGenerateUrl(\"");
      out.write(Application.getMessage(context, "failed_gen_url"));
      out.write("\");\n");
      
      out.write(clientId);
      out.write(".setMsgFailedSearch(\"");
      out.write(Application.getMessage(context, "failed_search"));
      out.write("\");\n");
      
      out.write(clientId);
      out.write(".setMsgFirstPage(\"");
      out.write(Application.getMessage(context, "first_page"));
      out.write("\");\n");
      
      out.write(clientId);
      out.write(".setMsgPreviousPage(\"");
      out.write(Application.getMessage(context, "prev_page"));
      out.write("\");\n");
      
      out.write(clientId);
      out.write(".setMsgNextPage(\"");
      out.write(Application.getMessage(context, "next_page"));
      out.write("\");\n");
      
      out.write(clientId);
      out.write(".setMsgLastPage(\"");
      out.write(Application.getMessage(context, "last_page"));
      out.write("\");\n");
         
      out.write(clientId);
      out.write(".setMsgInvalidTermLength(\"");
      out.write(Application.getMessage(context, "search_minimum"));
      out.write("\");\n");

      out.write("</script>\n");
      
      // write out the HTML
      String styleClass = (String)this.getAttributes().get("styleClass");
      String style = (String)this.getAttributes().get("style");
      
      if (styleClass != null || style != null)
      {
         out.write("<div");
         
         if (styleClass != null && styleClass.length() > 0)
         {
            out.write(" class='");
            out.write(styleClass);
            out.write("'");
         }
         
         if (style != null && style.length() > 0)
         {
            out.write(" style='");
            out.write(style);
            out.write("'");
         }
         
         out.write(">\n");
      }
      
      out.write("<div class='osPanel'><div class='osControls'>");
      out.write("<table border='0' cellpadding='2' cellspacing='0'><tr>");
      out.write("<td><input id='");
      out.write(clientId);
      out.write("-search-term' name='");
      out.write(clientId);
      out.write("-search-term' type='text' size='30' onkeyup='return ");
      out.write(clientId);
      out.write(".handleKeyPress(event);' />");
      out.write("</td><td><img src='");
      out.write(context.getExternalContext().getRequestContextPath());
      out.write("/images/icons/search_icon.gif' style='cursor:pointer' onclick='");
      out.write(clientId);
      out.write(".executeQuery()' title='");
      out.write(Application.getMessage(context, "search"));
      out.write("' /></td></tr></table>\n");
      out.write("<table border='0' cellpadding='2' cellspacing='0' style='margin-top: 2px;'><tr><td><img src='");
      out.write(context.getExternalContext().getRequestContextPath());
      out.write("/images/icons/expanded.gif' style='cursor:pointer' onclick='");
      out.write(clientId);
      out.write(".toggleOptions(this)' class='expanded' title='");
      out.write(Application.getMessage(context, "toggle_options"));
      out.write("' /></td><td><img src='");
      out.write(context.getExternalContext().getRequestContextPath());
      out.write("/images/icons/opensearch_controls.gif' /></td><td>");
      out.write(Application.getMessage(context, "options"));
      out.write("</td></tr></table>\n");
      
      out.write("<div id='");
      out.write(clientId);
      out.write("-os-options' class='osOptions'>");
      out.write(Application.getMessage(context, "show"));
      out.write("<input id='");
      out.write(clientId);
      out.write("-page-size' name='");
      out.write(clientId);
      out.write("-page-size' type='text' value='5' style='width: 25px; margin-left: 5px; margin-right: 5px;' />");
      out.write(Application.getMessage(context, "items_per_page"));
      out.write("<div style='margin-top: 6px; margin-bottom: 4px;'>");
      out.write(Application.getMessage(context, "search_in"));
      out.write(":</div><table border='0' cellpadding='2' cellspacing='0'>");
      for (OpenSearchEngine engine : engines)
      {
         out.write("<tr><td><input id='");
         out.write(clientId);
         out.write("-");
         out.write(engine.getId());
         out.write("-engine-enabled' name='");
         out.write(clientId);
         out.write("-");
         out.write(engine.getId());
         out.write("-engine-enabled' type='checkbox' checked='checked' />");
         out.write("</td><td>");
         out.write(Utils.encode(engine.getLabel()));
         out.write("</td></tr>");
      }
      out.write("</table></div></div>\n");
      
      out.write("<div id='");
      out.write(clientId);
      out.write("-os-results'></div>\n</div>\n");
      
      if (styleClass != null || style != null)
      {
         out.write("</div>\n");
      }
   }
   
   /**
    * Returns a list of OpenSearchEngine objects representing the 
    * registered OpenSearch engines.
    * 
    * @param context Faces context
    * @return List of registered engines
    */
   private List<OpenSearchEngine> getRegisteredEngines(FacesContext context)
   {
      List<OpenSearchEngine> engines = null;
      
      // get the web api config service object from spring
      ConfigService cfgSvc = (ConfigService)FacesContextUtils.
            getRequiredWebApplicationContext(context).getBean("webscripts.config");
      SearchProxy searchProxy = (SearchProxy)FacesContextUtils.
            getRequiredWebApplicationContext(context).getBean("webscript.org.alfresco.repository.search.searchproxy.get");
      if (cfgSvc != null)
      {
         // get the OpenSearch configuration
         Config cfg = cfgSvc.getConfig("OpenSearch");
         OpenSearchConfigElement osConfig = (OpenSearchConfigElement)cfg.
               getConfigElement(OpenSearchConfigElement.CONFIG_ELEMENT_ID);
         if (osConfig != null)
         {
            // generate the the list of engines with a unique for each
            int id = 1;
            engines = new ArrayList<OpenSearchEngine>();
            
            Set<EngineConfig> enginesCfg = osConfig.getEngines();
            for (EngineConfig engineCfg : enginesCfg)
            {
               // resolve engine label
               String label = engineCfg.getLabel();
               String labelId = engineCfg.getLabelId();
               if (labelId != null && labelId.length() > 0)
               {
                  label = Application.getMessage(context, labelId);
               }

               // locate search engine template url of most appropriate response type
               String url = searchProxy.createUrl(engineCfg, MimetypeMap.MIMETYPE_ATOM);
               if (url == null)
               {
                  url = searchProxy.createUrl(engineCfg, MimetypeMap.MIMETYPE_RSS);
               }
                
               if (url != null)
               {
                  if (url.startsWith("/"))
                  {
                     url = context.getExternalContext().getRequestContextPath() + "/wcservice" + url;
                  }
                          
                  // add the engine
                  OpenSearchEngine engine = new OpenSearchEngine(id, label, url);
                  engines.add(engine);
                  
                  // increase the id counter
                  id++;
                }
            }
         }
      }
      
      return engines;
   }
   
   /**
    * Inner class representing a registered OpenSearch engine.
    */
   private class OpenSearchEngine
   {
      private String id;
      private String label;
      private String url;
      
      public OpenSearchEngine(int id, String label, String url)
      {
         this.id = ENGINE_ID_PREFIX + Integer.toString(id);
         this.label = label;
         this.url = url;
      }

      public String getId()
      {
         return id;
      }

      public String getLabel()
      {
         return label;
      }

      public String getUrl()
      {
         return url;
      }
   }
}

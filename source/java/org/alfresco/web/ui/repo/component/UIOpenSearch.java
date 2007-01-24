package org.alfresco.web.ui.repo.component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;

import org.alfresco.config.Config;
import org.alfresco.config.ConfigService;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.web.app.Application;
import org.alfresco.web.config.OpenSearchConfigElement;
import org.alfresco.web.config.OpenSearchConfigElement.EngineConfig;
import org.alfresco.web.ui.common.Utils;
import org.alfresco.web.ui.common.component.SelfRenderingComponent;
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
   protected final static String ATOM_TYPE = "application/atom+xml";
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
      
      String clientId = this.getClientId(context);
      
      // output the scripts required by the component (checks are 
      // made to make sure the scripts are only written once)
      Utils.writeYahooScripts(context, out, null);
      
      // write out the JavaScript specific to the OpenSearch component,
      // again, make sure it's only done once
      Object present = context.getExternalContext().getRequestMap().get(SCRIPTS_WRITTEN);
      if (present == null)
      {
         out.write("<link rel=\"stylesheet\" href=\"");
         out.write(context.getExternalContext().getRequestContextPath());
         out.write("/css/opensearch.css\" type=\"text/css\">\n");         
         out.write("<script type=\"text/javascript\" src=\"");
         out.write(context.getExternalContext().getRequestContextPath());
         out.write("/scripts/ajax/opensearch.js\"> </script>\n");
         
         context.getExternalContext().getRequestMap().put(SCRIPTS_WRITTEN, Boolean.TRUE);
      }
      
      // write out the javascript initialisation required
      out.write("<script type='text/javascript'>\n");
      out.write("setSearchTermFieldId('");
      out.write(clientId);
      out.write("-search-term');\n");
      out.write("setPageSizeFieldId('");
      out.write(clientId);
      out.write("-page-size');\n");
      
      // register the engines on the client
      for (OpenSearchEngine engine : engines)
      {
         out.write("registerOpenSearchEngine('");
         out.write(engine.getId());
         out.write("', '");
         out.write(engine.getLabel());
         out.write("', '");
         out.write(engine.getUrl());
         out.write("');\n");
      }
      out.write("</script>\n");
      
      // write out the HTML
      out.write("<div class='osPanel'>\n");
      out.write("<table border='0' cellpadding='2' cellspacing='0'><tr>");
      out.write("<td><input id='");
      out.write(clientId);
      out.write("-search-term' name='");
      out.write(clientId);
      out.write("-search-term' type='text' size='25' onkeyup='return handleKeyPress(event);' />");
      out.write("</td><td><img src='");
      out.write(context.getExternalContext().getRequestContextPath());
      out.write("/images/icons/search_icon.gif' style='cursor:pointer' onclick='executeQuery()' title='");
      out.write(Application.getMessage(context, "search"));
      out.write("' /></td><td><img src='");
      out.write(context.getExternalContext().getRequestContextPath());
      out.write("/images/icons/collapsed.gif' style='cursor:pointer' onclick='toggleOptions(this)' class='collapsed' title='");
      out.write(Application.getMessage(context, "options"));
      out.write("' /></td></tr></table>\n");
      out.write("<div id='os-options' class='osOptions'>");
      out.write("<input id='");
      out.write(clientId);
      out.write("-page-size' name='");
      out.write(clientId);
      out.write("-page-size' type='text' value='5' style='width: 25px; margin-right: 5px;' />");
      out.write(Application.getMessage(context, "items_per_page"));
      out.write("<div style='margin-top: 6px; margin-bottom: 6px;'>");
      out.write(Application.getMessage(context, "perform_search_in"));
      out.write(":</div><table border='0' cellpadding='2' cellspacing='0'>");
      for (OpenSearchEngine engine : engines)
      {
         out.write("<tr><td><input id='");
         out.write(engine.getId());
         out.write("-engine-enabled' name='");
         out.write(engine.getId());
         out.write("-engine-enabled' type='checkbox' checked='checked' />");
         out.write("</td><td>");
         out.write(engine.getLabel());
         out.write("</td></tr>");
      }
      out.write("</table></div>\n");
      out.write("<div id='os-results'></div>\n</div>\n");
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
            getRequiredWebApplicationContext(context).getBean("web.api.Config");
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
               Map<String, String> urls = engineCfg.getUrls();
               String url = urls.get(MimetypeMap.MIMETYPE_ATOM);
               if (url == null)
               {
                  url = urls.get(MimetypeMap.MIMETYPE_RSS);
               }
                
               if (url != null)
               {
                  if (url.startsWith("/"))
                  {
                     url = context.getExternalContext().getRequestContextPath() + url;
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

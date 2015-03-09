/*
 * Copyright 2005-2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */

/**
 * Repository Admin Console
 * 
 * Common JavaScript library functions.
 * 
 * @author Kevin Roast
 */

/* Admin JavaScript namespace - public functions exposed through this namespace. */
var Admin = Admin || {};

(function() {

   /**
    * Return an indexed array of available Admin console tools.
    * 
    * [
    *    {
    *       id: <script identifier>,
    *       label: <display label>,
    *       group: <group identifier>,
    *       groupLabel: <group display label>,
    *       description: <description>,
    *       selected: <true if currently selected tool>
    *    }
    *    ...
    * ]
    */
   Admin.getConsoleTools = function getConsoleTools(currentToolId)
   {
      // return an map of group->tool[] information
      var toolInfo = {};
      
      // collect the tools required for the Admin Console
      var tools = utils.findWebScripts("AdminConsole");
      
      // process each tool and generate the data so that a label+link can
      // be output by the component template for each tool required
      for (var i = 0; i < tools.length; i++)
      {
         var tool = tools[i],
             id = tool.id,
             scriptName = id.substring(id.lastIndexOf('/') + 1, id.lastIndexOf('.'));
         
         // use the webscript ID to generate message bundle IDs
         var labelId = "admin-console.tool." + scriptName + ".label",
             descId = "admin-console.tool." + scriptName + ".description";
         
         // identify console tool grouping if any
         // simple convention is used to resolve group - last element of the webscript package path after 'admin'
         // for example: org.alfresco.repository.admin.system = system
         // package paths not matching the convention will be placed in the default root group
         // the I18N label is named: admin-console.tool.group.<yourgroupid>
         var group = "",
             groupLabelId = null,
             paths = tool.scriptPath.split('/');
         if (paths[paths.length-2] == "admin")
         {
            // found webscript package grouping
            group = paths[paths.length-1];
            groupLabelId = "admin-console.tool.group." + group;
         }
         
         var info =
         {
            id: scriptName,
            uri: tool.URIs[0],
            label: msg.get(labelId) != labelId ? msg.get(labelId) : tool.shortName,
            group: group,
            groupLabel: group != "" ? (msg.get(groupLabelId) != groupLabelId ? msg.get(groupLabelId) : String(group).replace(/_/g, " ")) : "",
            description: msg.get(descId) != descId ? msg.get(descId) : tool.description,
            selected: (currentToolId == scriptName)
         };
         
         // process family metadata
         var isCommunity = (utils.getRestrictions().licenseMode == "UNKNOWN"),
             index = -1,
             addTool = true,
             familys = tool.familys.toArray();
         for (var f=0; f<familys.length; f++)
         {
            // find the index if specified
            if (familys[f].indexOf("AdminConsole:Index:") !== -1)
            {
               index = parseInt(familys[f].substring("AdminConsole:Index:".length), 10);
            }
            
            // find community only pages
            if (familys[f] == "AdminConsole:Edition:Community")
            {
               addTool = isCommunity;
            }
         }
         
         // only add tools if not filtered from the list
         if (addTool)
         {
            // generate the tool info structure
            if (!toolInfo[group])
            {
               toolInfo[group] = [];
            }
            
            // add to group - with specific index if specified in descriptor
            if (index === -1)
            {
               toolInfo[group].push(info);
            }
            else
            {
               toolInfo[group].splice(index, 0, info);
            }
         }
      }
      
      // convert to simple indexed array now we have completed the group processing
      var toolsArray = [];
      for each (var g in toolInfo)
      {
         toolsArray.push(g);
      }
      toolsArray.sort(function(a,b) {
         return a[0].group > b[0].group ? 1 : -1
      });
      
      return toolsArray;
   }
   
   /**
    * Return the script ID of the default Admin Console tool (first tool indexed in the list)
    */
   Admin.getDefaultTool = function getDefaultTool()
   {
      var tools = Admin.getConsoleTools(),
          tool = tools[0][0];
      return tool.id;
   }
   
   /**
    * Return the URI of the default Admin Console tool (first tool indexed in the list)
    */
   Admin.getDefaultToolURI = function getDefaultTool()
   {
      var tools = Admin.getConsoleTools(),
          tool = tools[0][0];
      return tool.uri;
   }
   
   /**
    * Return an object containing IP metadata about the server instance.
    */
   Admin.getServerMetaData = function getServerMetaData()
   {
      return {
         hostname: utils.hostName,
         hostaddress: utils.hostAddress
      };
   }
   
   Admin.encodeHtml = function encodeHtml(s)
   {
      if (!s)
      {
         return "";
      }
      s = "" + s;
      return s.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#39;");
   }

})();

/**
 * END Repository Admin Console - Common JavaScript library functions.
 */

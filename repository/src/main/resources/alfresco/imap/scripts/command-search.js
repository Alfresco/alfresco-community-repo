<import resource="/Company Home/Data Dictionary/Scripts/command-utils.js">

/**
 *  Resources
 */

/* var templatePath = "/Data Dictionary/Imap Configs/Templates/imap_search_response_text_html.ftl";*/
var errorParameter = "Error: The query parameter is not set!";
var errorXPathNotValid = "Error: The Xpath query is not valid.";
var unknownCommand = "Unknown command";

/**
 *  Globals
 */
var title;
var command;

/**
 * Create content for e-mail in text format
 * 
 * @nodes (Array) ScriptNodes array
 * @return content for e-mail in text format
 */
function createContentTextPlain(nodes)
{
   var content = "Command: " + title + "\n\n";
   for (var i = 0; i < nodes.length; i++)
   {
      content = content + "Name:         " + nodes[i].getName() + "\nUrl:          " + 
      webApplicationContextUrl + nodes[i].getUrl();
      
      if (nodes[i].isDocument)
      {
         content = content + "\nDownload Url: " +
         webApplicationContextUrl + nodes[i].getDownloadUrl();
      }
      
      content = content + "\n\n";
   }
   return content;
}

/**
 * This for possible processing. It need to be investigated.
 * The possible solution is to send a search request into FreeMarker template and let the template do search!
 * @param nodes
 * @return
 */
function createResponseTextHtml(nodes)
{
   var template = companyhome.childByNamePath(templatePath);
   var result;
   if (template != null)
   {
      var args = new Array();
      args["title"] = title;
      args["nodes"] = nodes; /*it does not work; need to investigate how to send this to freemarker processing*/
      args["webApplicationContextUrl"] = webApplicationContextUrl;
      result = document.processTemplate(template, args);
      logger.log("Response template is found. Response body is created using template.");
   }
   else
   {
      result = createContentTextHtml(nodes);
      logger.log("Response template is NOT found. Response is created using default function.");
   }
   return result;
}

/**
 * Create content for e-mail in html format
 * 
 * @nodes (Array) ScriptNodes array
 * @return content for e-mail in html format
 */
function createContentTextHtml(nodes)
{
   var content ="<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 Transitional//EN\">" +
        "<html><head>" +
        "<meta http-equiv=Content-Type content=\"text/html; charset=UTF-8\">" +
        "<style type=\"text/css\">" +
        "* {font-family:Verdana,Arial,sans-serif;font-size:11px;}" +
        ".links {border:1px dotted #555555;border-collapse:collapse;width:99%;}" +
        ".links td {border:1px dotted #555555;padding:5px;}" +
        "</style>" +
        "</head>" +
        "<body>" +
        "<div>" + "Command: " + title + "\n<br/><br/>\n";
   content += "<table class=\"links\">\n";
   content += "<thead align=\"center\">";
   content += "<tr>";
   content += "<td>Name</td>";
   content += "<td>Url</td>";
   content += "<td>Download Url</td>";
   content += "</tr>";
   content += "</thead>\n" + "</div></body></html>";
   
   
   for (var i = 0; i < nodes.length; i++)
   {
      content += "<tr>\n";
      content += "<td>" + nodes[i].getName() + "</td>";
      content += "<td><a href=\"" + webApplicationContextUrl + nodes[i].getUrl() + "\">" + 
      webApplicationContextUrl + nodes[i].getUrl() + "</a></td>";
      content += "<td>&nbsp;";
      if (nodes[i].isDocument)
      {
         content += "<a href=\"" + webApplicationContextUrl + nodes[i].getDownloadUrl() + "\">" + 
         webApplicationContextUrl + nodes[i].getDownloadUrl() + "</a>";
      }
      content += "</td>\n";
      content += "</tr>\n";
   }
   content += "</table>";
   return content;
}

/**
 * Execute search command
 * 
 * @params (string) command parameters
 */
function commandSearch(params)
{
   var store = "workspace://SpacesStore";
   var query;
   var subject = "Search result";
   var type = "lucene";
   var paramArray = params.split(";");
   for (var i = 0; i < paramArray.length; i++)
   {
      var param = paramArray[i].split("=");
      param[0] = param[0].toLowerCase();
      
      switch (param[0])
      {
         case "store":    store = param[1]; break;
         case "query":    query = param[1]; break;
         case "subject": subject = param[1]; break;
         case "type":    type = param[1].toLowerCase(); break;
      }
   } 

   if (query == null)
   {
      createEmail(errorParameter, errorParameter, errorParameter);
      return;
   }

   var nodes;
   
   try
   {
      switch (type)
      {
         case "lucene":
            nodes = search.luceneSearch(store, query);
            break;
         case "xpath":
            var isValid = search.isValidXpathQuery(query);
            if (isValid == true)
            {
               nodes = search.xpathSearch(store, query);
            }
            else
            {
               createEmail(errorXPathNotValid, errorXPathNotValid, errorXPathNotValid);
               return;
            }
            break;
         case "node":
            var node = search.findNode(query);
            if (node == null) break;
            nodes = new Array(node);
            break;
         case "tag":
            nodes = search.tagSearch(store, query);
            break;
      }
   }
   catch (exception)
   {
      createEmail(exception.message, exception.message, "Search Error");
      return;
   }
   
   if (nodes == null || nodes.length == 0)
   {
      var message = "Nothing was found using query: '" + subject + "'.";
      createEmail(message, message, subject);
      return;
   }
   /*createEmail(createContentTextPlain(nodes), createResponseTextHtml(nodes), subject);*/
   createEmail(createContentTextPlain(nodes), createContentTextHtml(nodes), subject);
}
/**
 * Decode subject
 * 
 * @subject (string) subject
 */
function decodeSubject(subject)
{
    var s = new Array();
    s[0] = new Array("\\", "%5c");
    s[1] = new Array("/", "%2f");
    s[2] = new Array("*", "%2a");
    s[3] = new Array("|", "%7c");
    s[4] = new Array(":", "%3a");
    s[5] = new Array("\"", "%22");
    s[6] = new Array("<", "%3c");
    s[7] = new Array(">", "%3e");
    s[8] = new Array("?", "%3f");
    
   for (var i = 0; i < s.length; i++)
   {
      var re = new RegExp(s[i][1], 'g');
      subject = subject.replace(re, s[i][0]);
   }

   return subject;
}


function main()
{
   title = decodeSubject(document.properties["cm:title"]);
   command = title.split("-");
   if (command[0].toLowerCase() == "do")
   {
      if (command[1].toLowerCase() == "search")
      {
         commandSearch(title.substring(4 + command[1].length));
      }
      else
      {
         var message = unknownCommand + ": '" + title + "'";
         createEmail(message, message, message);
      }
   }
   else
   {
      var message = unknownCommand + ": '" + title + "'";
      createEmail(message, message, message);
   }
   
   document.remove();
}


logger.log("Start search command.");
main();
logger.log("End search command.");


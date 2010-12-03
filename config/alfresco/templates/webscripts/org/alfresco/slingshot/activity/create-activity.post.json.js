/**
 * Share Generic Component: post new activity
 */

var m_node = null,
   m_parentNode = null;

/* Posts to the activities service after a Share action */
function postActivity()
{
   var data = {},
      type = null,
      siteId = null,
      title = null,
      appTool = null,
      nodeRef = null,
      parentNodeRef = null;
   
   /*
    * Activity Type
    */
   if (json.has("type"))
   {
      type = json.get("type");
   }
   if (type == null || type.length === 0)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "Activity 'type' parameter missing when posting activity");
      return;
   }

   /*
    * Site
    */
   if (json.has("site"))
   {
      siteId = json.get("site");
   }
   if (siteId == null || siteId.length === 0)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "'site' parameter missing when posting activity");
      return;
   }
   // Check site existence
   if (siteService.getSite(siteId) == null)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "'" + siteId + "' is not a valid site");
      return;
   }
   
   /**
    * NodeRef & ParentNodeRef properties (must have at least one)
    */
   if (json.has("nodeRef"))
   {
      nodeRef = json.get("nodeRef");
      data.nodeRef = nodeRef;
      m_node = search.findNode(nodeRef);
   }
   if (json.has("parentNodeRef"))
   {
      parentNodeRef = json.get("nodeRef");
      data.parentNodeRef = parentNodeRef;
      m_parentNode = search.findNode(parentNodeRef);
   }
   if (nodeRef == null && parentNodeRef == null)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "Must specify either 'nodeRef' or 'parentNodeRef' parameter when posting activity");
      return;
   }

   /**
    * Title property
    */
   if (json.has("title"))
   {
      title = json.get("title");
      data.title = populateTokens(title);
   }
   if (title == null || title.length === 0)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "Activity 'title' parameter missing when posting activity");
      return;
   }

   /**
    * AppTool (optional)
    */
   if (json.has("appTool"))
   {
      appTool = json.get("appTool");
   }

   /**
    * Page and page params (optional)
    */
   if (json.has("page"))
   {
      data.page = populateTokens(json.get("page"));
   }

   try 
   {
      // Log to activity service
      activities.postActivity(type, siteId, appTool, jsonUtils.toJSONString(data));
   }
   catch(e)
   {
      if (logger.isLoggingEnabled())
      {
         logger.log(e);
      }
   }
}

/**
 * Property token substution.
 * Simplified version of YAHOO.lang.substitute()
 *
 * @method populateTokens
 * @param s {string} String containing zero or more tokens of the form {token}
 * <pre>
 *    {cm:name} Node's cm:name property
 *    {cm:name parent} Parent node's cm:name property
 * </pre>
 */
function populateTokens(s)
{
   var i, j, k, key, v, n, meta, saved=[], token, 
      SPACE = ' ', PARENT = 'parent', LBRACE = '{', RBRACE = '}',
      dump, objstr;

   for (;;)
   {
      i = s.lastIndexOf(LBRACE);
      if (i < 0)
      {
         break;
      }
      j = s.indexOf(RBRACE, i);
      if (i + 1 >= j)
      {
         break;
      }

      // Extract key and meta info
      token = s.substring(i + 1, j);
      key = token;
      meta = null;
      k = key.indexOf(SPACE);
      if (k > -1)
      {
         meta = key.substring(k + 1).toLowerCase();
         key = key.substring(0, k);
      }

      // Lookup the value
      n = meta == PARENT ? m_parentNode : m_node;
      v = null;
      if (n != null)
      {
         v = n.properties[key];
      }

      if (v == null)
      {
         // This {block} has no replace string. Save it for later.
         v = "~-" + saved.length + "-~";
         saved[saved.length] = token;
      }

      s = s.substring(0, i) + v + s.substring(j + 1);
   }

    // restore saved {block}s
    for (i = saved.length - 1; i >= 0; i = i - 1)
    {
       s = s.replace(new RegExp("~-" + i + "-~"), "{"  + saved[i] + "}", "g");
    }

    return s;
}

postActivity();
/**
 * A collection of general functions used across most wiki scripts.
 */
const DEFAULT_PAGE_CONTENT = "This is a new page. It has no content";

/**
 * Takes an array of template parameter arguments
 * and returns an object keyed on parameter name.
 */
function getTemplateArgs(params)
{
	var p = {};

	var param;
	for (var i=0; i < params.length; i++)
	{
		param = params[i];
		p[param] =  url.templateArgs[param];
	}

	return p;
}

/* Format and return error object */
function jsonError(errorString)
{
   status.setCode(status.STATUS_BAD_REQUEST, errorString);
}

// NOTE: may need a custom content type for a wiki entry
function createWikiPage(name, folder, options)
{
	var page = folder.createFile(name);
	if (options)
	{
      if (options.content)
      {
         page.content = options.content;
         page.mimetype = "text/html";
      }
		
      if (options.versionable)
      {
         page.addAspect("cm:versionable");
      }
	}
	
	// Initialise tags to empty array
	page.tags = [];
	
	// Set cm:title
	page.properties["cm:title"] = new String(name).replace(/_/g, " ");
	page.save();
		
	return page;
}

function getWikiContainer(site)
{
   var wiki;
   
   if (site.hasContainer("wiki"))
   {
      wiki = site.getContainer("wiki");
   }
   else
   {
      wiki = site.createContainer("wiki");
   }
   
   if(wiki != null)
   {
      if (!wiki.isTagScope)
      {
         wiki.isTagScope = true;
      }
   }
   
   return wiki;
}

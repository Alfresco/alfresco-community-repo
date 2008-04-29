/*
 * blogSpace
 *
 * Inputs:
 *  mandatory: nodeRef = parent space nodeRef
 *  optional:  n = nodeId for document to action against
 *             a = action
 *                 "p" = publish
 *                 "u" = update
 *                 "r" = remove
 *
 * Outputs: blogSpace - object containing node arrays of articles pending and to be updated
 */
var actionResult = parseArgs(args["n"], args["a"]);
model.blogSpace = main(args["nodeRef"], actionResult);

function parseArgs(nodeId, action)
{
   var result = "";
   if ((nodeId != null) && (action != null))
   {
      var blog = actions.create("blog-post");
      
      var node = search.findNode(nodeId);
      if (node != null)
      {
         var blogAction = "";
         var isPublished = false;
         if ((node.hasAspect("blg:blogPost")) && (node.properties["blg:published"] == true))
         {
            isPublished = true;
         }
         
         switch (action)
         {
            case "p":
               blogAction = (isPublished ? "" : "post");
               break;
            case "u":
               blogAction = (isPublished ? "update" : "");
               break;
            case "r":
               blogAction = (isPublished ? "remove" : "");
               break;
         }
         
         if (blogAction != "")
         {
            blog.parameters.action = blogAction;
            blog.execute(node);
            result = blog.parameters["result"];
         }
      }
   }
   return result;
}

function main(nodeRef, actionResult)
{
   var pending = new Array(),
      updates = new Array();

   var article = {};
   var person;

   var space = search.findNode(nodeRef);
   
   if (space != null)
   {
      for each(node in space.children)
      {
         person = people.getPerson(node.properties["cm:creator"]);
         article = 
         {
            "node": node,
            "person": person
         };

         if ((node.hasAspect("blg:blogPost")) && (node.properties["blg:published"] == true))
         {
               if (node.properties["cm:modified"] - node.properties["blg:lastUpdate"] > 5000)
               {
                  updates.push(article);
               }
         }
         else
         {
            pending.push(article);
         }
      }
   }
   
   var blogSpace =
   {
      "actionResult": actionResult,
      "pending": pending,
      "updates": updates
   };
   return blogSpace;
}

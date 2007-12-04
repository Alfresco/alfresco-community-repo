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
 * Outputs: blogSpace - object containing published and pending node arrays
 */
parseArgs(args["n"], args["a"]);
model.blogSpace = main(args["nodeRef"]);

function parseArgs(nodeId, action)
{
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
         }
      }
   }
}

function main(nodeRef)
{
   var published = new Array(),
      pending = new Array();

   var space = search.findNode(nodeRef);
   
   if (space != null)
   {
      for each(node in space.children)
      {
         if ((node.hasAspect("blg:blogPost")) && (node.properties["blg:published"] == true))
         {
            published.push(node);
         }
         else
         {
            pending.push(node);
         }
      }
   }
   
   var blogSpace =
   {
      "published": published,
      "pending": pending
   };
   return blogSpace;
}

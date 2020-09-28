function main ()
{
   var nodeRef = url.templateArgs.store_type + "://" + url.templateArgs.store_id + "/" + url.templateArgs.id,
      node = search.findNode(nodeRef),
      ASPECT_IGNORE_INHERITED_RULES = "rule:ignoreInheritedRules";

   model.nodeRef = nodeRef;

   if (node.hasAspect(ASPECT_IGNORE_INHERITED_RULES) == true)
   {
      node.removeAspect(ASPECT_IGNORE_INHERITED_RULES);
      model.inheritRules = true;
   } else
   {
      node.addAspect(ASPECT_IGNORE_INHERITED_RULES);
      model.inheritRules = false;
   }
};

main();
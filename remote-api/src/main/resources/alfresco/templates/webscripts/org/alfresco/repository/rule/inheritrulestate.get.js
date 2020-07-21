function main ()
{
   var nodeRef = url.templateArgs.store_type + "://" + url.templateArgs.store_id + "/" + url.templateArgs.id,
      node = search.findNode(nodeRef),
      ASPECT_IGNORE_INHERITED_RULES = "rule:ignoreInheritedRules";

   model.nodeRef = nodeRef;

   // Aspect indicates that rules are ignored.
   if (node.hasAspect(ASPECT_IGNORE_INHERITED_RULES) == true)
   {
      model.inheritRules = false;
   } else
   {
      model.inheritRules = true;
   }
};

main();
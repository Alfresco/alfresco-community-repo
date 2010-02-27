<import resource="classpath:alfresco/templates/webscripts/org/alfresco/cmis/lib/read.lib.js">

script:
{
   // locate association
   var rel = getAssocFromUrl();
   if (rel.assoc == null)
   {
       break script;
   }
   model.assoc = rel.assoc;
    
}

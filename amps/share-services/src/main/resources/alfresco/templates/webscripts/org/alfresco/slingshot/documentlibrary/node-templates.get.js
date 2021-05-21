/**
 * Document List Component: Create New Node - get list of available node templates in the Data Dictionary
 */
function main()
{
   var nodes = search.selectNodes('/app:company_home/app:dictionary/app:node_templates/*[subtypeOf("cm:content")]');
   return nodes;
}

model.nodes = main();
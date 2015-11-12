function applyTemplate(context, vanillaJSONx)
{
   var vanillaJSON=""+context.getParameter("vanillaJSON");

   var ctxPlaceholders = context.placeholders;
   var processedVanilla = vanillaJSON;

   for ( var ph in ctxPlaceholders)
   {
      processedVanilla = processedVanilla.replace(new RegExp("%" + ph + "%", "g"), ctxPlaceholders[ph]);
   }

   var propertiesRegExp = new RegExp("<.+?>", "g");

   if (context.actualNode!=null && context.actualNode.properties!=null)
   {
	   var actualProperties = context.actualNode.properties;
	
	   var propertiesPh = processedVanilla.match(propertiesRegExp);
	
	   var processedPh = [];
	
	   if (propertiesPh != null)
	   {
	      propertiesPh.forEach(function(ph)
	      {
	         if (processedPh.indexOf(ph) === -1)
	         {
	            var propertyValue = actualProperties[ph.substring(1, ph.length - 1)];
	            processedVanilla = processedVanilla.replace(new RegExp(ph, "g"), propertyValue);
	            processedPh.push(ph);
	         }
	      });
	   }
   }
   return JSON.parse(processedVanilla);
}

applyTemplate(context, vanillaJSON);
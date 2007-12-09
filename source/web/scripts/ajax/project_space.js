/*
* Prerequisites: mootools.v1.11.js
*/
var ProjectSpace =
{
	init: function()
	{
		$("refreshProjectSpace").addEvent("click", ProjectSpace.updateSummaries);
		ProjectSpace.updateSummaries();
   },
   
   updateSummaries: function()
   {
		var summaries = $$("#projectSummary .projectSpaceSummary");

		summaries.each(function(summary, i)
		{
			var summaryURL = summary.attributes["rel"].value;
			
			if (summaryURL != "")
			{
				// ajax call to load space summary
				var myAjax = new Ajax(summaryURL, {
					method: "get",
					headers: {"If-Modified-Since": "Sat, 1 Jan 2000 00:00:00 GMT"},
					update: summary
				}).request();
			}
		});
   }
}

window.addEvent('domready', ProjectSpace.init);

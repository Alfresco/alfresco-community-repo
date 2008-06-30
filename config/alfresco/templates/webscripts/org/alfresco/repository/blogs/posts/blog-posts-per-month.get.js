<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/requestutils.lib.js">
<import resource="classpath:alfresco/templates/webscripts/org/alfresco/repository/blogs/blogpost.lib.js">


function getBeginOfMonthDate(date)
{
	return new Date(date.getFullYear(), date.getMonth(), 1);
}



function getEndOfMonthDate(date)
{
	var year = date.getFullYear();
	var month = date.getMonth();
	var beginOfNextMonth = new Date(year, month + 1, 1); // will increment year by 1 if month > 11 
	return new Date(beginOfNextMonth.getTime() - 1);
}

/**
 * Creates an object containing information about the month.
 * This object holds all the data returned.
 */
function getMonthDataObject(date)
{
	var data = {};
	data.year = date.getFullYear();
	data.month = date.getMonth();
	data.firstPostInMonth = date;
	data.beginOfMonth = getBeginOfMonthDate(date);
	data.beginOfMonthMillis = data.beginOfMonth.getTime();
	data.endOfMonth = getEndOfMonthDate(date);
	data.endOfMonthMillis = data.endOfMonth.getTime();
	data.count = 1;
	return data;
}

/**
 * Fetches all posts found in the forum.
 */
function getBlogPostMonths(node)
{
	// query information
	var luceneQuery = " +TYPE:\"{http://www.alfresco.org/model/content/1.0}content\"" +
	                    " +PATH:\"" + node.qnamePath + "/*\" ";
	var sortAttribute = "@{http://www.alfresco.org/model/content/1.0}created";
	nodes = search.luceneSearch(node.nodeRef.storeRef.toString(), luceneQuery, sortAttribute, true);
	
	// fetch all dates with different month and year. Throw away doubles.
	var data = new Array();
	
	if (nodes.length > 0) {
		var curr = nodes[0].properties["cm:created"];
		var currData = getMonthDataObject(curr);
		data.push(currData);
		
		for (var x=1; x < nodes.length; x++)
		{
			var date = nodes[x].properties["cm:created"];
			// check whether we are in a new month
			if (curr.getFullYear() != date.getFullYear() || curr.getMonth() != date.getMonth())
			{
				curr = node;
				currData = getMonthDataObject(curr);
				data.push(currData);
			}
			// or still the same one
			else
			{
				currData.count += 1;
			}
		}
	}
	
	return data;
}

function main()
{
	// get requested node
	var node = getRequestNode();
	if (status.getCode() != status.STATUS_OK)
	{
		return;
	}

	// fetch the months
	model.data = getBlogPostMonths(node);
}

main();

function main()
{

// Get the args
var filter = args["filter"];
var maxResults = args["maxResults"];
var skipCountStr = args["skipCount"];
var skipCount = skipCountStr != null ? parseInt(skipCountStr) : -1;
var sortBy = args["sortBy"];
var sortAsc = args["dir"] != "desc";

// Get the collection of people
var paging = utils.createPaging(maxResults != null ? parseInt(maxResults) : 0, skipCount);
var peopleCollection = people.getPeoplePaging(filter, paging, sortBy, sortAsc);

var startIndex = args["startIndex"] != null ? parseInt(args["startIndex"]) : 0;
var pageSize = args["pageSize"] != null ? parseInt(args["pageSize"]) : peopleCollection.length

// Pass the queried sites to the template
model.maxItems = pageSize;
model.totalItems = peopleCollection.length;
model.skipCount = skipCount > 0 ? (skipCount + startIndex) : startIndex;

model.peoplelist = peopleCollection.slice(startIndex, startIndex + pageSize);

}
main();

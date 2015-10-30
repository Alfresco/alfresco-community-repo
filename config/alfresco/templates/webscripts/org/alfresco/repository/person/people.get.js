function main()
{

// Get the args
var filter = args["filter"];
var maxResults = args["maxResults"];
var sortBy = args["sortBy"];
var sortAsc = args["dir"] != "desc";

// Get the sorted collection of people
var peopleCollection = people.getPeople(filter, maxResults != null ? parseInt(maxResults) : -1, sortBy, sortAsc);

var skipCount = args["startIndex"] != null ? parseInt(args["startIndex"]) : 0;
var pageSize = args["pageSize"] != null ? parseInt(args["pageSize"]) : peopleCollection.length

// Pass the queried sites to the template
model.maxItems = pageSize;
model.totalItems = peopleCollection.length;
model.skipCount = skipCount;

model.peoplelist = peopleCollection.slice(skipCount, skipCount + pageSize);

}
main();

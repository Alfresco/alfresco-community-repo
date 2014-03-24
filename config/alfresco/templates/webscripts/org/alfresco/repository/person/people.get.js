// Get the args
var filter = args["filter"];
var maxResults = args["maxResults"];
var sortBy = args["sortBy"];
var sortAsc = args["dir"] != "desc";

// Get the collection of people
var paging = utils.createPaging(maxResults != null ? parseInt(maxResults) : 0, -1);
var peopleCollection = people.getPeoplePaging(filter, paging, sortBy, sortAsc);

// Pass the queried sites to the template
model.peoplelist = peopleCollection;
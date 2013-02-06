// Get the args
var filter = args["filter"];
var maxResults = args["maxResults"];
var sortBy = args["sortBy"];
var sortAsc = args["dir"] != "desc";

// Get the collection of people
var peopleCollection = people.getPeople(filter, maxResults != null ? parseInt(maxResults) : 0, sortBy, sortAsc);

// Pass the queried sites to the template
model.peoplelist = peopleCollection;
// Get the filter query string
var filter = args["filter"];

// Get the collection of people
var peopleCollection = people.getPeople(filter);

// Pass the queried sites to the template
model.people = peopleCollection;

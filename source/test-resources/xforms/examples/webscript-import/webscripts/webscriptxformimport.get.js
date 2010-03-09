// Parse arguments
var storeid = args.storeid;

// Validate arguments
if (storeid == undefined || storeid == "")
{
    status.code     = 400;
    status.message  = "Mandatory query string parameter 'storeid' was not provided.";
    status.redirect = true;
}

// Generate an id (in a truly terrible way, but it's just an example!)
model.storeid = storeid;
model.idValue = Math.floor(Math.random() * 1000);

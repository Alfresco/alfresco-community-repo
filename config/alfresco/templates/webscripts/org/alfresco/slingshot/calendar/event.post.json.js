<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/calendar/lib/calendar.lib.js">

var siteId = null;
if (!json.isNull("site"))
{
   siteId = json.get("site");
}

var params = {};
// Variables we are expecting
var props =
[
   "from",
   "to",
   "what",
   "where",
   "desc",
   "datefrom",
   "start",
   "dateto",
   "end",
   "allday",
   "tags",
   "docfolder"
];

var p;
for (var k = 0, kk = props.length; k < kk; k++)
{
   p = props[k];
   if (!json.isNull(p))
   {
      params[p] = json.get(p);
   }
}

model.result = createEvent(siteId, params);


function createEvent(siteId, params)
{
   if (siteId === null)
   {
      return (
      {
         "error": "Site identifier is undefined"
      });
   }

   var site = siteService.getSite(siteId);
   if (site === null)
   {
      return (
      {
         "error": "Could not find specified site"
      });
   }

   var calendar = getCalendarContainer(site);
   if (calendar === null)
   {
      return (
      {
         "error": "Could not get container"
      });
   }

   var timestamp = new Date().getTime();
   var random = Math.round(Math.random() * 10000);
   var event = calendar.createNode(timestamp + "-" + random + ".ics", "ia:calendarEvent");

   if (event === null)
   {
      return (
      {
         "error": "Could not create event"
      });
   }

   event.properties["ia:whatEvent"] = params["what"];
   event.properties["ia:whereEvent"] = params["where"];
   event.properties["ia:descriptionEvent"] = params["desc"];
   if (params["docfolder"] != "")
   {
   event.properties["ia:docFolder"] = params["docfolder"];
   }

   var fromDate = params["from"];
   var toDate = params["to"];

   var allday = params["allday"];
   if (allday === undefined)
   {
      fromDate += " " + params["start"];
      toDate += " " + params["end"];
      allday = '';
   }

   if (params['tags'])
   {
     var tags = String(params["tags"]); // space delimited string
     if (tags !== "")
     {
        var tagsArray = tags.split(" ");
        if (tagsArray.length > 0)
        {
           event.tags = tagsArray;
        }
     }     
   }

   var from = new Date(fromDate);
   event.properties["ia:fromDate"] = from;

   var to = new Date(toDate);
   event.properties["ia:toDate"] = to;
   event.save();

   try
   {
      var pad = function (value, length)
      {
         value = String(value);
         length = parseInt(length) || 2;
         while (value.length < length)
         {
            value = "0" + value;
         }
         return value;
      };

      var isoDate = from.getFullYear() + "-" + pad(from.getMonth() + 1) + "-" + pad(from.getDate());
      var data =
      {
         title: params["what"],
         page: json.get("page") + "?date=" + isoDate
      }
      activities.postActivity("org.alfresco.calendar.event-created", siteId, "calendar", jsonUtils.toJSONString(data));
   }
   catch(e)
   {
      if (logger.isLoggingEnabled()) 
      {
         logger.log(e);
      }
   }

   return (
   {
      "name": params["what"],
      "from": from,
      "to": to,
      "uri": "calendar/event/" + siteId + "/" + event.name + "?date=" + isoDate,
      "tags": event.tags,
      "desc": params['desc'],
      "where":params['where'],
      "allday":allday,
      "docfolder": params['docfolder']
   });
};
var child1 = companyhome.childByNamePath("/Data Dictionary");
var child2 = companyhome.childByNamePath("/Data Dictionary/Scripts");
var child3 = companyhome.childByNamePath("/Data Dictionary/Scripts/backup and log.js");
logger.getSystem().out(child1 != null && child2 != null && child3 != null);

var parentCount = child1.parentAssocs["contains"].length;

var parents = child2.parents;

var result = (child1 != null && child2 != null && child3 != null && parentCount == 1 && parents.length == 1);
result;
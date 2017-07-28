var results1 = companyhome.childFileFolders();
var results2 = companyhome.childFileFolders(true, false);
var results3 = companyhome.childFileFolders(false, true);
var results4 = companyhome.childFileFolders(false, true, "fm:forums");
var types = new Array();
types.push("fm:forums");
types.push("fm:forum");
var results5 = companyhome.childFileFolders(false, true, types);
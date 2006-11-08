// Main action

function runAction()
{
  var urlStr = webURL + "navigate/showDocDetails/workspace/SpacesStore/" + deskParams.getTarget(0).getNode().getId() +
    "?ticket=" + deskParams.getTicket();

  return urlStr;
}

// Run the action
//
// Response :-
//  Success - no return or return 0, or "0,<message>"
// For error or control response then return a string :-
//  Error   		- "1,<error message>"
//  FileNotFound	- "2,<message>"
//  AccessDenied    - "3,<message>"
//  BadParameter    - "4,<message>
//  NotWorkingCopy  - "5,<message>"
//  NoSuchAction    - "6,<message>
//  LaunchURL       - "7,<URL>"
//  CommandLine     - "8,<commandline>"

var response = "7," + runAction();
response;

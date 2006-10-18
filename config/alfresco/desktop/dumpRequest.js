// Main action

function runAction()
{
  out.println("Dump request details :-");
  out.println("  Folder node = " + deskParams.getFolderNode());
  var folder = deskParams.getFolder();
  out.println("  Folder path = " + folder.getFullName());
  out.println("  Number of targets = " + deskParams.numberOfTargetNodes());

  if ( deskParams.numberOfTargetNodes() > 0) {
    out.println("  Targets:");

    for ( var i = 0; i < deskParams.numberOfTargetNodes(); i++) {
      var target = deskParams.getTarget( i);
      out.println("    Type = " + target.getTypeAsString() + ", path = " + target.getTarget());
      out.println("    Node = " + target.getNode());
    }
  }
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

runAction();
var response = "0,Javascript completed successfully";
response;
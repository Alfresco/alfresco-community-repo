<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
   <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">   
   <meta name="Generator" content="Alfresco Repository">

   <style type="text/css">
      body {
         background-color:#FFFFFF;
         color:#000000;
      }
      * {
         font-family:Verdana,Arial,sans-serif;
         font-size:11px;
      }
      h1 {
         text-align:left;
         font-size:15px;
      }
      h2 {
         text-align:left;
         font-size:13px;
      }
      .links {
         border:0;
         border-collapse:collapse;
         width:99%;
      }
      .links td {
         border:0;
         padding:5px;
      }
      .description {
         border:0;
         border-collapse:collapse;
         width:99%;
      }
      .description td {
         border:1px dotted #555555;
         padding:5px;
      }
   </style>
</head>
<body>
   <h1>Workflow has started successfully.</h1>
   <table class="description">
      <tr>
         <td>Ticket:</td><td>${args.alfTicket}</td>
      </tr>
      <tr>
         <td>NodeRef id:</td><td>${args.nodeRefId}</td>
      </tr>
      <tr>
         <td>Workflow type:</td><td>${args.workflowType}</td>
      </tr>
      <tr>
         <td>Asign to:</td><td>${args.assignTo}</td>
      </tr>
      <tr>
         <td>Due date:</td><td>${args.workflowDueDateDay}/${args.workflowDueDateMonth}/${args.workflowDueDateYear}</td>
      </tr>
      <tr>
         <td>Description:</td><td>${args.description}</td>
      </tr>
      <tr>
         <td>Result:</td><td> --- </td>
      </tr>
   </table>
</body>
</html>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "data":
   {
      "restoredNode":
      {
         "archivedNodeRef": "${restoreNodeReport.archivedNodeRef!""}",
         "restoredNodeRef": "${restoreNodeReport.restoredNodeRef!""}",
         "status": "${restoreNodeReport.status!""}",
         "success": ${restoreNodeReport.status.isSuccess()?string("true", "false")}
      }
   }
}
</#escape>
<#escape x as jsonUtils.encodeJSONString(x)>
{
   "statusString": "${tagActions.resultString}",
   "statusCode": ${tagActions.resultCode?string},
   "newTag": "${tagActions.newTag?string}"
}
</#escape>
<html>
<head>
   <title></title>
</head>
<body>
<#if status.code == 200>
    <#if (args.successCallback?exists)>
       <script type="text/javascript">
          ${args.successCallback}.call(${args.successScope}, {
             status: {
                "code" : ${status.code},
                "name" : "${status.codeName}",
                "description" : "${status.codeDescription}"
             },
             message: "${jsonUtils.encodeJSONString(status.message)}"
          });
       </script>
    </#if>
<#else>
    <#if (args.failureCallback?exists)>
       <script type="text/javascript">
          ${args.failureCallback}.call(${args.failureScope}, {
             status: {
                "code" : ${status.code},
                "name" : "${status.codeName}",
                "description" : "${status.codeDescription}"
             },
             message: "${jsonUtils.encodeJSONString(status.message)}"
          });
       </script>
    </#if>
</#if>
</body>
</html>
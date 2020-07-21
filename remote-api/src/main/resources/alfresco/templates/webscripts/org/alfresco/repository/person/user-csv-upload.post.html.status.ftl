<html>
<head>
   <title></title>
</head>
<body>
<#if status.code == 200>
   <#if (args.success!"")?matches("^[\\w\\d\\._]+$")>
       <script type="text/javascript">
          ${args.success}({
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
   <#if (args.failure!"")?matches("^[\\w\\d\\._]+$")>
       <script type="text/javascript">
          ${args.failure}({
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
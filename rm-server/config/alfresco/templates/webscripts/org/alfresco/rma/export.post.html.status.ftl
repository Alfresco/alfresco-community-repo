<html>
<head>
   <title>Export failure</title>
</head>
<body>
<#if (args.failureCallbackFunction?exists)>
   <script type="text/javascript">
      ${args.failureCallbackFunction}.call(${args.failureCallbackScope!'window'}, {
         status: {
            "code" : ${status.code},
            "name" : "${status.codeName}",
            "description" : "${status.codeDescription}"
         },
         message: "${jsonUtils.encodeJSONString(status.message)}"
      });
   </script>
</#if>
</body>
</html>
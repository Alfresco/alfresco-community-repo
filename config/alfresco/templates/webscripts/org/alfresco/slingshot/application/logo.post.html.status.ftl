<html>
<head>
   <title>Upload Logo Failure</title>
</head>
<body>
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
</body>
</html>
<html>
<head>
   <title>Upload Failure</title>
</head>
<body>
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
</body>
</html>
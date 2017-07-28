<html>
<head>
   <title>Upload Custom Model Failure</title>
</head>
<body>
<#if (args.failure!"")?matches("^[\\w\\d\\._]+$")>
   <script type="text/javascript">
      ${args.failure?js_string}({
         status: {
            "code" : ${status.code?html},
            "name" : "${status.codeName?json_string}",
            "description" : "${status.codeDescription?json_string}"
         },
         message: "${status.message?json_string}"
      });
   </script>
</#if>
</body>
</html>
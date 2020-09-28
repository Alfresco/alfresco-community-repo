<html>
<head>
   <title>Upload failure</title>
</head>
<body>
<#if (args.failure!"")?matches("^[\\w\\d\\._]+$")>
   <script type="text/javascript">
      ${args.failure}({});
   </script>
</#if>
</body>
</html>
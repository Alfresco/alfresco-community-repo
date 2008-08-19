<html>
<head>
   <title>Upload failure</title>
</head>
<body>
<#if (args.failure?exists)>
   <script type="text/javascript">
      ${args.failure}
   </script>
</#if>
</body>
</html>
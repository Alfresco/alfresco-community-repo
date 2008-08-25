<html>
<head>
   <title>Upload failure</title>
</head>
<body>
<#if (args.failureCallback?exists)>
   <script type="text/javascript">
      ${args.failureCallback}.call(${args.failureScope}, {});
   </script>
</#if>
</body>
</html>
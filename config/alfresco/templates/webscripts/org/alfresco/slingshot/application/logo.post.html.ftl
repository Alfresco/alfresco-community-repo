<html>
<head>
   <title>Upload success</title>
</head>
<body>
<#if (args.successCallback?exists)>
   <script type="text/javascript">
      ${args.successCallback}.call(${args.successScope},
      {
         nodeRef: "${logo.nodeRef}",
         fileName: "${name}"
      });
   </script>
</#if>
</body>
</html>
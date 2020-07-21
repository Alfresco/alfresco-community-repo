<h1>${root[0]}<h1>
<h2>${root[1]}<h2>
<h3>${root[2]?string("true","false")}<h3>
<h4>${root[3]!"<null>"}<h4>
<h5>${root[4]?datetime?string.medium_medium}<h5>
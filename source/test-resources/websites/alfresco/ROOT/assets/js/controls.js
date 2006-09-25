function MM_swapImgRestore() {
  var i,x,a=document.MM_sr; for(i=0;a&&i<a.length&&(x=a[i])&&x.oSrc;i++) x.src=x.oSrc;
}

function MM_preloadImages() {
  var d=document; if(d.images){ if(!d.MM_p) d.MM_p=new Array();
    var i,j=d.MM_p.length,a=MM_preloadImages.arguments; for(i=0; i<a.length; i++)
    if (a[i].indexOf("#")!=0){ d.MM_p[j]=new Image; d.MM_p[j++].src=a[i];}}
}

function MM_findObj(n, d) {
  var p,i,x;  if(!d) d=document; if((p=n.indexOf("?"))>0&&parent.frames.length) {
    d=parent.frames[n.substring(p+1)].document; n=n.substring(0,p);}
  if(!(x=d[n])&&d.all) x=d.all[n]; for (i=0;!x&&i<d.forms.length;i++) x=d.forms[i][n];
  for(i=0;!x&&d.layers&&i<d.layers.length;i++) x=MM_findObj(n,d.layers[i].document);
  if(!x && d.getElementById) x=d.getElementById(n); return x;
}

function MM_swapImage() {
  var i,j=0,x,a=MM_swapImage.arguments; document.MM_sr=new Array; for(i=0;i<(a.length-2);i+=3)
   if ((x=MM_findObj(a[i]))!=null){document.MM_sr[j++]=x; if(!x.oSrc) x.oSrc=x.src; x.src=a[i+2];}
}
function Alfresco_nav() {
/*
	var g,b,k,f,ag=arguments,a=parseInt(ag[0]);
	if(!document.p7setc) {
		p7c=new Array();document.p7setc=true;
		for(var u=0;u<10;u++) {
			p7c[u]=new Array();
		}
	}
	for(k=0;k<p7c[a].length;k++) {
		if((g=MM_findObj(p7c[a][k]))!=null) {
			b=(document.layers)?g:g.style;
			b.visibility="hidden";
		}
	}
	for(k=1;k<ag.length;k++) {
		if((g=MM_findObj(ag[k]))!=null) {
			b=(document.layers)?g:g.style;
			b.visibility="visible";
			f=false;
			for(var j=0;j<p7c[a].length;j++) {
				if(ag[k]==p7c[a][j]) { f=true; }
			}
			if(!f) { p7c[a][p7c[a].length++]=ag[k]; }
		}
	}
	*/
}

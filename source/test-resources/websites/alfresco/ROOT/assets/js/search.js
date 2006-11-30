function BC_key(e){
// Mozilla compatible
var key;
if(window.event) {
// for IE, e.keyCode or window.event.keyCode can be used
key = e.keyCode; 
}else if(e.which) {
// netscape
key = e.which; 	}
if (key==13){document.forms['search'].submit();}
}
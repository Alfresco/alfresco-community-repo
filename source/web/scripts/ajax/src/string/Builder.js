dojo.provide("dojo.string.Builder");
dojo.require("dojo.string");

// NOTE: testing shows that direct "+=" concatenation is *much* faster on
// Spidermoneky and Rhino, while arr.push()/arr.join() style concatenation is
// significantly quicker on IE (Jscript/wsh/etc.).

dojo.string.Builder = function(str){

	this.arrConcat = (dojo.render.html.capable && dojo.render.html["ie"]);

	var a = [];
	var b = "";
	var length = this.length = b.length;

	if(this.arrConcat){
		if(b.length > 0){
			a.push(b);
		}
		b = "";
	}

	this.toString = this.valueOf = function(){ 
		return (this.arrConcat) ? a.join("") : b;
	};

	this.append = function(){
		for(var x=0; x<arguments.length; x++){
			var s = arguments[x];
			if((s instanceof String)||(typeof s == "string")){
				if(this.arrConcat){
					a.push(s);
				}else{
					b+=s;
				}
				length += s.length;
				this.length = length;
			}else{
				// if we get something array-like, call append with it as args
				this.append.apply(this, s);
			}
		}
		return this;
	};

	this.clear = function(){
		a = [];
		b = "";
		length = this.length = 0;
		return this;
	};

	this.remove = function(f,l){
		var s = ""; 
		if(this.arrConcat){
			b = a.join(""); 
		}
		a=[];
		if(f>0){
			s = b.substring(0, (f-1));
		}
		b = s + b.substring(f + l); 
		length = this.length = b.length; 
		if(this.arrConcat){
			a.push(b);
			b="";
		}
		return this;
	};

	this.replace = function(o,n){
		if(this.arrConcat){
			b = a.join(""); 
		}
		a = []; 
		b = b.replace(o,n); 
		length = this.length = b.length; 
		if(this.arrConcat){
			a.push(b);
			b="";
		}
		return this;
	};

	this.insert = function(idx,s){
		if(this.arrConcat){
			b = a.join(""); 
		}
		a=[];
		if(idx == 0){
			b = s + b;
		}else{
			var t = b.split("");
			t.splice(idx,0,s);
			b = t.join("")
		}
		length = this.length = b.length; 
		if(this.arrConcat){
			a.push(b); 
			b="";
		}
		return this;
	};

	this.append.apply(this, arguments);
};

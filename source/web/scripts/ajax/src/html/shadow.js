dojo.provide("dojo.html.shadow");

dojo.require("dojo.lang.common");
dojo.require("dojo.uri.Uri");

dojo.html.shadow = function(node) {
	this.init(node);
}

dojo.lang.extend(dojo.html.shadow, {

	shadowPng: dojo.uri.dojoUri("src/html/images/shadow"),
	shadowThickness: 8,
	shadowOffset: 15,

	init: function(node){
		this.node=node;

		// make all the pieces of the shadow, and position/size them as much
		// as possible (but a lot of the coordinates are set in sizeShadow
		this.pieces={};
		var x1 = -1 * this.shadowThickness;
		var y0 = this.shadowOffset;
		var y1 = this.shadowOffset + this.shadowThickness;
		this._makePiece("tl", "top", y0, "left", x1);
		this._makePiece("l", "top", y1, "left", x1, "scale");
		this._makePiece("tr", "top", y0, "left", 0);
		this._makePiece("r", "top", y1, "left", 0, "scale");
		this._makePiece("bl", "top", 0, "left", x1);
		this._makePiece("b", "top", 0, "left", 0, "crop");
		this._makePiece("br", "top", 0, "left", 0);
	},

	_makePiece: function(name, vertAttach, vertCoord, horzAttach, horzCoord, sizing){
		var img;
		var url = this.shadowPng + name.toUpperCase() + ".png";
		if(dojo.render.html.ie){
			img=dojo.doc().createElement("div");
			img.style.filter="progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+url+"'"+
			(sizing?", sizingMethod='"+sizing+"'":"") + ")";
		}else{
			img=dojo.doc().createElement("img");
			img.src=url;
		}
		img.style.position="absolute";
		img.style[vertAttach]=vertCoord+"px";
		img.style[horzAttach]=horzCoord+"px";
		img.style.width=this.shadowThickness+"px";
		img.style.height=this.shadowThickness+"px";
		this.pieces[name]=img;
		this.node.appendChild(img);
	},

	size: function(width, height){
		var sideHeight = height - (this.shadowOffset+this.shadowThickness+1);
		if (sideHeight < 0) { sideHeight = 0; }
		if (height < 1) { height = 1; }
		if (width < 1) { width = 1; }
		with(this.pieces){
			l.style.height = sideHeight+"px";
			r.style.height = sideHeight+"px";
			b.style.width = (width-1)+"px";
			bl.style.top = (height-1)+"px";
			b.style.top = (height-1)+"px";
			br.style.top = (height-1)+"px";
			tr.style.left = (width-1)+"px";
			r.style.left = (width-1)+"px";
			br.style.left = (width-1)+"px";
		}
	}
});


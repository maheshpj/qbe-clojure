
//test();

function test() {
	alert(1);
};

function dislayOptions(clmnm, name) {
	var div = document.getElementById(name);
	var clmchkd = document.getElementById(clmnm).checked;
	
	if (clmchkd) { 
		div.style.display = 'table-row';
    } else {
    	div.style.display = 'none';
    }
};

function toggle(id){
    ul = "ul_" + id;
    //img = "img_" + id;
    hdn = "HDN." + id;
    ulElement = document.getElementById(ul);
    hdnElement = document.getElementById(hdn);
    //imgElement = document.getElementById(img);
    if (ulElement){
        if (ulElement.className == 'closed'){
            ulElement.className = "open";
            hdnElement.value = "open";
            //imgElement.src = "opened.gif";
        } else {
            ulElement.className = "closed";
            hdnElement.value = "closed";
            //imgElement.src = "closed.gif";
        }
    }
};

function openUl(id) {
	ul = "ul_" + id;
	ulElement = document.getElementById(ul);
	ulElement.className = "open";
}

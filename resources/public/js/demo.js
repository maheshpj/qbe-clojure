
function test() {
	alert(1);
}

function dislay-options (clmnm name) {
	var display = document.getElementById(name).style.display;
	var clmchkd = document.getElementById(clmnm).checked;
	if (clmchkd) { 
       	display = 'inline';
    } else {
		display = 'none';
    }
}
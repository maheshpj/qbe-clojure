$(document).ready(function() {
	
	 $("body").append('<div id="loading_indicator">Loading...<img src="img/ajax-loader.gif"></div>');
	 
	 $("#loading_indicator").css({
		display:"none",
		margin:"0px",
		paddingLeft:"0px",
		paddingRight:"0px",
		paddingTop:"0px",
 		paddingBottom:"0px",
	 	position:"absolute",
	 	right:"50%",
	 	top:"3px",
	 	width:"auto",
	 	height: "30px",
	 	border: "1px dotted grey",
	 	background: "gold"
	 });

	$(document).ajaxStart(function() {
		$("#loading_indicator").show();
	}).ajaxStop(function() {
		$("#loading_indicator").hide();
	});
	
	var url = "/run"; 

	$("#runbtn").click(function(event) {
		$.ajax({
			type: "POST",
			url: url,
			data: $("#schemaForm").serialize(), 
			success: function(data)
			{
				$("#result").html(data);
			}
		});
	});
	
	$("#filtersel").click(function(event) {
		$.ajax({
			type: "POST",
			url: "/filtersel/on",
			data: $("#schemaForm").serialize(), 
			success: function(data)
			{
				$("#list-div").html(data);
			}
		});
	});
	
	$("#filterseloff").click(function(event) {
		$.ajax({
			type: "POST",
			url: "/filtersel/off",
			data: $("#schemaForm").serialize(), 
			success: function(data)
			{
				$("#list-div").html(data);
			}
		});
	});
})
	
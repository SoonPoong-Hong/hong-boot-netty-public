<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>websocket test</title>
<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.0/jquery.min.js"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" integrity="sha384-Tc5IQib027qvyjSMfHjOMaLkfuWVxZxUPnCJA7l2mCWNIpG9mGCD8wGNIcPD7Txa" crossorigin="anonymous"></script>

<script>

// 	{
// 	  "msg" : "잘 지내고 있냐",
// 	  "action" : "SendMsg",
// 	  "headers" : {
// 	    "lets" : "go go",
// 	    "refId" : "id_12345",
// 	    "roomId" : "room_1234"
// 	  }
// 	}


var websocket ;
function startWebsocket(){
	websocket = new WebSocket("ws://localhost:8090/websocket" );
	
	websocket.onerror = function(event) {
        log("== Error : " + event)
    };

    websocket.onopen = function(event) {
//     	alert("== onopen");
        log("== onopen");
    };

    websocket.onmessage = function(event) {
        log(event.data);
    };
    
	$('#msg').keyup(function(e) {
	    if (e.keyCode == 13) {
	    	websocket.send($("#msg").val());
	    	$("#msg").val("");
	    }        
	});
}

function log(msg){
	$('<div/>').text(msg).appendTo($("#log"));
}

$(function(){
	startWebsocket();
});

</script>



</head>
<body>


<div id="log"></div>
<input type="text" id="msg" style="width:100%"/>


</body>

<style type="text/css">
html {
    overflow: hidden;
}
/* body { */
/*     overflow: hidden; */
/*     padding: 0; */
/*     margin: 0; */
/*     width: 100%; */
/*     height: 100%; */
/*     background: gray; */
/* } */
/* #log { */
/*     background: white; */
/*     margin: 0; */
/*     padding: 0.5em 0.5em 0.5em 0.5em; */
/*     position: absolute; */
/*     top: 0.5em; */
/*     left: 0.5em; */
/*     right: 0.5em; */
/*     bottom: 3em; */
/*     overflow: auto; */
/*     width:100%; */
/* } */
/* #form { */
/*     padding: 0 0.5em 0 0.5em; */
/*     margin: 0; */
/*     position: absolute; */
/*     bottom: 1em; */
/*     left: 0px; */
/*     width: 100%; */
/*     overflow: hidden; */
/* } */
</style>

</html>
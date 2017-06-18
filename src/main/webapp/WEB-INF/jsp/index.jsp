<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>websocket test</title>

<style type="text/css">
.contents_inner {
    height: 300px;
    max-height: 300px;
    overflow-y: scroll;
    background: ivory;
    margin-left: 20px;
    margin-right: 20px;
}

.scrollable_200 {
    height: 200px;
    max-height: 200px;
    overflow-y: scroll;
    background: ivory;
}

.scrollable_300 {
    height: 300px;
    max-height: 300px;
    overflow-y: scroll;
    background: ivory;
    margin-left: 20px;
    margin-right: 20px;
}

.scrollable_400 {
    height: 400px;
    max-height: 400px;
    overflow-y: scroll;
    background: ivory;
}

.scrollable_500 {
    height: 500px;
    max-height: 500px;
    overflow-y: scroll;
    background: ivory;
}

.margin_20{
    margin-left: 20px;
    margin-right: 20px;
}
</style>

<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.2.0/jquery.min.js"></script>
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.css" crossorigin="anonymous">
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.css"  crossorigin="anonymous">
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js" crossorigin="anonymous"></script>

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
var myId;
var roomInfo = {};
function startWebsocket(){
	var name = prompt("이름은 ?");
	if(name==null || name==""){
		alert("이름을 입력하셔야죠.");
		return;
	}
	
	websocket = new WebSocket("ws://${host}:${websocketPort}/websocket" );
	
	websocket.onerror = function(event) {
		console.log("== error", event);
        alert("== Error : " + event.code + " , " + event.reason);
    };

    websocket.onopen = function(event) {
        console.log("== onopen");
        $("#globalMsg").text("websocket 연결됨");
        login(name);
    };
	
    websocket.onclose = function(event){
    	console.log("== websocket closeed");
        $("#globalMsg").text("websocket 종료");
    	initDisplay();
    	$("#myId").text("");
    	$("#allUsers").empty();
    	websocket = null;
    };
    
    websocket.onmessage = function(event) {
        console.log(event.data);
        var obj = JSON.parse(event.data);
        var action = obj.action;
        if(action=='LoginConfirmed'){
        	var refId = getFromHeader(obj, "refId");
        	var refName = getFromHeader(obj, "refName");
        	console.log("== myId : " + refId);
        	myId = refId;
        	$("#myId").text(myId);
        	$("#myName").text(refName);
        } else if(action=='AllUserList'){
        	var refId = getFromHeader(obj, "refId");
        	var arr = JSON.parse(obj.msg);
        	var target = $("#allUsers");
        	target.html("");
        	$.each(arr, function(idx, elem){
	        	append2(target, elem.name + "(" + elem.id + ")"  + (myId==elem.id ? " => 나" : ""), elem.id );
        	});
        } else if(action=='RoomList'){
        	var arr = JSON.parse(obj.msg);
        	var target = $("#allRooms");
        	target.html("");
        	$.each(arr, function(idx, elem){
	        	append2(target, elem.name, elem.id );
        	});
        } else if(action=='UserList'){
        	var arr = JSON.parse(obj.msg);
        	var roomId = getFromHeader(obj, "roomId");
      		var idx = getIdxFromRoomname(roomId);
        	var target = $("#roomUsers_"+idx);
        	target.html("");
        	$.each(arr, function(idx, elem){
	        	append(target, elem.name + "(" + elem.id + ")" + (myId==elem.id ? " => 나" : "") ) ;
        	});
        } else if(action=='EnterToRoom'){
        	var roomId = getFromHeader(obj, "roomId");
        	var refId = getFromHeader(obj, "refId");
        	var refName = getFromHeader(obj, "refName");
      		var idx = getIdxFromRoomname(roomId);
        	if(refId==myId){
	        	$("#room_"+idx).css("color","blue");
	        	append($("#contents_"+idx), "방으로 들어왔음.");
	        	$("#container_"+idx).find(".switchComp").css("background-color","white").prop("disabled",false);
        	}else{
	        	append($("#contents_"+idx), refName+"("+refId+")이 방으로 들어왔습니다.");
        	}
        	$("#input_"+idx).css("background-color", "white");
        } else if(action=='ExitFromRoom'){
        	var roomId = getFromHeader(obj, "roomId");
        	var refId = getFromHeader(obj, "refId");
        	var refName = getFromHeader(obj, "refName");
      		var idx = getIdxFromRoomname(roomId);
        	if(refId==myId){
	        	$("#room_"+idx).css("color","black").val("");
	        	append($("#contents_"+idx), "나는 방에서 나갔음.");
	        	$("#container_"+idx).find(".switchComp").css("background-color","silver").prop("disabled",true);
	        	$("#roomUsers_"+idx).empty();
        	}else{
	        	append($("#contents_"+idx), refName+"("+refId+")이 방에서 나갔습니다.");
        	}
        	$("#input_"+idx).css("background-color", "silver");
        } else if(action=='SendMsg'){
        	var roomId = getFromHeader(obj, "roomId");
        	var refId = getFromHeader(obj, "refId");
        	var refName = getFromHeader(obj, "refName");
      		var idx = getIdxFromRoomname(roomId);
        	append($("#contents_"+idx), (myId==refId ? "<나> " : refName+"("+refId+")" + " : ") + obj.msg);
        } else if(action=='LogIn'){
        	var roomId = getFromHeader(obj, "roomId");
        	var refId = getFromHeader(obj, "refId");
        	var refName = getFromHeader(obj, "refName");
        	var target = $("#allUsers");
        	if(target.find("[hong-etc='" + refId+"']").length==0){
        		append2(target, refName + "(" + refId +")", refId);
        	}
        } else if(action=='LogOut'){
        	var roomId = getFromHeader(obj, "roomId");
        	var refId = getFromHeader(obj, "refId");
        	var refName = getFromHeader(obj, "refName");
        	var target = $("#allUsers");
        	target.find("[hong-etc='" + refId+"']").remove();
        }
    };
    
// 	$('#input_1').keyup(function(e) {
// 	    if (e.keyCode == 13) {
// 	    	console.log("==");
// 	    	append($("#contents_1"), $("#input_1").val());
// 	    	$("#contents_1").animate({ scrollTop: $(document).height() });
// // 	    	websocket.send($("#msg").val());
// // 	    	$("#msg").val("");
// 	    }        
// 	});
}

function onMsgKeyUp(event, idx){
    if (event.keyCode == 13 || event.which == 13) {
    	console.log("== enter");
  		var roomId = $("#room_"+idx).val();
    	var input = $("#input_"+idx);
    	var msg = input.val();
    	input.val("");
    	var obj = new Builder().action("SendMsg").header("roomId", roomId).msg(msg).finish();
    	var jsonStr = JSON.stringify(obj);
    	websocket.send(jsonStr);
    }   	
}

function getFromHeader(obj, key){
	var headers =  obj.headers;
	if(headers!=null){
		return headers[key];
	}else{
		return null;
	}
}

function login(name){
	var obj = new Builder().action("LogIn").header("name", name).finish();
	var jsonStr = JSON.stringify(obj);
	websocket.send(jsonStr);
}

function disconnect(){
	websocket.close();
	initDisplay();
}

function Builder(){
	var me = {};
	function action(a){
		me.action=a;
		return this;
	}
	function header(k,v){
		if(me.headers==null){
			me.headers = {};
			me.headers[k] = v;
		}else{
			me.headers.push({k:v});
		}
		return this;
	}
	function msg(m){
		me.msg = m;
		return this;
	}
	function finish(){
		return me;
	}	
	return {
		action : action,
		header : header,
		msg : msg,
		finish : finish
	};
}

function createRoom(idx){
	if(websocket==null ){
		alert("websocket을 시작한후에 하세요.");
		return;
	}
	var roomName = prompt("방명은 ? ");
	console.log("== roomName", roomName);
	if(roomName==null || roomName==""){
		alert("방명을 입력하셔야지요.");
		return;
	}
	$("#room_" + idx).val(roomName);
	var obj = new Builder().action("CreateRoom").header("roomId", roomName).finish();
	var jsonStr = JSON.stringify(obj);
	websocket.send(jsonStr);
}

function exitFromRoom(idx){
	var roomId = $("#room_"+idx).val();
	var obj = new Builder().action("ExitFromRoom").header("roomId", roomId).finish();
	var jsonStr = JSON.stringify(obj);
	websocket.send(jsonStr);
}

function requestAllUserList(){
	var obj = new Builder().action("AllUserList").finish();
	var jsonStr = JSON.stringify(obj);
	websocket.send(jsonStr);
}

function getIdxFromRoomname(roomName){
	var result ;
	$("#room_1, #room_2, #room_3, #room_4").each(function(){
		var val = $(this).val();
		if(val==roomName){
			result = $(this).prop("id").substring(5);
			return false;
		}
	});
	return result;
}

function log(msg){
	$('<div/>').text(msg).appendTo($("#log"));
}

function append($div, msg, etcVal){
	var children = $div.children("div");
	var len = children.length;
	// 50개 이전것은 삭제
	if(len>50){
		children.slice(0,len-50).remove();
	}
	var newElem = $('<div/>').text(msg);
	if(etcVal!=null){
		newElem.attr("hong-etc", etcVal);
	}
	newElem.appendTo($div);
	$div.animate({ scrollTop: 100000 });	
}

function append2($div, msg, etcVal){
	var newElem = $('<div/>').text(msg);
	if(etcVal!=null){
		newElem.attr("hong-etc", etcVal);
	}
// 	if(funcName!=null){
// 		newElem.attr("onclick", funcName + "('" + etcVal +"');").attr("style", "cursor: pointer;");
// 	}
	newElem.appendTo($div);
	$div.animate({ scrollTop: 100000 });	
}

function onClick_room(name){
	if(confirm(name + " 방에 들어가시겠습니까?")){
    	var obj = new Builder().action("EnterToRoom").header("roomId", name).finish();
    	var jsonStr = JSON.stringify(obj);
    	websocket.send(jsonStr);		
	}
}

function initDisplay(){
	$(".switchComp").css("background-color","silver").prop("disabled", true);
}

// function createNewRoom(){
// 	var name = $("#newRoomName").val();
// 	var obj = new Builder().action("CreateRoom").header("roomId", name).finish();
// 	var jsonStr = JSON.stringify(obj);
// 	websocket.send(jsonStr);
// }

// function popupMessenger(){
// 	var win = window.open("/msgPopup", "_blank", "toolbar=yes,scrollbars=yes,resizable=yes,top=500,left=500,width=400,height=400");
// }


$(function(){
//	startWebsocket();
	initDisplay();
});

</script>



</head>
<body>


<div class="container-fluid">
	<div class="row">
		내 ID : <span id="myId"></span> (<span id="myName"></span>) 
		<button onclick="startWebsocket();">websocket 시작</button> 
		<button onclick="disconnect();">websocket 끊기</button> 
		<span id="globalMsg"></span>
	</div>
	<div class="row">
	
	  <div class="col-xs-2">  
	 		<div class="row">전체 사용자 목록</div>
	 		<button onclick="requestAllUserList();">새로고침</button>
		  	<div id="allUsers" class="row scrollable_400" > </div>
	 		<div class="row">전체 방 목록</div>
		  	<div id="allRooms" class="row scrollable_200"> </div>
	  </div>
	  
	  <div class="col-xs-10 row">  
			  <div class="col-xs-6">  
			  	<div id="container_1" class="row" style="margin-bottom: 20px;">
				  	<div class="">
					  	<input type="text" id="room_1" readonly class="room roomId switchComp" title="방 만들기">
					  	<button class=" " onclick="createRoom('1');" >방만들거나 들어가기</button>
					  	<button class=" switchComp" onclick="exitFromRoom('1');">방나가기</button>
				  	</div>
				  	<div class="row">
					  <div class="col-xs-4" style="">  
						  <div>사용자</div>
						  <div id="roomUsers_1" class="row switchComp scrollable_300" ></div>
					  </div>
					  <div class="col-xs-8" style="">  
					  	<div class="contents_inner switchComp" id="contents_1" ></div>
					  	<div><input id="input_1" onkeyup="onMsgKeyUp(event, '1');" type="text" style="width:90%;" class="form-control switchComp margin_20"></div>
					  </div>
				  	</div>
			  	</div>

			  	<div id="container_2" class="row" style="margin-bottom: 20px;">
				  	<div class="">
					  	<input type="text" id="room_2" readonly class="room roomId switchComp" title="방 만들기">
					  	<button class=" " onclick="createRoom('2');" >방만들거나 들어가기</button>
					  	<button class=" switchComp" onclick="exitFromRoom('2');">방나가기</button>
				  	</div>
				  	<div class="row">
					  <div class="col-xs-4" style=";">  
						  <div>사용자</div>
						  <div class="row switchComp scrollable_300" id="roomUsers_2"></div>
					  </div>
					  <div class="col-xs-8" style="">  
					  	<div class="contents_inner switchComp" id="contents_2" ></div>
					  	<div><input id="input_2" onkeyup="onMsgKeyUp(event, '2');" type="text" class="form-control switchComp margin_20"></div>
					  </div>
				  	</div>
			  	</div>
			  </div>
			  
			  
			  <div class="col-xs-6">  
			  	<div id="container_3" class="row" style="margin-bottom: 20px;">
				  	<div class="">
					  	<input type="text" id="room_3" readonly class="room roomId switchComp" title="방 만들기">
					  	<button class=" " onclick="createRoom('3');" >방만들거나 들어가기</button>
					  	<button class=" switchComp" onclick="exitFromRoom('3');">방나가기</button>
				  	</div>
				  	<div class="row">
					  <div class="col-xs-4" style=";">  
						  <div>사용자</div>
						  <div class="row switchComp scrollable_300" id="roomUsers_3"></div>
					  </div>
					  <div class="col-xs-8" style="">  
					  	<div class="contents_inner switchComp" id="contents_3" ></div>
					  	<div><input id="input_3" onkeyup="onMsgKeyUp(event, '3');" type="text" class="form-control switchComp margin_20"></div>
					  </div>
				  	</div>
				</div>
				
			  	<div id="container_4" class="row" style="margin-bottom: 20px;">
				  	<div class="">
					  	<input type="text" id="room_4" readonly class="room roomId switchComp" title="방 만들기">
					  	<button class=" " onclick="createRoom('4');" >방만들거나 들어가기</button>
					  	<button class=" switchComp" onclick="exitFromRoom('4');">방나가기</button>
				  	</div>
				  	<div class="row">
					  <div class="col-xs-4" style=";">  
						  <div>사용자</div>
						  <div class="row switchComp scrollable_300" id="roomUsers_4"></div>
					  </div>
					  <div class="col-xs-8" style="">  
					  	<div class="contents_inner switchComp" id="contents_4" ></div>
					  	<div><input id="input_4" onkeyup="onMsgKeyUp(event, '4');" type="text" class="form-control switchComp margin_20"></div>
					  </div>
				  	</div>
			  	</div>
			  </div>
			  
	  </div>
	  
	</div>

	<div id="log"></div>
</div>


</body>

</html>
$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");
	//发送ajax异步请求前，将csrf令牌设置到请求的消息头中
	/*var token = $("meta[name='_csrf']").attr("content");
	var header = $("meta[name='_csrf_header']").attr("content");
	$(document).ajaxSend(function (e,xhr,options) {
		xhr.setRequestHeader(header,token);
	});*/


	var title = $("#recipient-name").val();
	var content = $("#message-text").val();

	$.post(
		contextPath+"/discuss/add",
		{"title":title,"content":content},
		function (data) {
			data = $.parseJSON(data);
			$("#hintBody").text(data.msg);
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				if (data.code == 0){
					window.location.reload();
				}
			}, 2000);
		}
	)


}
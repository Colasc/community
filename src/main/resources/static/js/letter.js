$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
	$("#sendModal").modal("hide");
	var toName = $("#recipient-name").val();
	var content = $("#message-text").val();
	$.post(
		contextPath+"/letter/send",
		{"toName":toName,"content":content},
		function (data) {
			var data = $.parseJSON(data);
			if (data.code == 0){
				$("#hintBody").text("发送成功");
			}else {
				$("#hintBody").text(data.msg);
			}
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				location.reload();
			}, 2000);
		}
	);


}

function delete_msg() {

	/*window.alert("您确定要删除吗?");

	var messageId = $("#messageId").val();
	$.post(
		contextPath+"/letter/delete",
		{"id":messageId},
		function (data) {
			data = $.parseJSON(data);
			if (data.code == 0){
				// TODO 删除数据
				$(this).parents(".media").remove();
			}else {
				alert("删除失败！");
			}
		}
	);*/

	// TODO 删除数据
	$(this).parents(".media").remove();


}
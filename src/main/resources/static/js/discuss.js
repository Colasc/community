$(function () {
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
});

function like(btn,entityType,entityId,entityUserId,postId) {
    $.post(
        contextPath+"/like",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId,"postId":postId},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0){
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.likeStatus == 1?"已赞":"赞");
            }else {
                alert(data.msg);
            }
        }
    )
}
//置顶
function setTop() {
    var id = $("#postId").val();
    $.post(
        contextPath+"/discuss/top",
        {"id":id},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0){
                $("#topBtn").attr("disabled","disabled");
            }else {
                alert(data.msg);
            }
        }
    )
}
//加精
function setWonderful() {
    var id = $("#postId").val();
    $.post(
        contextPath+"/discuss/wonderful",
        {"id":id},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0){
                $("#wonderfulBtn").attr("disabled","disabled");
            }else {
                alert(data.msg);
            }
        }
    )
}
//删除
function setDelete() {
    var id = $("#postId").val();
    $.post(
        contextPath+"/discuss/delete",
        {"id":id},
        function (data) {
            data = $.parseJSON(data);
            if (data.code == 0){
                location.href = contextPath + "/index";
            }else {
                alert(data.msg);
            }
        }
    )
}
$(function(){
	$("#publishBtn").click(publish);
});

function publish() {
	$("#publishModal").modal("hide");

	//获取标题内容
	const title = $("#recipient-name").val();
	const content = $("#message-text").val();
	$.post(
		CONTEXT_PATH+"/discuss/add",
		{title:title,content:content},
		function (data) {
			data = $.parseJSON(data);
			console.log(data);
			//在提示框中显示返回消息
			$("#hintBody").text(data.message);
			//显示提示框
			$("#hintModal").modal("show");
			//2秒后自动消失
			setTimeout(function(){
				$("#hintModal").modal("hide");
				//刷新页面
				if (data.code === 200) {
					window.location.reload();
				}
			}, 2000);
		}
	)


}
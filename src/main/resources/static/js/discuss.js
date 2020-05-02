function like(btn, entityType, entityId, entityUserId) {
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId},
        function(data) {
            // data = $.parseJSON(data);
            if(data.code === 200) {
                $(btn).children("i").text(data.data.likeCount);
                $(btn).children("b").text(data.data.likeStatus===1?'已赞':"赞");
            } else {
                alert(data.message);
            }
        }
    );
}
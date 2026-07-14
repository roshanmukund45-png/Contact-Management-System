console.log("this is script file")

const toggleSidebar=()=>{
    if($(".sidebar").is(":visible")){
        //true
        //band karna hai
        $(".sidebar").css("display","none");
        $(".content").css("margin-left","0%");
    }else{
        //false
        //show karna hai
        $(".sidebar").css("display","block");
        $(".content").css("margin-left","20%");
    }
}
function deleteContact(Cid){
				swal({
				  title: "Are you sure?",
				  text: "you want to delete this contact..",
				  icon: "warning",
				  buttons: true,
				  dangerMode: true,
				})
				.then((willDelete) => {
				  if (willDelete) {
				    

					window.location="/user/delete/"+Cid;
				  } else {
				    swal("Your contact is safe! !");
				  }
				});
			}
			console.log("this is script file");
			console.log("swal is", typeof swal);

    $(document).ready(function() {  	
    	
    	
    	// Verification longueur min du password 
    	$("#changePasswordForm").submit(function( event ) {  	
			var newPasswd = $('#_changepassword_newpassword');
			if (newPasswd.val().length < 6) {
				alert('Merci de saisir un password contenant au moins 6 caractères.');
				 event.preventDefault();
			} 
    	});
    	
    	$('.password-container').pschecker({ 
    	});

    	
    });  
    
    
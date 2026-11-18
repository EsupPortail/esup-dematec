    $(document).ready(function() {  	
    	
    	
    	// Verification longueur min du password 
    	$("#changePasswordForm").submit(function( event ) {  	
			var newPasswd = $('#_changepassword_newpassword');
			if (newPasswd.val().length < 6) {
				alert('Merci de saisir un mot de passe contenant au moins 6 caractères.');
				$('form').off("submit", preventDoubleSubmission);
				event.preventDefault();
			} 
    	});
    	$(".password-container #User").submit(function( event ) {  	
			var newPasswd = $('#_password_id');
			if (newPasswd.val().length < 6) {
				alert('Merci de saisir un mot de passe contenant au moins 6 caractères.');
				$('form').off("submit", preventDoubleSubmission);
				event.preventDefault();
			} 
    	});
    	
    	
    	
    	
    	function toogleSidebarLeft() {
    	    $('#toggleSidebar i').addClass('icon-chevron-right');
    	    $('#toggleSidebar i').removeClass('icon-chevron-left');
  	        $('#demat-right').removeClass('span9');
	        $('#demat-right').addClass('span12');
	        $('#demat-left').hide();
	        sessionStorage.setItem("dematSidebar", "left");
    	}
    	
    	function toogleSidebarRight() {
    	    $('#toggleSidebar i').removeClass('icon-chevron-right');
    	    $('#toggleSidebar i').addClass('icon-chevron-left');
  	        $('#demat-right').removeClass('span12');
	        $('#demat-right').addClass('span9');
	        $('#demat-left').show();
	        sessionStorage.setItem("dematSidebar", "right");
    	}
    	
    	// toogle sur menu gauche    	
    	$('#toggleSidebar').click(function() {
    	  if ($('#toggleSidebar i').hasClass('icon-chevron-left')) {
    		  toogleSidebarLeft();
    	  } else {
    		  toogleSidebarRight();
    	  }
    	});

    	if(sessionStorage.getItem("dematSidebar") === "left") {
    		toogleSidebarLeft();
    	}	
    	
    	// jQuery plugin to prevent double submission of forms
    	function preventDoubleSubmission(e) {
          // Une confirmation ou une validation peut avoir annulé la soumission.
          if (e.isDefaultPrevented()) {
            return;
          }
    	  if(!$('#_zip_id').is(':checked') && !$('#_csv_id').is(':checked') && !$('#_mails_id').is(':checked') && !$('#templateFile').val()) {
	    	    var $form = $(this);
	
	    	    if ($form.data('submitted') === true) {
	    	      // Previously submitted - don't submit again
	    	      e.preventDefault();
	    	      alert('Envoi en cours, merci de patienter ... ou de recharger complètement votre page [F5] pour annuler.');
	    	    } else {
	    	      // Mark it so that the next submit can be ignored
	    	      $form.data('submitted', true);
	    	    }
    		}
    	 }
    	
    	$('form').on('submit', preventDoubleSubmission);
    	
    	
    	$(".reset").click(function() {
    		$("form").find('input:text, input:password, input:file, select, textarea').val('');
    		$("form").find('input:radio, input:checkbox, option').removeAttr('checked').removeAttr('selected');
    		$("form").find('option').removeAttr('selected');
    	});
    	
    	$('#_zip_id').change(function() {
            if($(this).is(":checked")) {
            	$('#_csv_id').attr("checked", false);
            	$('#_mails_id').attr("checked", false);
            }
        });
    	$('#_csv_id').change(function() {
            if($(this).is(":checked")) {
            	$('#_zip_id').attr("checked", false);
            	$('#_mails_id').attr("checked", false);
            }
        });
    	$('#_mails_id').change(function() {
            if($(this).is(":checked")) {
            	$('#_csv_id').attr("checked", false);
            	$('#_zip_id').attr("checked", false);
            }
        });

	});
    	
    function humanFileSize(bytes) {
	    var thresh = 1024;
	    if(Math.abs(bytes) < thresh) {
	        return bytes + ' B';
	    }
	    var units = ['kB','MB','GB','TB','PB','EB','ZB','YB']
	    var u = -1;
	    do {
	        bytes /= thresh;
	        ++u;
	    } while(Math.abs(bytes) >= thresh && u < units.length - 1);
	    return bytes.toFixed(1)+' '+units[u];
	}

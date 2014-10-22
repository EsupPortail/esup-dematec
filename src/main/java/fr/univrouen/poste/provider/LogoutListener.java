package fr.univrouen.poste.provider;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.context.ApplicationListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.session.SessionDestroyedEvent;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fr.univrouen.poste.services.ReturnReceiptService;

@Component
public class LogoutListener implements ApplicationListener<SessionDestroyedEvent> {

	@Resource 
	ReturnReceiptService returnReceiptService;
	
	@Override
	@Transactional
	public void onApplicationEvent(SessionDestroyedEvent event)
	{
		List<SecurityContext> lstSecurityContext = event.getSecurityContexts();
		UserDetails ud;
		for (SecurityContext securityContext : lstSecurityContext)
		{
			Authentication auth = securityContext.getAuthentication();
			returnReceiptService.sendDepotStatusIfRequired(auth);
		}
	}

}



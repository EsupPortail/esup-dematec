package fr.univrouen.poste.provider;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.web.filter.GenericFilterBean;

public class CheckProfilSpringSecurityFilter extends GenericFilterBean {

	final Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) req;
		
		if(!request.getServletPath().startsWith("/resources/") && !"/profilChoice".equals(request.getServletPath())
			&& request.isUserInRole("ROLE_CANDIDAT") 
			&& request.isUserInRole("ROLE_MEMBRE")) {
				logger.info(request.getRemoteUser() + " est authentifié et est à la fois membre et candidat, il faut qu'il choisisse un profil.");
		        HttpServletResponse response = (HttpServletResponse) res;      
	        response.sendRedirect(request.getContextPath() + "/profilChoice");
		} else {
			chain.doFilter(req, res);
		}
	}

}

package fr.univrouen.poste.provider;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.filter.GenericFilterBean;

public class CheckProfilSpringSecurityFilter extends GenericFilterBean {

	private final Logger logger = Logger.getLogger(getClass());

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

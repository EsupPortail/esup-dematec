package fr.univrouen.poste.provider;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.GenericFilterBean;

public class CheckProfilSpringSecurityFilter extends GenericFilterBean {

	private final Logger logger = Logger.getLogger(getClass());

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {

		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		HttpServletRequest request = (HttpServletRequest) req;
		
		if(!"/profilChoice".equals(request.getServletPath())
			&& auth.getAuthorities().contains(new GrantedAuthorityImpl("ROLE_CANDIDAT")) 
			&& auth.getAuthorities().contains(new GrantedAuthorityImpl("ROLE_MEMBRE"))) {
				logger.warn(auth.getName() + " est authentifié et est à la fois membre et candidat, il faut qu'il choisisse un profil.");
		        HttpServletResponse response = (HttpServletResponse) res;      
	        response.sendRedirect(request.getContextPath() + "/profilChoice");
		} else {
			chain.doFilter(req, res);
		}
	}

}

/**
 * Licensed to ESUP-Portail under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * ESUP-Portail licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.univrouen.poste.web;

import fr.univrouen.poste.dao.PosteCandidatureDao;
import fr.univrouen.poste.domain.PosteCandidature;
import fr.univrouen.poste.services.LogService;
import fr.univrouen.poste.web.HiddenHttpMethodFilter.HttpMethodRequestWrapper;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;


@Service
@Controller
public class ExceptionController implements HandlerExceptionResolver {
	
	final Logger log = LoggerFactory.getLogger(getClass());

	@Resource
	LogService logService;

	@Resource
	PosteCandidatureDao posteCandidatureDao;

	@Resource
	ConfigInterceptor configInterceptor;

	
	@Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
		
		if(ex instanceof AccessDeniedException) {
			return this.deniedHandler(request, response);
		}
		
        String ip = request.getRemoteAddr();	
		if(ex instanceof MultipartException || ex instanceof IOException) {
			log.warn("MultipartException or IOException with this client " + ip + ". We can assume that the client has canceled his request (because of a double-click / double-submit of the form for example).", ex);
		} else {	
			log.error("Uncaught exception  with this client " + ip, ex);
		}
		
		// hack for logging uploads failed 
		if(request.getServletPath().matches("/postecandidatures/[0-9]*/addFile")) {
			String posteCandidatureId = request.getServletPath().replaceAll("/postecandidatures/([0-9]*)/addFile", "$1");
			PosteCandidature posteCandidature = posteCandidatureDao.findPosteCandidature(Long.valueOf(posteCandidatureId));
			logService.logActionFile(LogService.UPLOAD_FAILED_ACTION, posteCandidature, null, request, new Date());
		}
		
		
		if(response.isCommitted()) {
			// Client can't get exception page here. 
			return null;
		} else {
	    	//response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	        ModelAndView modelAndview = new ModelAndView("uncaughtException");
	        modelAndview.addObject("exception", ex);
			modelAndview.addObject("exception_stacktrace", getStackTrace(ex));
			modelAndview.addObject("exception_message", ex.getMessage());
			configInterceptor.completeModel(request.getServletPath(), modelAndview);
	        avoid405Error(request);
	        return modelAndview;
		}
    }

	@RequestMapping("/denied")
    public ModelAndView deniedHandler(HttpServletRequest request, HttpServletResponse response) {
        String ip = request.getRemoteAddr();	
	    log.warn("Access Denied for " + ip);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        avoid405Error(request);
		return new ModelAndView("accessDeniedException");
    }

	


	@RequestMapping("/uncaughtException")
    public ModelAndView uncaughtExceptionView(HttpServletRequest request) {
	    Throwable exception = (Throwable) request.getAttribute("jakarta.servlet.error.exception");
	    ModelAndView modelAndview = new ModelAndView("uncaughtException");
	    modelAndview.addObject("uncaughtException", true);
	    modelAndview.addObject("exception", exception);
		modelAndview.addObject("exception_stacktrace", getStackTrace(exception));
		modelAndview.addObject("exception_message", exception.getMessage());
		configInterceptor.completeModel(request.getServletPath(), modelAndview);
	    avoid405Error(request);
		return modelAndview;
    }
   
	/**
	 * Try to avoid 405 - JSPs only permit GET POST or HEAD with exceptions on put/delete/patch
	 * @param request
	 */
	void avoid405Error(HttpServletRequest request) {		
		ServletRequest servletRequest = request;
	    while(servletRequest!= null && !(servletRequest instanceof HttpMethodRequestWrapper)  && (servletRequest instanceof HttpServletRequestWrapper)) {	    	
	    	servletRequest = ((HttpServletRequestWrapper)servletRequest).getRequest();
	    }
	    if(servletRequest instanceof HttpMethodRequestWrapper) {
	    	((HttpMethodRequestWrapper) servletRequest).setMethod("GET");
	    }
	    org.springframework.web.filter.HiddenHttpMethodFilter a;
	}


	private String getStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}

}


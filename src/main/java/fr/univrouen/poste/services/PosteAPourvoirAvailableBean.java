package fr.univrouen.poste.services;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

import fr.univrouen.poste.domain.PosteAPourvoir;

@RooJavaBean
@RooToString
public class PosteAPourvoirAvailableBean {
	
	PosteAPourvoir poste;
	
	Boolean candidat = false;
	
}

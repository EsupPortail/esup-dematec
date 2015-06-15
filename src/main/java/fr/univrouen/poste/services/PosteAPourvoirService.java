package fr.univrouen.poste.services;

import java.util.List;

import org.springframework.stereotype.Service;

import fr.univrouen.poste.domain.PosteAPourvoir;

@Service
public class PosteAPourvoirService {

	public List<PosteAPourvoir>  getCurrentPosteAPourvoirs() {
		// TODO dates début et fin de postes à pourvoir
		List<PosteAPourvoir> postes = PosteAPourvoir.findAllPosteAPourvoirs();
		return postes;
	}

}

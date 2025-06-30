package fr.univrouen.poste.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DematFileDummy implements DematFile {

	String filename;
	
	String fileSizeFormatted;
	
	public DematFileDummy(String filename, String fileSizeFormatted) {
		super();
		this.filename = filename;
		this.fileSizeFormatted = fileSizeFormatted;
	}

}

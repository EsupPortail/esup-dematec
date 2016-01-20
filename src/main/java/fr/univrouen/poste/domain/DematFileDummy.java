package fr.univrouen.poste.domain;

public class DematFileDummy implements DematFile {

	String filename;
	
	String fileSizeFormatted;
	
	public DematFileDummy(String filename, String fileSizeFormatted) {
		super();
		this.filename = filename;
		this.fileSizeFormatted = fileSizeFormatted;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getFileSizeFormatted() {
		return fileSizeFormatted;
	}

	public void setFileSizeFormatted(String fileSizeFormatted) {
		this.fileSizeFormatted = fileSizeFormatted;
	}

}

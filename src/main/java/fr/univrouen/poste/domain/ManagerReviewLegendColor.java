package fr.univrouen.poste.domain;

import java.util.ArrayList;
import java.util.List;

import org.springframework.roo.addon.javabean.RooJavaBean;
import org.springframework.roo.addon.tostring.RooToString;

import fr.univrouen.poste.domain.ManagerReview.ReviewStatusTypes;

@RooJavaBean
@RooToString
public class ManagerReviewLegendColor {
	
	public ManagerReviewLegendColor(String color, ReviewStatusTypes reviewStatus) {
		super();
		this.color = color;
		this.reviewStatus = reviewStatus;
	}

	private String color;
	
    private ReviewStatusTypes reviewStatus;
	

    public static String getColor(ReviewStatusTypes reviewStatus) {
    	String color = "#FFFFFF";
    	switch(reviewStatus) {
    	case Non_vue: 
    		color = AppliConfig.getCacheColorCandidatureNonVue();
    		break;
    	case Vue: 
    		color = AppliConfig.getCacheColorCandidatureVue();
    		break;
    	case Vue_incomplet: 
    		color = AppliConfig.getCacheColorCandidatureVueIncomplet();
    		break;
    	case Vue_incomplet_mais_modifie_depuis : 
    		color = AppliConfig.getCacheColorCandidatureVueIncompletModifieDepuis();
    		break;
    	case Vue_mais_modifie_depuis : 
    		color = AppliConfig.getCacheColorCandidatureVueModifieDepuis();
    		break;
    	default:
    		break;	   			
    	}
    	return color;
    }
    
	public static List<ManagerReviewLegendColor> getLegendColors() {
		List<ManagerReviewLegendColor> legendColors = new ArrayList<ManagerReviewLegendColor>();
		for(ReviewStatusTypes statusType : ReviewStatusTypes.values()) {
			legendColors.add(new ManagerReviewLegendColor(ManagerReviewLegendColor.getColor(statusType), statusType));
		}
		return legendColors;
	}
	
	
}

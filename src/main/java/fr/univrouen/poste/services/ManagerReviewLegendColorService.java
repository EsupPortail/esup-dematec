package fr.univrouen.poste.services;

import fr.univrouen.poste.domain.ManagerReview.ReviewStatusTypes;
import fr.univrouen.poste.domain.ManagerReviewLegendColor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ManagerReviewLegendColorService {

    @Autowired
    AppliConfigService appliConfigService;

    public String getColor(ReviewStatusTypes reviewStatus) {
        String color = "#FFFFFF";
        switch(reviewStatus) {
            case Non_vue:
                color = appliConfigService.getCacheColorCandidatureNonVue();
                break;
            case Vue:
                color = appliConfigService.getCacheColorCandidatureVue();
                break;
            case Vue_incomplet:
                color = appliConfigService.getCacheColorCandidatureVueIncomplet();
                break;
            case Vue_incomplet_mais_modifie_depuis :
                color = appliConfigService.getCacheColorCandidatureVueIncompletModifieDepuis();
                break;
            case Vue_mais_modifie_depuis :
                color = appliConfigService.getCacheColorCandidatureVueModifieDepuis();
                break;
            default:
                break;
        }
        return color;
    }

    public List<ManagerReviewLegendColor> getLegendColors() {
        List<ManagerReviewLegendColor> legendColors = new ArrayList<ManagerReviewLegendColor>();
        for(ReviewStatusTypes statusType : ReviewStatusTypes.values()) {
            legendColors.add(new ManagerReviewLegendColor(getColor(statusType), statusType));
        }
        return legendColors;
    }

    public Map<ReviewStatusTypes, String> getLegendColorsMap() {
        return Map.of(
                ReviewStatusTypes.Non_vue, getColor(ReviewStatusTypes.Non_vue),
                ReviewStatusTypes.Vue, getColor(ReviewStatusTypes.Vue),
                ReviewStatusTypes.Vue_incomplet, getColor(ReviewStatusTypes.Vue_incomplet),
                ReviewStatusTypes.Vue_incomplet_mais_modifie_depuis, getColor(ReviewStatusTypes.Vue_incomplet_mais_modifie_depuis),
                ReviewStatusTypes.Vue_mais_modifie_depuis, getColor(ReviewStatusTypes.Vue_mais_modifie_depuis)
        );
    }

}


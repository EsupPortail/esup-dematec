package fr.univrouen.poste.domain;

import fr.univrouen.poste.domain.ManagerReview.ReviewStatusTypes;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ManagerReviewLegendColor {
	
	public ManagerReviewLegendColor(String color, ReviewStatusTypes reviewStatus) {
		super();
		this.color = color;
		this.reviewStatus = reviewStatus;
	}

	String color;
	
    ReviewStatusTypes reviewStatus;

	public String getColor() {
        return this.color;
    }

	public void setColor(String color) {
        this.color = color;
    }

	public ReviewStatusTypes getReviewStatus() {
        return this.reviewStatus;
    }

	public void setReviewStatus(ReviewStatusTypes reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}

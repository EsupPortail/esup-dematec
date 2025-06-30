package fr.univrouen.poste.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

@Entity
@Getter
@Setter
public class AppliVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "my_seq")
    @SequenceGenerator(
            name = "my_seq",
            sequenceName = "hibernate_sequence",
            allocationSize = 1
    )
    Long id;

	
	String esupDematEcVersion;

	public String toString() {
        return ReflectionToStringBuilder.toString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}

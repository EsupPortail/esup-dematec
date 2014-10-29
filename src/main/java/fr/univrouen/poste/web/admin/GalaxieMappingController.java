package fr.univrouen.poste.web.admin;
import fr.univrouen.poste.domain.GalaxieMapping;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/admin/galaxiemapping")
@Controller
@RooWebScaffold(path = "admin/galaxiemapping", formBackingObject = GalaxieMapping.class, delete=false, create=false)
public class GalaxieMappingController {
}

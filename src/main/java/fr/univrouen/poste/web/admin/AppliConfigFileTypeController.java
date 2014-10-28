package fr.univrouen.poste.web.admin;
import fr.univrouen.poste.domain.AppliConfigFileType;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/admin/appliconfigfiletype")
@Controller
@RooWebScaffold(path = "admin/appliconfigfiletype", formBackingObject = AppliConfigFileType.class)
public class AppliConfigFileTypeController {
}

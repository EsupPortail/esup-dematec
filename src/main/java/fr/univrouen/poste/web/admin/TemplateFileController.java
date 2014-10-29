package fr.univrouen.poste.web.admin;
import fr.univrouen.poste.domain.TemplateFile;
import org.springframework.roo.addon.web.mvc.controller.scaffold.RooWebScaffold;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/admin/templatefiles")
@Controller
@RooWebScaffold(path = "admin/templatefiles", formBackingObject = TemplateFile.class)
public class TemplateFileController {
}

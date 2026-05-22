package fr.univrouen.poste.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @see PageableHandlerMethodArgumentResolver
 * Resolver de Pageable qui regarde dans la session si un paramètre size a été sauvegardé.
 * Si oui, il l'utilise pour créer le Pageable.
 * Si non, il utilise la valeur donnée dans le fichier de configuration (par défaut 20).
 * Si un paramètre size est présent dans la requête, il l'utilise et le sauvegarde en session.
 */
public class PrefAwarePageableResolver extends PageableHandlerMethodArgumentResolver {

    static final String SIZE_IN_SESSION = "paginationSize";

    int pageSize = 20;

    public void setPageSize(int defaultPageSize) {
        this.pageSize = defaultPageSize;
    }

    @Override
    public Pageable resolveArgument(MethodParameter methodParameter, @Nullable ModelAndViewContainer mavContainer, NativeWebRequest webRequest, @Nullable WebDataBinderFactory binderFactory) {
        Pageable pageable = super.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);

        HttpServletRequest httpRequest = webRequest.getNativeRequest(HttpServletRequest.class);
        if (httpRequest == null) {
            return pageable;
        }
        HttpSession session = httpRequest.getSession();

        String requestPagesize = webRequest.getParameter("size");
        if (StringUtils.isNotBlank(requestPagesize)) {
            session.setAttribute(SIZE_IN_SESSION, requestPagesize);
        }

        String sessionPagesize = (String) session.getAttribute(SIZE_IN_SESSION);
        int size = (sessionPagesize != null) ? Integer.parseInt(sessionPagesize) : pageSize;

        return PageRequest.of(pageable.getPageNumber(), size, pageable.getSort());
    }
}
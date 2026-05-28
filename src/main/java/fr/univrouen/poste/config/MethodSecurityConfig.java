package fr.univrouen.poste.config;

import fr.univrouen.poste.utils.PostePermissionEvaluator;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(MethodSecurityConfig.class);

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * Vérifie au démarrage que @EnableMethodSecurity est bien actif dans ce contexte.
     * Si ce message n'apparaît PAS dans les logs, les @PreAuthorize des @Controller
     * ne seront silencieusement pas appliqués.
     */
    @PostConstruct
    public void checkMethodSecurityActive() {
        boolean interceptorPresent =
                applicationContext.containsBean("preFilterAuthorizationMethodInterceptor")
                || applicationContext.containsBean("methodSecurityInterceptor")
                || applicationContext.containsBean("authorizationMethodInterceptor");

        if (interceptorPresent) {
            log.info("[SECURITY] @EnableMethodSecurity est ACTIF dans le contexte '{}' — " +
                     "@PreAuthorize/@PostAuthorize seront bien appliqués.",
                     applicationContext.getDisplayName());
        } else {
            log.error("[SECURITY] ATTENTION : @EnableMethodSecurity semble INACTIF dans le contexte '{}' ! " +
                      "Les annotations @PreAuthorize/@PostAuthorize sur les @Controller ne seront PAS appliquées. " +
                      "Vérifiez que MethodSecurityConfig est bien importé dans le contexte Web (WebMvcConfig).",
                      applicationContext.getDisplayName());
        }
    }

    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler(
            PostePermissionEvaluator permissionEvaluator) {
        DefaultMethodSecurityExpressionHandler handler = new DefaultMethodSecurityExpressionHandler();
        handler.setPermissionEvaluator(permissionEvaluator);
        return handler;
    }
}
/**
 * Licensed to ESUP-Portail under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * ESUP-Portail licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.univrouen.poste.config;

import fr.univrouen.poste.web.ApplicationConversionServiceFactoryBean;
import fr.univrouen.poste.web.PrefAwarePageableResolver;
import jakarta.annotation.Resource;
import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect;
import org.springframework.context.annotation.*;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.http.CacheControl;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.ui.context.support.ResourceBundleThemeSource;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;
import org.springframework.web.servlet.resource.VersionResourceResolver;
import org.springframework.web.servlet.theme.CookieThemeResolver;
import org.thymeleaf.dialect.springdata.SpringDataDialect;
import org.thymeleaf.extras.springsecurity6.dialect.SpringSecurityDialect;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.spring6.view.ThymeleafViewResolver;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * JavaConfig Spring MVC remplaçant webmvc-config.xml.
 * Hérite de WebMvcConfigurationSupport (équivalent de @EnableWebMvc) pour
 * pouvoir surcharger mvcConversionService() et y brancher
 * ApplicationConversionServiceFactoryBean — exactement comme
 * mvc:annotation-driven conversion-service="applicationConversionService".
 */
@Configuration
@EnableTransactionManagement
@Import(MethodSecurityConfig.class)
@ComponentScan(
        basePackages = "fr.univrouen.poste",
        useDefaultFilters = false,
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.ANNOTATION,
                        classes = org.springframework.stereotype.Controller.class),
                @ComponentScan.Filter(type = FilterType.ANNOTATION,
                        classes = org.springframework.web.bind.annotation.ControllerAdvice.class)
        }
)
public class WebMvcConfig extends WebMvcConfigurationSupport {

    @Resource
    private fr.univrouen.poste.web.ConfigInterceptor configInterceptor;

    /* ------------------------------------------------------------------ */
    /* Argument resolvers                                                   */
    /* ------------------------------------------------------------------ */

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        PrefAwarePageableResolver resolver = new PrefAwarePageableResolver();
        resolver.setPageSize(20);
        resolvers.add(resolver);
    }

    /* ------------------------------------------------------------------ */
    /* Ressources statiques                                                 */
    /* ------------------------------------------------------------------ */

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/resources/**")
                .addResourceLocations("/")
                .setCacheControl(CacheControl.maxAge(86400, TimeUnit.SECONDS).cachePublic())
                .resourceChain(true)
                .addResolver(new VersionResourceResolver().addContentVersionStrategy("/**"))
                .addResolver(new PathResourceResolver());
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    /* ------------------------------------------------------------------ */
    /* View controllers                                                     */
    /* ------------------------------------------------------------------ */

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/resourceNotFound");
        registry.addViewController("/dataAccessFailure");
    }

    /* ------------------------------------------------------------------ */
    /* Interceptors                                                         */
    /* ------------------------------------------------------------------ */

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(configInterceptor);
    }

    /* ------------------------------------------------------------------ */
    /* Conversion service                                                   */
    /* ------------------------------------------------------------------ */

    @Bean
    public ApplicationConversionServiceFactoryBean applicationConversionService() {
        return new ApplicationConversionServiceFactoryBean();
    }

    /**
     * Surcharge du ConversionService MVC — branche ApplicationConversionServiceFactoryBean.
     * Équivalent de : mvc:annotation-driven conversion-service="applicationConversionService"
     */
    @Bean
    @Override
    public FormattingConversionService mvcConversionService() {
        ApplicationConversionServiceFactoryBean factory = applicationConversionService();
        factory.afterPropertiesSet();
        return factory.getObject();
    }

    /* ------------------------------------------------------------------ */
    /* Locale / Thème                                                       */
    /* ------------------------------------------------------------------ */

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver resolver = new CookieLocaleResolver();
        resolver.setCookieName("locale");
        return resolver;
    }

    @Bean
    public ResourceBundleThemeSource themeSource() {
        return new ResourceBundleThemeSource();
    }

    @Bean
    public CookieThemeResolver themeResolver() {
        CookieThemeResolver resolver = new CookieThemeResolver();
        resolver.setCookieName("theme");
        resolver.setDefaultThemeName("standard");
        return resolver;
    }

    /* ------------------------------------------------------------------ */
    /* Thymeleaf                                                            */
    /* ------------------------------------------------------------------ */

    @Bean
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix("/WEB-INF/templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(org.thymeleaf.templatemode.TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);
        return resolver;
    }

    @Bean
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templateResolver());
        engine.setAdditionalDialects(Set.of(
                new LayoutDialect(),
                new SpringSecurityDialect(),
                new SpringDataDialect()
        ));
        return engine;
    }

    @Bean
    public ThymeleafViewResolver thymeleafViewResolver() {
        ThymeleafViewResolver resolver = new ThymeleafViewResolver();
        resolver.setTemplateEngine(templateEngine());
        resolver.setOrder(0);
        resolver.setCharacterEncoding("UTF-8");
        return resolver;
    }
}

package com.cambiaso.ioc.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
@Configuration
public class PageableConfig implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(@NonNull List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new CustomPageableHandlerMethodArgumentResolver());
    }

    static class CustomPageableHandlerMethodArgumentResolver extends PageableHandlerMethodArgumentResolver {

        private final ObjectMapper objectMapper = new ObjectMapper();

        public CustomPageableHandlerMethodArgumentResolver() {
            setOneIndexedParameters(false);
            setMaxPageSize(100);
            setFallbackPageable(PageRequest.of(0, 20, Sort.unsorted()));
        }

        @Override
        public @NonNull Pageable resolveArgument(@NonNull MethodParameter methodParameter,
                                                 @Nullable ModelAndViewContainer mavContainer,
                                                 @NonNull NativeWebRequest webRequest,
                                                 @Nullable WebDataBinderFactory binderFactory) {

            String sortParam = webRequest.getParameter("sort");

            // Handle JSON array format: ["name,asc"] or ["name","asc"]
            if (sortParam != null) {
                String sp = sortParam.trim();
                if (sp.startsWith("[") && sp.endsWith("]")) {
                 try {
                    // Parse JSON safely into a JsonNode and validate elements
                    JsonNode root = objectMapper.readTree(sp);
                    if (root != null && root.isArray() && root.size() > 0) {
                        List<Sort.Order> orders = new ArrayList<>();

                        // allow alphanumeric, dot, underscore and dash in property names
                        Pattern propertyPattern = Pattern.compile("^[A-Za-z0-9_.-]+$");

                        for (JsonNode node : root) {
                            String sortStr = node.isTextual() ? node.textValue() : null;
                            if (sortStr == null || sortStr.isEmpty()) continue;

                            String[] parts = sortStr.split(",");
                            if (parts.length < 1) continue;

                            String property = parts[0].trim();
                            if (property.isEmpty() || !propertyPattern.matcher(property).matches()) continue;

                            Sort.Direction direction = parts.length > 1 && "desc".equalsIgnoreCase(parts[1].trim())
                                ? Sort.Direction.DESC
                                : Sort.Direction.ASC;

                            orders.add(new Sort.Order(direction, property));
                        }

                        if (!orders.isEmpty()) {
                            // Get page and size parameters
                            String pageParam = webRequest.getParameter("page");
                            String sizeParam = webRequest.getParameter("size");

                            int page = pageParam != null ? Math.max(0, Integer.parseInt(pageParam)) : 0;
                            int size = sizeParam != null ? Math.min(100, Math.max(1, Integer.parseInt(sizeParam))) : 20;

                            return PageRequest.of(page, size, Sort.by(orders));
                        }
                    }
                 } catch (Exception e) {
                     log.warn("Failed to parse JSON array sort parameter: {}, falling back to default", sortParam, e);
                 }
                }
            }

             // Fall back to default Spring behavior
            try {
                 Pageable resolved = super.resolveArgument(methodParameter, mavContainer, webRequest, binderFactory);

                // resolved is @NonNull by contract from the superclass
                // If sort is empty, explicitly set to unsorted
                if (resolved.getSort().isUnsorted()) {
                     return PageRequest.of(
                         Math.max(0, resolved.getPageNumber()),
                         Math.min(100, Math.max(1, resolved.getPageSize())),
                         Sort.unsorted()
                     );
                 }

                 return resolved;
             } catch (Exception e) {
                 log.error("Error resolving pageable, using fallback", e);
                 return PageRequest.of(0, 20, Sort.unsorted());
             }
         }
     }
 }

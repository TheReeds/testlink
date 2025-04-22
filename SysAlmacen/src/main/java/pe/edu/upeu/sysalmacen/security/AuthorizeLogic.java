package pe.edu.upeu.sysalmacen.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class AuthorizeLogic {

    private static final Map<String, Set<String>> PATH_ROLES = Map.of(
        "findAll", Set.of("ADMIN"),
        "findById", Set.of("USER", "DBA"),
        "getById", Set.of("USER", "DBA"),
        "default", Set.of("ROOT")
    );

    public boolean hasAccess(String path) {
        // Obtener roles requeridos para el path
        Set<String> requiredRoles = PATH_ROLES.getOrDefault(path, PATH_ROLES.get("default"));
        
        // Obtener autenticación actual
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        // Logging de depuración
        log.debug("Validando acceso para usuario: {} en path: {}", auth.getName(), path);
        log.debug("Roles requeridos: {}", requiredRoles);

        Set<String> userRoles = auth.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .map(String::toUpperCase)
            .collect(Collectors.toSet());

        log.debug("Roles del usuario: {}", userRoles);

        return requiredRoles.stream()
            .anyMatch(requiredRole -> 
                userRoles.contains("ROLE_" + requiredRole) || 
                userRoles.contains(requiredRole)
            );
    }
}

package pe.edu.upeu.sysalmacen.control;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.upeu.sysalmacen.dtos.UsuarioDTO;
import pe.edu.upeu.sysalmacen.security.JwtTokenUtil;
import pe.edu.upeu.sysalmacen.security.JwtUserDetailsService;
import pe.edu.upeu.sysalmacen.servicio.IUsuarioService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final IUsuarioService userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final JwtUserDetailsService jwtUserDetailsService;
    @PostMapping("/login")
    public ResponseEntity<UsuarioDTO> login(HttpServletRequest request, @RequestBody @Valid UsuarioDTO userDto) {
        request.getSession().setAttribute("USER_SESSION", userDto.getUser());
        logger.debug("Sesión iniciada para usuario: {}", userDto.getUser());
        return ResponseEntity.ok(userDto);
    }

    @PostMapping("/register")
    public ResponseEntity<UsuarioDTO> register(@RequestBody @Valid UsuarioDTO.UsuarioCrearDto user) {
        logger.info("Registrando nuevo usuario con rol: {}", user.rol());
        
        UsuarioDTO createdUser = userService.register(user);
        final UserDetails userDetails = jwtUserDetailsService.loadUserByUsername(user.user());
        createdUser.setToken(jwtTokenUtil.generateToken(userDetails));
        
        logger.info("Usuario {} registrado exitosamente", createdUser.getUser());
        
        return ResponseEntity.created(URI.create("/users/" + createdUser.getUser())).body(createdUser);
    }
}

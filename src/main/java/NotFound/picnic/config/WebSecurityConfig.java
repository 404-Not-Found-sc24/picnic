package NotFound.picnic.config;

import NotFound.picnic.auth.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.security.config.Customizer.withDefaults;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@AllArgsConstructor
public class WebSecurityConfig implements WebMvcConfigurer {
    private final CustomUserDetailsService customUserDetailsService;
    private final JwtUtil jwtUtil;
    private final CustomAccessDeniedHandler accessDeniedHandler;
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;

    private static final String[] AUTH_WHITELIST = {
            "swagger-ui/**", "/api/auth/sign-up", "/api/auth/sign-in", "/api/auth/reissueToken"  // 여기에 포함되지 않은 모든 요청은 인증 필요
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // CSRF, CORS
        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(withDefaults());

        http.sessionManagement(sessionManagement -> sessionManagement.sessionCreationPolicy(
                SessionCreationPolicy.STATELESS
        ));
        // formLogin, basicHttp 비활성화
        http.formLogin(withDefaults());
        http.httpBasic(AbstractHttpConfigurer::disable);

        // JwtAuthFilter를 UsernamePasswordAuthenticationFilter 앞에 추가
        http.addFilterBefore(new JwtAuthFilter(customUserDetailsService, jwtUtil), UsernamePasswordAuthenticationFilter.class);

        http.exceptionHandling((exceptionHandling) -> exceptionHandling
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
        );

        // 권한 규칙 작성
        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(AUTH_WHITELIST).permitAll()
                //@PreAuthorization을 사용하는 경우 모든 경로에 대한 인증 처리는 permit
                .anyRequest().permitAll()
        );

        return http.build();
    }


    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://nadueli.com", "http://localhost:3000", "https://www.nadueli.com")
                .allowedMethods("GET", "POST", "PATCH", "DELETE")
                .allowCredentials(true);
    }
}

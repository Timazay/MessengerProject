package by.timazaytsev.messengerproject.api.common.security.filter;

import by.timazaytsev.messengerproject.api.common.security.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NotNull HttpServletResponse response, @NotNull FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        doFilter(request, response, filterChain, authHeader);
    }

    private void doFilter(HttpServletRequest request,
                          HttpServletResponse response,
                          FilterChain filterChain,
                          String authHeader) throws ServletException, IOException {
        final String jwt = authHeader.substring(7);
        final String username = jwtService.extractUsername(jwt);
        final String mail = jwtService.extractMail(jwt);

        if ((username != null || mail != null) && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = getUserDetails(username, mail);

            if (jwtService.validateToken(jwt)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    private UserDetails getUserDetails(String username, String mail) {
        if (username != null)
            return userDetailsService.loadUserByUsername(username);
        return userDetailsService.loadUserByUsername(mail);
    }
}

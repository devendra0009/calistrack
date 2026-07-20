package com.davendra.calistrack_backend.auth.filter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Validates Firebase ID tokens on incoming requests.
 * <p>
 * Uses {@code checkRevoked=false}: signature + expiry only. Session revoke on login is
 * handled via the DB refresh-token table. Checking revocation on every request breaks
 * login when {@code revokeRefreshTokens} was called (tokensValidAfterTime race).
 */
@Component
public class FirebaseAuthenticationFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(FirebaseAuthenticationFilter.class);

	@Override
	protected void doFilterInternal(
			HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain
	) throws ServletException, IOException {

		if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
			filterChain.doFilter(request, response);
			return;
		}

		String authorization = request.getHeader("Authorization");
		if (authorization != null && authorization.startsWith("Bearer ")) {
			String token = authorization.substring(7).trim();
			try {
				FirebaseToken decoded = FirebaseAuth.getInstance().verifyIdToken(token, false);
				request.setAttribute("firebaseUid", decoded.getUid());
				request.setAttribute("email", decoded.getEmail());

				var auth = new UsernamePasswordAuthenticationToken(
						decoded.getUid(),
						null,
						List.of(new SimpleGrantedAuthority("ROLE_USER"))
				);
				SecurityContextHolder.getContext().setAuthentication(auth);
			} catch (FirebaseAuthException e) {
				log.warn("Firebase ID token rejected on {}: {} ({})",
						request.getRequestURI(), e.getMessage(), e.getAuthErrorCode());
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setContentType("application/problem+json");
				response.getWriter().write(
						"{\"type\":\"https://calistrack.app/problems/invalid-token\","
								+ "\"title\":\"Unauthorized\",\"status\":401,"
								+ "\"detail\":\"Invalid or expired Firebase token\"}"
				);
				return;
			} catch (Exception e) {
				log.warn("Firebase ID token verification failed on {}: {}",
						request.getRequestURI(), e.toString());
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setContentType("application/problem+json");
				response.getWriter().write(
						"{\"type\":\"https://calistrack.app/problems/invalid-token\","
								+ "\"title\":\"Unauthorized\",\"status\":401,"
								+ "\"detail\":\"Invalid or expired Firebase token\"}"
				);
				return;
			}
		}

		filterChain.doFilter(request, response);
	}
}

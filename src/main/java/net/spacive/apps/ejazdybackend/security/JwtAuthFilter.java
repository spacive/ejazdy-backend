package net.spacive.apps.ejazdybackend.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import net.spacive.apps.ejazdybackend.config.CognitoConfiguration;
import net.spacive.apps.ejazdybackend.model.CognitoUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is spring boot filter implementation.
 *
 * <p>It is responsible for jwt verification and
 * user authorization based on groups claim.
 *
 * @author  Juraj Haluska
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    /**
     * HTTP Authorization header string.
     */
    private static final String AUTH_HEADER_STRING = "Authorization";

    /**
     * HTTP Authorization schema.
     */
    private static final String AUTH_BEARER_STRING = "Bearer";

    /**
     * Jwt attribute key for groups.
     */
    private static final String COGNITO_GROUP_CLAIM = "cognito:groups";

    /**
     * Cognito configuration properties.
     */
    private CognitoConfiguration properties;

    /**
     * cached key set from @see nimbusds.
     */
    private RemoteJWKSet remoteJWKSet;


    /**
     * Constructor.
     *
     * @param properties injected CognitoConfiguration.
     * @throws MalformedURLException if url of key store is invalid.
     */
    @Autowired
    public JwtAuthFilter(CognitoConfiguration properties) throws MalformedURLException {
        URL JWKUrl = new URL(properties.getIssuer() + properties.getKeyStorePath());
        this.remoteJWKSet = new RemoteJWKSet(JWKUrl);
        this.properties = properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest req,
            HttpServletResponse res,
            FilterChain chain) throws IOException, ServletException {

        String header = req.getHeader(AUTH_HEADER_STRING).replace(AUTH_BEARER_STRING, "");

        try {
            JWT jwt = JWTParser.parse(header);

            // check if issuer is our user pool
            if (properties.getIssuer().equals(jwt.getJWTClaimsSet().getIssuer())) {

                JWSKeySelector keySelector = new JWSVerificationKeySelector(JWSAlgorithm.RS256, remoteJWKSet);

                ConfigurableJWTProcessor jwtProcessor = new DefaultJWTProcessor();
                jwtProcessor.setJWSKeySelector(keySelector);

                // check token
                JWTClaimsSet claimsSet = jwtProcessor.process(jwt, null);

                // process roles (gropus in cognito)
                List<String> groups = (List<String>) claimsSet.getClaim(COGNITO_GROUP_CLAIM);
                List<GrantedAuthority> authorities = new ArrayList<>();

                groups.forEach(group ->
                        authorities.add(new SimpleGrantedAuthority(
                                properties.getGroupRoleMap().get(group)
                        ))
                );

                // process other claims
                final CognitoUser cognitoUser = new CognitoUser.Builder()
                        .withId(claimsSet.getSubject())
                        .withEmail((String) claimsSet.getClaim("email"))
                        .withPhone((String) claimsSet.getClaim("phone_number"))
                        .withFirstName((String) claimsSet.getClaim("given_name"))
                        .withLastName((String) claimsSet.getClaim("family_name"))
                        .withUserGroup(groups.get(0))   // group with highest precedence
                        .build();

                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                        cognitoUser,
                        null,
                        authorities
                );

                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        chain.doFilter(req, res);
    }
}
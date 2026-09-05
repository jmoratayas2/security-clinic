package com.clinicas.security.security;

import java.util.Collection;
import java.util.List;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthoritiesConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        List<String> values = jwt.getClaimAsStringList("authorities");
        Collection<SimpleGrantedAuthority> authorities = values == null ? List.of() : values.stream().map(SimpleGrantedAuthority::new).toList();
        return new JwtAuthenticationToken(jwt, authorities, jwt.getSubject());
    }
}

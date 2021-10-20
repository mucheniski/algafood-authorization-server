package com.example.algafoodauthorization;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    /*
    * O Client é o app, site ou serviço que vai buscar o token para consumir os recursos
    * não é o usuário, o usuário se loga no client
    *
    * o Padrão de tempo de expiração do refresh_token é de 30 dias, mas pode ser configurado também
    * */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients
            .inMemory()
                .withClient("algafood-web")
                .secret(passwordEncoder.encode("web123"))
                .authorizedGrantTypes("password", "refresh_token") // O padrão do refresh_token é de 30 dias
                .scopes("write", "read")
                .accessTokenValiditySeconds(horasExpiracao(6)) // 6 horas (padrão é 12 horas)
                .refreshTokenValiditySeconds(horasExpiracao(24)) // 24 horas
            .and()
                .withClient("aplicacao-terceira") // Pode ser em qualquer linguagem que consome nosso authorization
                .secret(passwordEncoder.encode("terceiro123"))
                .authorizedGrantTypes("client_credentials")
                .scopes("read")
            .and()
                .withClient("checktoken")
                .secret(passwordEncoder.encode("check123"))
            .and()
                .withClient("aplicacao-analitica") // Novo cliente que vai consumir autorização do authorization-server
                .secret(passwordEncoder.encode("analitica123"))
                .authorizedGrantTypes("authorization_code")
                .redirectUris("https://www.aplicacao-analitica/analise") // É preciso cadastrar as uris de retorno nesse fluxo, pode existir mais de uma, separadas por virgula.
                .scopes("write", "read");
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
//        security.checkTokenAccess("isAuthenticated()");
        security.checkTokenAccess("permitAll()");
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        endpoints
                .authenticationManager(authenticationManager) // Somente o fluxo password flow precisa do authenticationManager
                .userDetailsService(userDetailsService)
                .reuseRefreshTokens(false); // Toda vez que o refresh_token é usado ele é inválido, assim o client precisa logar de novo
    }

    private int horasExpiracao(int quantidadeHoras) {
        return 60 * 60 * quantidadeHoras;
    }

}

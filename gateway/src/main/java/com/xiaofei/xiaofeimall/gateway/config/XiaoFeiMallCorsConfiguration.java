package com.xiaofei.xiaofeimall.gateway.config;


import lombok.extern.log4j.Log4j2;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Log4j2
@Configuration
public class XiaoFeiMallCorsConfiguration {


    /**
     * 请求过来打印日志
     * @return
     */
    @Bean
    public GlobalFilter globalFilter() {
        return (exchange, chain) -> chain.filter(exchange).then(Mono.fromRunnable(() -> {
            // 在响应之后打印日志
            if (!exchange.getRequest().getURI().toString().contains("img")){
                log.info("请求路径:" + exchange.getRequest().getURI() + "\t" +
                        "请求方式: " + exchange.getRequest().getMethod().toString() + "\t" +
                        "路径对应的状态码: " + exchange.getResponse().getStatusCode());
            }
        }));
    }

    @Bean
    //SpringBoot已经提供了一个跨域filter
    public CorsWebFilter corsWebFilter(){

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.setAllowCredentials(true);

        source.registerCorsConfiguration("/**", corsConfiguration);
        //创建对象时需要传入跨域信息,这是响应式编程,所以都是reactive;
        return new CorsWebFilter(source);
    }
}

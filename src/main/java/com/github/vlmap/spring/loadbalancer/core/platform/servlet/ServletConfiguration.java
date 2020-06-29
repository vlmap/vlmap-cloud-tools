package com.github.vlmap.spring.loadbalancer.core.platform.servlet;

import com.github.vlmap.spring.loadbalancer.GrayLoadBalancerProperties;
import com.github.vlmap.spring.loadbalancer.core.platform.StrictFilter;
import com.github.vlmap.spring.loadbalancer.core.registration.MetaDataProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.mvc.MvcEndpoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Configuration
@ConditionalOnWebApplication
@EnableConfigurationProperties({GrayLoadBalancerProperties.class})

public class ServletConfiguration {



    @Bean
    public ReadBodyServletFilter readBodyFilter(GrayLoadBalancerProperties properties) {
        return new ReadBodyServletFilter(properties);
    }

    @Bean
    public AttacherServletFilter attacherFilter(GrayLoadBalancerProperties properties) {

        return new AttacherServletFilter(properties);
    }

    @Bean
    public ResponderServletFilter responderFilter(GrayLoadBalancerProperties properties) {

        return new ResponderServletFilter(properties);
    }

    @Bean

    public StrictServletFilter strictFilter(GrayLoadBalancerProperties properties) {

        return new StrictServletFilter(properties);
    }


    @Bean
    public RuntimeRouteTagFilter runtimeRouteTagFilter(GrayLoadBalancerProperties properties) {

        return new RuntimeRouteTagFilter(properties);
    }

    @ConditionalOnClass(Registration.class)
    @Configuration
    static class MetaDataProviderConfiguration {
        @Bean
        public MetaDataProvider metaDataProvider(Registration registration) {
            return new MetaDataProvider(registration);
        }
    }

    @Configuration
    @ConditionalOnClass(MvcEndpoint.class)
    public static class Acturator{
        @Autowired
        private Collection<MvcEndpoint> endpoints;
        @Autowired
        private StrictFilter filter;
        /**
         * spring boot1.5    版本 endpoint
         *
         */
        @PostConstruct
        public void initMethod() {
            List<String> list=new ArrayList<String>();
            for(MvcEndpoint endpoint:endpoints){
                list.add(endpoint.getPath());
            }
            List<String> temp=  filter.getIgnores();
            if(temp!=null){
                list.addAll(temp);
            }
            filter.setIgnores(list);
        }
    }

}

package com.asdf123.as3f.service.abs;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Created by l2 on 17/01/17.
 */

public abstract class AbstractServiceEndpoint {

    protected final RestTemplate restTemplate;

    public AbstractServiceEndpoint() {
        this.restTemplate = new RestTemplate();
        this.restTemplate.getMessageConverters().add(new MappingJackson2HttpMessageConverter());
    }

}

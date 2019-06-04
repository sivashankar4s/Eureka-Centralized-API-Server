package com.i2i.central.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.i2i.central.service.ServiceDefinitionsContext;
import com.i2i.central.service.ServiceDescriptionUpdater;

@RestController
public class CentralController {
    @Autowired
    private ServiceDefinitionsContext definitionContext;
    @Autowired
    private ServiceDescriptionUpdater serviceDescriptionUpdater;
    
    @GetMapping("/service/{servicename}")
    public String getServiceDefinition(@PathVariable("servicename") String serviceName){
        return definitionContext.getSwaggerDefinition(serviceName);
    }
    

    @GetMapping("/service/{servicename}/{serviceurl}")
    public void addServiceDefinition(@PathVariable("servicename") String serviceName,
            @PathVariable("serviceurl") String serviceurl) {
        serviceDescriptionUpdater.addSwaggerURL(serviceName, serviceurl);
    }
    @PostMapping("/service")
    public void addServiceDefinition(@RequestBody Map<String, String> data) {
        serviceDescriptionUpdater.addSwaggerURL(String.valueOf(data.get("servicename")), String.valueOf(data.get("serviceurl")));
    }
}

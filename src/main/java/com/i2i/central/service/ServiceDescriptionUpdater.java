package com.i2i.central.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;

/**
 * 
 * @author satish sharma
 * <pre>
 *   Periodically poll the service instaces and update the in memory store as key value pair	
 * </pre>
 */
@Component
public class ServiceDescriptionUpdater {
	
	private static final Logger logger = LoggerFactory.getLogger(ServiceDescriptionUpdater.class);
	
	private static final String DEFAULT_SWAGGER_URL="api/v1/v2/api-docs";
	private static final String KEY_SWAGGER_URL="swagger_url";
	private static final int STATUS_CODE=200;
	
	@Autowired
	private DiscoveryClient discoveryClient;

	private final RestTemplate template;
	
	public ServiceDescriptionUpdater(){
		this.template = new RestTemplate();
	}
	@Autowired
	private EurekaClient eurekaClient;
	@Autowired
	private ServiceDefinitionsContext definitionContext;
	
	@Scheduled(fixedDelayString= "${swagger.config.refreshrate}")
    public void refreshSwaggerConfigurations() {
        logger.debug("Starting Service Definition Context refresh");
        discoveryClient.getServices().stream().forEach(serviceId -> {
            logger.debug("Attempting service definition refresh for Service : {} ", serviceId);
            List<InstanceInfo> serviceInstances = eurekaClient.getApplication(serviceId).getInstances();
            System.out.println("===serviceInstances==" + serviceInstances);
            if (serviceInstances == null || serviceInstances.isEmpty()) {
                logger.info("No instances available for service : {} ", serviceId);
            } else {
                InstanceInfo instance = serviceInstances.get(0);
                String swaggerURL = instance.getHomePageUrl() + instance.getStatusPageUrl();
                Optional<Object> jsonData = getSwaggerDefinitionForAPI(serviceId, swaggerURL);
                
                if (jsonData.isPresent()) {
                    String content = getJSON(serviceId, jsonData.get());
                    definitionContext.addServiceDefinition(serviceId, content);
                } else {
                    logger.error(
                            "Skipping service id : {} Error : Could not get Swagegr definition from API ",
                            serviceId);
                }
                
                logger.info("Service Definition Context Refreshed at :  {}", LocalDate.now());
            }
        });
    }
    
    private Optional<Object> getSwaggerDefinitionForAPI(String serviceName, String url) {
        logger.debug("Accessing the SwaggerDefinition JSON for Service : {} : URL : {} ", serviceName, url);
        try {
            Object jsonData = template.getForObject(url, Object.class);
            return Optional.of(jsonData);
        } catch (RestClientException ex) {
            logger.error("Error while getting service definition for service : {} Error : {} ", serviceName,
                    ex.getMessage());
            return Optional.empty();
        }
    }
    public void addSwaggerURL(String serviceId, String url) {
        String swaggerURL = url+DEFAULT_SWAGGER_URL;
        Optional<Object> jsonData = getSwaggerDefinitionForAPI(serviceId, swaggerURL);
        
        if(jsonData.isPresent()){
            String content = getJSON(serviceId, jsonData.get());
            definitionContext.addServiceDefinition(serviceId, content);
        }else{
            logger.error("Skipping service id : {} Error : Could not get Swagegr definition from API ",serviceId);
        }
        
        logger.info("Service Definition Context Refreshed at :  {}",LocalDate.now());
    }
    
	
	private Optional<Object> getSwaggerDefinitionForAPIWithAuth(String serviceName, String url){
		logger.debug("Accessing the SwaggerDefinition JSON for Service : {} : URL : {} ", serviceName, url);
		try{
		    HttpHeaders headers = new HttpHeaders();
		    headers.add("tenantidentifier", "localhost_unittest");

		    HttpEntity<String> entity = new HttpEntity<>("body", headers);
		    
		    ResponseEntity<Object> response  = template.exchange(url, HttpMethod.GET, entity, Object.class);
            if (null != response.getBody()) {
                Object jsonData = response.getBody();
                return Optional.of(jsonData);
            }
		}catch(RestClientException ex){
			logger.error("Error while getting service definition for service : {} Error : {} ", serviceName, ex.getMessage());
			return Optional.empty();
		}
		return Optional.empty();
	}

	public String getJSON(String serviceId, Object jsonData){
		try {
			return new ObjectMapper().writeValueAsString(jsonData);
		} catch (JsonProcessingException e) {
			logger.error("Error : {} ", e.getMessage());
			return "";
		}
	}
}

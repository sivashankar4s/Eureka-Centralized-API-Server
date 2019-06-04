Eureka client config example 

eureka:
  instance:
    appname: {server name}
    statusPageUrl: {swagger url}
  client:
    enabled: true
    register-with-eureka: true
    fetch-registry: true
    service-url:
      defaultZone: http://localhost:8761/eureka/

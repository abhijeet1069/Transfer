# Microservices

## Microservice patterns

-  Core development patterns
  - Service granularity : Ensuring each microservice has the right level of responsibility.
  - Communication protocols : JSON
  - Interface design : How to structure service URLs to communicate intent?
  - Configuration Management of service : How to move between different environments in cloud so as to never change app code or config?
  - Event processing between services
    
-  Routing patterns
  How do I get my client's request for a service to a specific instance of a service?
  -  Service Discovery : How do you make your microservice discoverable so client applications can find them without having the location
                         of the service hardcoded into the application.
  -  Service routing : How do you provide a single entry point of all your services so that security policies and routing rules are applied
                       uniformly to multiple services and service instances in your microservice solution.
    
-  Client resiliency patterns
-  Security patterns
-  Logging and tracing patterns
-  Build and deployment patterns


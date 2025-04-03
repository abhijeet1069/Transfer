# Microservice patterns

## Core development patterns
-  Service granularity : Ensuring each microservice has the right level of responsibility.
-  Communication protocols : JSON
-  Interface design : How to structure service URLs to communicate intent?
-  Configuration Management of service : How to move between different environments in cloud so as to never change app code or config?
-  Event processing between services
    
## Routing patterns

How do I get my client's request for a service to a specific instance of a service?

-  Service Discovery : How do you make your microservice discoverable so client applications can find them without having the location
                         of the service hardcoded into the application.
-  Service routing : How do you provide a single entry point of all your services so that security policies and routing rules are applied
                       uniformly to multiple services and service instances in your microservice solution.
    
## Client resiliency patterns

Preventing problems of a single service from cascading up and out to the consumers of the service.

- Client side load balancing :  How do you cache the location of your service instances on the service client so that calls to multiple instances of a microservice
                                are load balanced to all the health instances of that microservice?
- Circuit breaker pattern : How do you prevent a client from continuing to call a service that is failing or suffering performance problems?
- Fallback pattern : When a service call fails, how do you provide a “plug-in” mechanism that will allow the service client to try to carry out its work through alternative
                     means other than the microservice being called?
- Bulkhead pattern : Microservice applications use multiple distributed resources to carry out their work. How do you compartmentalize these calls so that the misbehavior
                     of one service call doesn’t negatively impact the rest of the application?
  
## Security patterns

- Authentication : How do you determine the service client calling the service is who they say they are?
- Authorization : How do you determine whether the service client calling a microservice is allowed to undertake the action they’re trying to undertake?
- Credential management and propagation : How do you prevent a service client from constantly having to present their credentials for service calls involved in a transaction?

## Logging and tracing patterns : 

- Log correlation : How do you tie together all the logs produced between services for a single user transaction? With this pattern, we’ll look at how to implement a
                    correlation ID, which is a unique identifier that will be carried across all service calls in a transaction and can be used to tie together log
                    entries produced from each service.
- Log aggregation : With this pattern we’ll look at how to pull together all of the logs produced by your microservices (and their individual instances) into a single
                    queryable database. We’ll also look at how to use correlation IDs to assist in searching your aggregated logs.
- Microservice tracing : Finally, we’ll explore how to visualize the flow of a client transaction across all the services involved and understand the performance
                         characteristics of services involved in the transaction.

## Build and deployment patterns

One of the core parts of a microservice architecture is that each instance of a microservice should be identical to all its other instances. You can’t allow “configuration
drift” (something changes on a server after it’s been deployed) to occur, because this can introduce instability in your applications.

- Build and deployment pipeline : How do you create a repeatable build and deployment process that emphasizes one button builds and deployment to any environment in your organization?
- Infrastructure as code : How do you treat the provisioning of your services as code that can be executed and managed under source control?
- Immutable Infrastructure : Once a microservice image is created, how do you ensure that it’s never changed after it has been deployed?
- Phoenix Servers : The longer a server is running, the more opportunity for configuration drift. How do you ensure that servers that run microservices get torn
                    down on a regular basis and recreated off an immutable image?


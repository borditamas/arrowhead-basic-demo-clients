package com.porto.demo.consumer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.HttpMethod;

import eu.arrowhead.client.library.ArrowheadService;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.dto.shared.OrchestrationFlags.Flag;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO;
import eu.arrowhead.common.dto.shared.OrchestrationFormRequestDTO.Builder;
import eu.arrowhead.common.dto.shared.OrchestrationResponseDTO;
import eu.arrowhead.common.dto.shared.OrchestrationResultDTO;
import eu.arrowhead.common.dto.shared.ServiceQueryFormDTO;
import eu.arrowhead.common.exception.ArrowheadException;

@SpringBootApplication
@ComponentScan(basePackages = {CommonConstants.BASE_PACKAGE}) //TODO: add custom packages if any
public class ConsumerMain implements ApplicationRunner {
    
    //=================================================================================================
	// members
	
	public static final String GET_RANDOM_NUMBER_SERVICE_DEFINITION = "random-numbers";
	
    @Autowired
	private ArrowheadService arrowheadService;
    
	private final Logger logger = LogManager.getLogger( ConsumerMain.class );
    
    //=================================================================================================
	// methods

	//------------------------------------------------------------------------------------------------
    public static void main( final String[] args ) {
    	SpringApplication.run(ConsumerMain.class, args);
    }

    //-------------------------------------------------------------------------------------------------
    @Override
	public void run(final ApplicationArguments args) throws Exception {
    	while (true) {						
			//INITIATING THE ORCHESTRATION
	    	
	    	final Builder orchestrationFormBuilder = arrowheadService.getOrchestrationFormBuilder();
	    	
	    	final ServiceQueryFormDTO requestedService = new ServiceQueryFormDTO();
	    	requestedService.setServiceDefinitionRequirement(GET_RANDOM_NUMBER_SERVICE_DEFINITION);
	    	
	    	orchestrationFormBuilder.requestedService(requestedService)
	    							.flag(Flag.MATCHMAKING, true)
	    							.flag(Flag.OVERRIDE_STORE, true)
	    							.flag(Flag.TRIGGER_INTER_CLOUD, false);
	    	
	    	final OrchestrationFormRequestDTO orchestrationRequest = orchestrationFormBuilder.build();
	    	
	    	OrchestrationResponseDTO response = null;
	    	try {
	    		response = arrowheadService.proceedOrchestration(orchestrationRequest);			
			} catch (final ArrowheadException ex) {
				logger.info(ex.getMessage());
			}
	    	
	    	//CONSUMING THE SERVICE
	    	
	    	if (response == null || response.getResponse().isEmpty()) {
	    		logger.info("Orchestration response is empty");
	    		return;
	    	}
	    	
	    	final OrchestrationResultDTO result = response.getResponse().get(0);
	    	
	    	final HttpMethod httpMethod = HttpMethod.valueOf(result.getMetadata().get("http-method"));
	    	final String address = result.getProvider().getAddress();
	    	final int port = result.getProvider().getPort();
	    	final String serviceUri = result.getServiceUri();
	    	final String interfaceName = result.getInterfaces().get(0).getInterfaceName(); //Simplest way of choosing an interface.
	    	String token = null;
	    	if (result.getAuthorizationTokens() != null) {
	    		token = result.getAuthorizationTokens().get(interfaceName); //Can be null when the security type of the provider is 'CERTIFICATE' or nothing.
			}
	    	final Object payload = null; //Can be null if not specified in the description of the service.
	    	
	    	final String consumedService = arrowheadService.consumeServiceHTTP(String.class, httpMethod, address, port, serviceUri, interfaceName, token, payload);
	    	System.out.println(consumedService);
	    	Thread.sleep(2000);
    	}
	}
}

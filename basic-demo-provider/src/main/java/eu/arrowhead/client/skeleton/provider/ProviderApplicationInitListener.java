package eu.arrowhead.client.skeleton.provider;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.porto.demo.provider.controller.ProviderController;

import eu.arrowhead.client.library.ArrowheadService;
import eu.arrowhead.client.library.config.ApplicationInitListener;
import eu.arrowhead.client.library.util.ClientCommonConstants;
import eu.arrowhead.client.skeleton.provider.security.ProviderSecurityConfig;
import eu.arrowhead.common.CommonConstants;
import eu.arrowhead.common.Utilities;
import eu.arrowhead.common.core.CoreSystem;
import eu.arrowhead.common.dto.shared.ServiceRegistryRequestDTO;
import eu.arrowhead.common.dto.shared.ServiceSecurityType;
import eu.arrowhead.common.dto.shared.SystemRequestDTO;
import eu.arrowhead.common.exception.ArrowheadException;

@Component
public class ProviderApplicationInitListener extends ApplicationInitListener {
	
	//=================================================================================================
	// members
	
	@Autowired
	private ArrowheadService arrowheadService;
	
	@Autowired
	private ProviderSecurityConfig providerSecurityConfig;
	
	@Value(ClientCommonConstants.$TOKEN_SECURITY_FILTER_ENABLED_WD)
	private boolean tokenSecurityFilterEnabled;
	
	@Value(CommonConstants.$SERVER_SSL_ENABLED_WD)
	private boolean sslEnabled;
	
	@Value(ClientCommonConstants.$CLIENT_SYSTEM_NAME)
	private String mySystemName;
	
	@Value(ClientCommonConstants.$CLIENT_SERVER_ADDRESS_WD)
	private String mySystemAddress;
	
	@Value(ClientCommonConstants.$CLIENT_SERVER_PORT_WD)
	private int mySystemPort;
	
	private final Logger logger = LogManager.getLogger(ProviderApplicationInitListener.class);
	
	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@Override
	protected void customInit(final ContextRefreshedEvent event) {

		//Checking the availability of necessary core systems
		checkCoreSystemReachability(CoreSystem.SERVICE_REGISTRY);
		if (sslEnabled && tokenSecurityFilterEnabled) {
			checkCoreSystemReachability(CoreSystem.AUTHORIZATION);			

			//Initialize Arrowhead Context
			arrowheadService.updateCoreServiceURIs(CoreSystem.AUTHORIZATION);			
		}		
		
		setTokenSecurityFilter();
		
		//Registering my service 
		SystemRequestDTO mySystem = new SystemRequestDTO();
		mySystem.setSystemName(mySystemName);
		mySystem.setAddress(mySystemAddress);
		mySystem.setPort(mySystemPort);
		
		ServiceRegistryRequestDTO serviceRegistryRequestDTO = new ServiceRegistryRequestDTO();
		serviceRegistryRequestDTO.setProviderSystem(mySystem);
		serviceRegistryRequestDTO.setServiceDefinition(ProviderController.GET_RANDOM_NUMBER_SERVICE_DEFINITION);
		serviceRegistryRequestDTO.setServiceUri(ProviderController.GET_RANDOM_NUMBER_SERVICE_URI);
		serviceRegistryRequestDTO.setInterfaces(List.of(ProviderController.GET_RANDOM_NUMBER_SERVICE_INTERFACE_INSECURE, ProviderController.GET_RANDOM_NUMBER_SERVICE_INTERFACE_SECURE));
		serviceRegistryRequestDTO.setSecure(sslEnabled ? (tokenSecurityFilterEnabled? ServiceSecurityType.TOKEN : ServiceSecurityType.CERTIFICATE) : ServiceSecurityType.NOT_SECURE);
		serviceRegistryRequestDTO.getMetadata().put("http-method", ProviderController.GET_RANDOM_NUMBER_SERVICE_HTTP_METHOD);
		arrowheadService.forceRegisterServiceToServiceRegistry(serviceRegistryRequestDTO);
	}
	
	//-------------------------------------------------------------------------------------------------
	@Override
	public void customDestroy() {
		arrowheadService.unregisterServiceFromServiceRegistry(ProviderController.GET_RANDOM_NUMBER_SERVICE_DEFINITION);
	}
	
	//=================================================================================================
	// assistant methods

	//-------------------------------------------------------------------------------------------------
	private void setTokenSecurityFilter() {
		if(!tokenSecurityFilterEnabled || !sslEnabled) {
			logger.info("TokenSecurityFilter in not active");
		} else {
			final PublicKey authorizationPublicKey = arrowheadService.queryAuthorizationPublicKey();
			if (authorizationPublicKey == null) {
				throw new ArrowheadException("Authorization public key is null");
			}
			
			KeyStore keystore;
			try {
				keystore = KeyStore.getInstance(sslProperties.getKeyStoreType());
				keystore.load(sslProperties.getKeyStore().getInputStream(), sslProperties.getKeyStorePassword().toCharArray());
			} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException ex) {
				throw new ArrowheadException(ex.getMessage());
			}			
			final PrivateKey providerPrivateKey = Utilities.getPrivateKey(keystore, sslProperties.getKeyPassword());

			providerSecurityConfig.getTokenSecurityFilter().setAuthorizationPublicKey(authorizationPublicKey);
			providerSecurityConfig.getTokenSecurityFilter().setMyPrivateKey(providerPrivateKey);
		}
	}
}

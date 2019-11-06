package com.porto.demo.provider.controller;

import java.util.Random;

import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProviderController {
	
	//=================================================================================================
	// members

	public static final String GET_RANDOM_NUMBER_SERVICE_DEFINITION = "random-numbers";
	public static final String GET_RANDOM_NUMBER_SERVICE_URI = "/rnd";
	public static final String GET_RANDOM_NUMBER_SERVICE_HTTP_METHOD = HttpMethod.GET.name();
	public static final String GET_RANDOM_NUMBER_SERVICE_INTERFACE_INSECURE = "HTTP-INSECURE-TEXT";
	public static final String GET_RANDOM_NUMBER_SERVICE_INTERFACE_SECURE = "HTTP-SECURE-TEXT";	

	//=================================================================================================
	// methods

	//-------------------------------------------------------------------------------------------------
	@GetMapping(path = GET_RANDOM_NUMBER_SERVICE_URI, produces = MediaType.TEXT_PLAIN_VALUE)
	public int getRandomNumber() {
		return new Random().nextInt();
	}
}

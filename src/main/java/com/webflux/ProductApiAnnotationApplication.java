package com.webflux;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.webflux.handler.ProductHandler;
import com.webflux.model.Product;
import com.webflux.repository.ProductRepository;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class ProductApiAnnotationApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductApiAnnotationApplication.class, args);
	}
	
	
	@Bean
	CommandLineRunner init(ProductRepository repository) {
		return args -> {
			Flux<Product> productFlux = Flux.just(
					new Product(null, "Big Latte", 2.99), 
					new Product(null, "Big Decaf", 2.49), 
					new Product(null, "Green Tea", 1.99))
					.flatMap(p -> repository.save(p));
			
			productFlux.thenMany(repository.findAll())
						.subscribe(System.out::println);
		};
	}
	
	@Bean
	RouterFunction<ServerResponse> routes(ProductHandler handler ) {
		//The order is important to avoid confuse {id} with a route
		/*return RouterFunctions
				.route(RequestPredicates.GET("/productsFunc").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::getAllProducts)
				.andRoute(RequestPredicates.POST("/productsFunc").and(RequestPredicates.contentType(MediaType.APPLICATION_JSON)), handler::saveProduct)
				.andRoute(RequestPredicates.DELETE("/productsFunc").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::deleteAll)
				.andRoute(RequestPredicates.GET("/productsFunc/events").and(RequestPredicates.accept(MediaType.TEXT_EVENT_STREAM)), handler::getProductEvents)
				.andRoute(RequestPredicates.GET("/products/{id}").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::getProduct)
				.andRoute(RequestPredicates.PUT("/products/{id}").and(RequestPredicates.contentType(MediaType.APPLICATION_JSON)), handler::updateProduct)
				.andRoute(RequestPredicates.DELETE("/products/{id}").and(RequestPredicates.accept(MediaType.APPLICATION_JSON)), handler::deleteProduct);*/
		return RouterFunctions.nest(RequestPredicates.path("productsFunc"), 
					RouterFunctions.nest(RequestPredicates.accept(MediaType.APPLICATION_JSON).or(RequestPredicates.contentType(MediaType.APPLICATION_JSON)).or(RequestPredicates.contentType(MediaType.TEXT_EVENT_STREAM)), 
				
						RouterFunctions.route(RequestPredicates.GET(""), handler::getAllProducts)
						.andRoute(RequestPredicates.POST(""), handler::saveProduct)
						.andRoute(RequestPredicates.DELETE(""), handler::deleteAll)
						.andRoute(RequestPredicates.GET("/events"), handler::getProductEvents)
						.andNest(RequestPredicates.path("/{id}"), 
								RouterFunctions.route(RequestPredicates.method(HttpMethod.GET), handler::getProduct)
												.andRoute(RequestPredicates.method(HttpMethod.PUT), handler::updateProduct)
												.andRoute(RequestPredicates.method(HttpMethod.DELETE), handler::deleteProduct)
						)
					)
				);
	}
	

}

package com.webflux.handler;

import java.time.Duration;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.webflux.model.Product;
import com.webflux.model.ProductEvent;
import com.webflux.repository.ProductRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class ProductHandler {
	
private ProductRepository repository;
	
	public ProductHandler(ProductRepository repository) {
		this.repository = repository;
	}
	
	
	public Mono<ServerResponse> getAllProducts(ServerRequest request) {
		Flux<Product> products = repository.findAll();
		
		return ServerResponse.ok()
				.contentType(MediaType.APPLICATION_JSON)
				.body(products, Product.class);
	}
	
	public Mono<ServerResponse> getProduct(ServerRequest request) {
		String id = request.pathVariable("id");
		
		Mono<Product> productMono = repository.findById(id);
		Mono<ServerResponse> notFound = ServerResponse.notFound().build();
		
		return productMono
				.flatMap(p -> ServerResponse.ok()
							.contentType(MediaType.APPLICATION_JSON)
							.body(BodyInserters.fromObject(p))
							)
				.switchIfEmpty(notFound);
	}
	
	public Mono<ServerResponse> saveProduct(ServerRequest request) {
		Mono<Product> productMono = request.bodyToMono(Product.class);
		
		return productMono.flatMap(p -> 
			ServerResponse.status(HttpStatus.CREATED)
			.contentType(MediaType.APPLICATION_JSON)
			.body(repository.save(p), Product.class));
				
	}
	
	public Mono<ServerResponse> updateProduct(ServerRequest request) {
		String id = request.pathVariable("id");
		
		Mono<Product> existingProductMono = repository.findById(id);
		Mono<Product> productMono = request.bodyToMono(Product.class);
		Mono<ServerResponse> notFound = ServerResponse.notFound().build();
		
		return productMono
				.zipWith(existingProductMono, (product, existingProduct) -> new Product(existingProduct.getId(), product.getName(), product.getPrice()))
				.flatMap(p -> ServerResponse.ok()
							.contentType(MediaType.APPLICATION_JSON)
							.body(repository.save(p), Product.class)
							)
				.switchIfEmpty(notFound);
	}
	
	public Mono<ServerResponse> deleteProduct(ServerRequest request) {
		String id = request.pathVariable("id");
		
		Mono<Product> productMono = repository.findById(id);
		Mono<ServerResponse> notFound = ServerResponse.notFound().build();
				
		return productMono
				.flatMap(existingProduct -> ServerResponse.ok().build(repository.delete(existingProduct)))
				.switchIfEmpty(notFound);
	}
	
	public Mono<ServerResponse> deleteAll(ServerRequest request) {	
		return ServerResponse.ok().build(repository.deleteAll());
	}
	
	public Mono<ServerResponse> getProductEvents(ServerRequest request) {
		Flux<ProductEvent> eventsFlux = Flux.interval(Duration.ofSeconds(1)).map(v -> new ProductEvent(v, "Event"));
				
		return ServerResponse.ok()
				.contentType(MediaType.TEXT_EVENT_STREAM)
				.body(eventsFlux, ProductEvent.class);
	}
	
	
	
	
	

}

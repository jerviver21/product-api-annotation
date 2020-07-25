package com.webflux.controller;

import java.time.Duration;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.webflux.model.Product;
import com.webflux.model.ProductEvent;
import com.webflux.repository.ProductRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/products")
public class ProductController {
	
	private ProductRepository repository;
	
	public ProductController(ProductRepository repository) {
		this.repository = repository;
	}
	
	@GetMapping
	public Flux<Product> getAllProducts() {
		return repository.findAll();
	}
	
	//ResponseEntity is value and the specific status, could be not found for instance
	@GetMapping("{id}")
	public Mono<ResponseEntity<Product>> getProduct(@PathVariable String id) {
		return repository.findById(id)
				.map(p -> ResponseEntity.ok(p))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public Mono<Product> saveProduct(@RequestBody Product product) {
		return repository.save(product);
	}
	
	
	@PutMapping("{id}")
	public Mono<ResponseEntity<Product>> updateProduct(@PathVariable String id, @RequestBody Product product) {
		return repository.findById(id)
				.flatMap(existingProduct -> {
					existingProduct.setName(product.getName());
					existingProduct.setPrice(product.getPrice());
					return repository.save(existingProduct);
				}).map(updateProduct -> ResponseEntity.ok(updateProduct))
				.defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@DeleteMapping("{id}")
	public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable String id) {
		return repository.findById(id)
				.flatMap(existingProduct -> 
					repository.delete(existingProduct)
					.then(Mono.just(ResponseEntity.ok().<Void>build()))
				).defaultIfEmpty(ResponseEntity.notFound().build());
	}
	
	@DeleteMapping
	public Mono<Void> deleteAllProducts() {
		return repository.deleteAll();
	}
	
	@GetMapping(value = "/events", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<ProductEvent> getProductEvents() {
		return Flux.interval(Duration.ofSeconds(1)).map(v -> new ProductEvent(v, "Event"));
	}

}

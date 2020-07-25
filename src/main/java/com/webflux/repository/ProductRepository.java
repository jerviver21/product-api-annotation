package com.webflux.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import com.webflux.model.Product;

import reactor.core.publisher.Flux;

public interface ProductRepository extends ReactiveMongoRepository<Product, String>{
	Flux<Product> findByNameOrderByPrice(String name);
}

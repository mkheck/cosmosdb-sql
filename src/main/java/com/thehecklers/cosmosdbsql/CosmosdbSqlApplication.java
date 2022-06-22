package com.thehecklers.cosmosdbsql;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.GeneratedValue;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import com.azure.spring.data.cosmos.repository.ReactiveCosmosRepository;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.annotation.Id;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CosmosdbSqlApplication {

	public static void main(String[] args) {
		SpringApplication.run(CosmosdbSqlApplication.class, args);
	}

}

@Component
class DataLoader {
	private final UserRepository repo;

	DataLoader(UserRepository repo) {
		this.repo = repo;
	}

	@PostConstruct
	void loadData() {
		repo.deleteAll()
				.thenMany(Flux.just(new User("Alpha", "Bravo", "123 N 4567th St"),
						new User("Charlie", "Delta", "1313 Mockingbird Lane")))
				.flatMap(repo::save)
				.thenMany(repo.findAll())
				.subscribe(System.out::println);
	}
}

@RestController
class CosmosSqlController {
	private final UserRepository repo;

	CosmosSqlController(UserRepository repo) {
		this.repo = repo;
	}

	@GetMapping
	Flux<User> getAllUsers() {
		return repo.findAll();
	}
}

interface UserRepository extends ReactiveCosmosRepository<User, Long> {}

//@Container(containerName = "data")
//record User(@Id Long id, String firstName, @PartitionKey String lastName, String address) {}

@Container(containerName = "data")
@Data
@NoArgsConstructor
@RequiredArgsConstructor
class User {
	@Id
	@GeneratedValue
	private String id;
	@NonNull
	@PartitionKey
	private String firstName;
	@NonNull
	private String lastName, address;
}

package edu.fudan.main.repository;


import edu.fudan.main.domain.User;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends Neo4jRepository<User, Long>{
    Optional<User> findByEmail(String email);

    List<User> findByName(String name);

}

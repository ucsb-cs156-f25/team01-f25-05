package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.Articles;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/** The ArticlesRepository is a repository for Articles entities. */
@Repository
public interface ArticlesRepository extends CrudRepository<Articles, Long> {
  /**
   * This method returns all Articles entities with a given email.
   *
   * @param email the email of the user who added the article
   * @return all Articles entities with the given email
   */
  Iterable<Articles> findAllByEmail(String email);
}

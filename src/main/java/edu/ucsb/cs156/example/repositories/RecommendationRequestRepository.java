package edu.ucsb.cs156.example.repositories;

import edu.ucsb.cs156.example.entities.RecommendationRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecommendationRequestRepository
    extends CrudRepository<RecommendationRequest, Long> {
  /**
   * This method returns all UCSBDate entities with a given quarterYYYYQ.
   *
   * @param requesterEmail
   * @return all Request info for the requester
   */
  Iterable<RecommendationRequest> findAllByRequesterEmail(String requesterEmail);
}

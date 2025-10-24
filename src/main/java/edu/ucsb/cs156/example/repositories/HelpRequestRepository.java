package edu.ucsb.cs156.dining.repositories;

import edu.ucsb.cs156.dining.entities.HelpRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/** The HelpRequestRepository is a repository for HelpRequest entities. */
@Repository
public interface HelpRequestRepository extends CrudRepository<HelpRequest, Long> {
  Iterable<HelpRequest> findByRequesterEmail(String requesterEmail);

  Iterable<HelpRequest> findByTeamId(String teamId);

  Iterable<HelpRequest> findBySolved(boolean solved);
}

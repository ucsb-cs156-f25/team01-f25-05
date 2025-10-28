package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for UCSBDates */
@Tag(name = "UCSBDates")
@RequestMapping("/api/recommendationrequest")
@RestController
@Slf4j
public class RecommendationRequestController extends ApiController {

  @Autowired RecommendationRequestRepository recReqRepository;

  /**
   * List all Recommendation Requests
   *
   * @return an iterable of Recommendation Requests
   */
  @Operation(summary = "List all Recommendation Requests")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<RecommendationRequest> allUCSBDates() {
    Iterable<RecommendationRequest> reqs = recReqRepository.findAll();
    return reqs;
  }

  /**
   * Get a single request by id
   *
   * @param id the id of the date
   * @return a Recommendation Request
   */
  @Operation(summary = "Get a single Recommendation Request")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public RecommendationRequest getById(@Parameter(name = "id") @RequestParam Long id) {
    RecommendationRequest req =
        recReqRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

    return req;
  }

  /**
   * Create a new recommendation request
   *
   * @param requesterEmail the eamail of requester
   * @param professorEmail the email of professor
   * @param explanation the explanation
   * @param dateRequested the data requested
   * @param dateNeeded the data needed
   * @param done the status
   * @return the saved request
   */
  @Operation(summary = "Create a new Request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public RecommendationRequest postRecommendationRequest(
      @Parameter(name = "requesterEmail") @RequestParam String requesterEmail,
      @Parameter(name = "professorEmail") @RequestParam String professorEmail,
      @Parameter(name = "explanation") @RequestParam String explanation,
      @Parameter(
              name = "dateRequested",
              description =
                  "date (in iso format, e.g. YYYY-mm-ddTHH:MM:SSZ; see https://en.wikipedia.org/wiki/ISO_8601)")
          @RequestParam("dateRequested")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateRequested,
      @Parameter(
              name = "dateNeeded",
              description =
                  "date (in iso format, e.g. YYYY-mm-ddTHH:MM:SSZ; see https://en.wikipedia.org/wiki/ISO_8601)")
          @RequestParam("dateNeeded")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateNeeded,
      @Parameter(name = "done") @RequestParam boolean done)
      throws JsonProcessingException {

    // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // See: https://www.baeldung.com/spring-date-parameters

    log.info("dateRequested={}, dateNeeded={}", dateRequested, dateNeeded);

    RecommendationRequest recReq = new RecommendationRequest();
    recReq.setRequesterEmail(requesterEmail);
    recReq.setProfessorEmail(professorEmail);
    recReq.setExplanation(explanation);
    recReq.setDateRequested(dateRequested);
    recReq.setDateNeeded(dateNeeded);
    recReq.setDone(done);

    RecommendationRequest saved = recReqRepository.save(recReq);

    return saved;
  }

  /**
   * Delete a Recommendation Request
   *
   * @param id the id of the Recommendation Request to delete
   * @return a message indicating the Recommendation Request was deleted
   */
  @Operation(summary = "Delete a Recommendation Request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteRecommendationRequest(@Parameter(name = "id") @RequestParam Long id) {
    RecommendationRequest req =
        recReqRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

    recReqRepository.delete(req);
    return genericMessage("Recommendation Request with id %s deleted".formatted(id));
  }

  /**
   * Update a single Recommendation Request
   *
   * @param id id of the Recommendation Request to update
   * @param newReqest the new Request
   * @return the updated date object
   */
  @Operation(summary = "Update a single Recommendation Request")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public RecommendationRequest updateRecommendationRequest(
      @Parameter(name = "id") @RequestParam Long id,
      @RequestBody @Valid RecommendationRequest newReqest) {

    RecommendationRequest recReq =
        recReqRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(RecommendationRequest.class, id));

    recReq.setRequesterEmail(newReqest.getRequesterEmail());
    recReq.setProfessorEmail(newReqest.getProfessorEmail());
    recReq.setExplanation(newReqest.getExplanation());
    recReq.setDateRequested(newReqest.getDateRequested());
    recReq.setDateNeeded(newReqest.getDateNeeded());
    recReq.setDone(newReqest.getDone());

    recReqRepository.save(recReq);

    return recReq;
  }
}

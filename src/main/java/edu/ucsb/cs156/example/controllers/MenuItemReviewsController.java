package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.MenuItemReviews;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.MenuItemReviewsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for MenuItemReviews */
@Tag(name = "MenuItemReviews")
@RequestMapping("/api/menuitemreviews")
@RestController
@Slf4j
public class MenuItemReviewsController extends ApiController {

  @Autowired MenuItemReviewsRepository menuItemReviewsRepository;

  /**
   * List all MenuItemReviews
   *
   * @return an iterable of MenuItemReviews
   */
  @Operation(summary = "List all menu item reviews")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<MenuItemReviews> allMenuItemReviews() {
    Iterable<MenuItemReviews> reviews = menuItemReviewsRepository.findAll();
    return reviews;
  }

  /**
   * Get a single review by id
   *
   * @param id the id of the review
   * @return a MenuItemReviews
   */
  @Operation(summary = "Get a single menu item review")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public MenuItemReviews getById(@Parameter(name = "id") @RequestParam Long id) {
    MenuItemReviews review =
        menuItemReviewsRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(MenuItemReviews.class, id));

    return review;
  }

  /**
   * Create a new review
   *
   * @param reviewerEmail the email of the reviewer
   * @param stars the rating
   * @param dateReviewed the date of review
   * @param comments optional comments
   * @return the saved review
   */
  @Operation(summary = "Create a new menu item review")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public MenuItemReviews postMenuItemReview(
      @Parameter(name = "reviewerEmail") @RequestParam String reviewerEmail,
      @Parameter(name = "stars") @RequestParam int stars,
      @Parameter(
              name = "dateReviewed",
              description =
                  "date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)")
          @RequestParam("dateReviewed")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateReviewed,
      @Parameter(name = "comments") @RequestParam String comments)
      throws JsonProcessingException {

    log.info("dateReviewed={}", dateReviewed);

    MenuItemReviews review = new MenuItemReviews();
    review.setReviewerEmail(reviewerEmail);
    review.setStars(stars);
    review.setDateReviewed(dateReviewed);
    review.setComments(comments);

    MenuItemReviews savedReview = menuItemReviewsRepository.save(review);

    return savedReview;
  }

  //
  // /**
  //  * Delete a MenuItemReviews
  //  *
  //  * @param id the id of the review to delete
  //  * @return a message indicating deletion
  //  */
  // @Operation(summary = "Delete a menu item review")
  // @PreAuthorize("hasRole('ROLE_ADMIN')")
  // @DeleteMapping("")
  // public Object deleteMenuItemReview(@Parameter(name = "id") @RequestParam Long id) {
  //   MenuItemReviews review =
  //       menuItemReviewsRepository
  //           .findById(id)
  //           .orElseThrow(() -> new EntityNotFoundException(MenuItemReviews.class, id));
  //
  //   menuItemReviewsRepository.delete(review);
  //   return genericMessage("MenuItemReviews with id %s deleted".formatted(id));
  // }
  //
  /**
   * Update a single review
   *
   * @param id id of the review to update
   * @param incoming the new review
   * @return the updated review object
   */
  @Operation(summary = "Update a single menu item review")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public MenuItemReviews updateMenuItemReview(
      @Parameter(name = "id") @RequestParam Long id, @RequestBody @Valid MenuItemReviews incoming) {

    MenuItemReviews review =
        menuItemReviewsRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(MenuItemReviews.class, id));

    review.setReviewerEmail(incoming.getReviewerEmail());
    review.setStars(incoming.getStars());
    review.setDateReviewed(incoming.getDateReviewed());
    review.setComments(incoming.getComments());

    menuItemReviewsRepository.save(review);

    return review;
  }
}

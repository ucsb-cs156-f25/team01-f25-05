package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.MenuItemReviews;
import edu.ucsb.cs156.example.repositories.MenuItemReviewsRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = MenuItemReviewsController.class)
@Import(TestConfig.class)
public class MenuItemReviewsControllerTests extends ControllerTestCase {

  @MockBean MenuItemReviewsRepository menuItemReviewsRepository;

  @MockBean UserRepository userRepository;

  // Authorization tests for /api/menuitemreviews/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/menuitemreviews/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/menuitemreviews/all")).andExpect(status().is(200)); // logged
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/menuitemreviews?id=7"))
        .andExpect(status().is(403)); // logged out users can't get by id
  }

  // Authorization tests for /api/menuitemreviews/post
  // (Perhaps should also have these for put and delete)

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/menuitemreviews/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(post("/api/menuitemreviews/post"))
        .andExpect(status().is(403)); // only admins can post
  }

  // Tests with mocks for database actions

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    // arrange
    LocalDateTime dateReviewed = LocalDateTime.parse("2023-01-01T12:00:00");

    MenuItemReviews review =
        MenuItemReviews.builder()
            .id(7L)
            .reviewerEmail("test@example.com")
            .stars(5)
            .dateReviewed(dateReviewed)
            .comments("Excellent")
            .build();

    when(menuItemReviewsRepository.findById(eq(7L))).thenReturn(Optional.of(review));

    // act
    MvcResult response =
        mockMvc.perform(get("/api/menuitemreviews?id=7")).andExpect(status().isOk()).andReturn();

    // assert

    verify(menuItemReviewsRepository, times(1)).findById(eq(7L));
    String expectedJson = mapper.writeValueAsString(review);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange

    when(menuItemReviewsRepository.findById(eq(7L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/menuitemreviews?id=7"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert

    verify(menuItemReviewsRepository, times(1)).findById(eq(7L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("MenuItemReviews with id 7 not found", json.get("message"));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_menuitemreviews() throws Exception {

    // arrange
    LocalDateTime date1 = LocalDateTime.parse("2023-01-01T12:00:00");
    LocalDateTime date2 = LocalDateTime.parse("2023-02-01T12:00:00");

    MenuItemReviews review1 =
        MenuItemReviews.builder()
            .id(1L)
            .reviewerEmail("user1@example.com")
            .stars(4)
            .dateReviewed(date1)
            .comments("Good")
            .build();

    MenuItemReviews review2 =
        MenuItemReviews.builder()
            .id(2L)
            .reviewerEmail("user2@example.com")
            .stars(3)
            .dateReviewed(date2)
            .comments("Average")
            .build();

    ArrayList<MenuItemReviews> expectedReviews = new ArrayList<>();
    expectedReviews.addAll(Arrays.asList(review1, review2));

    when(menuItemReviewsRepository.findAll()).thenReturn(expectedReviews);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/menuitemreviews/all")).andExpect(status().isOk()).andReturn();

    // assert

    verify(menuItemReviewsRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedReviews);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_menuitemreview() throws Exception {

    LocalDateTime dateReviewed = LocalDateTime.parse("2023-01-01T12:00:00");

    MenuItemReviews expectedReview =
        MenuItemReviews.builder()
            .reviewerEmail("test@example.com")
            .stars(5)
            .dateReviewed(dateReviewed)
            .comments("Excellent")
            .build();

    when(menuItemReviewsRepository.save(eq(expectedReview))).thenReturn(expectedReview);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/menuitemreviews/post?reviewerEmail=test@example.com&stars=5&dateReviewed=2023-01-01T12:00:00&comments=Excellent")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(menuItemReviewsRepository, times(1)).save(expectedReview);

    String expectedJson = mapper.writeValueAsString(expectedReview);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  //
  // @WithMockUser(roles = {"ADMIN", "USER"})
  // @Test
  // public void admin_can_delete_a_review() throws Exception {
  //   // arrange
  //
  //   LocalDateTime dateReviewed = LocalDateTime.parse("2023-01-01T12:00:00");
  //
  //   MenuItemReviews review =
  //       MenuItemReviews.builder()
  //           .id(15L)
  //           .reviewerEmail("test@example.com")
  //           .stars(4)
  //           .dateReviewed(dateReviewed)
  //           .comments("Good")
  //           .build();
  //
  //   when(menuItemReviewsRepository.findById(eq(15L))).thenReturn(Optional.of(review));
  //
  //   // act
  //   MvcResult response =
  //       mockMvc
  //           .perform(delete("/api/menuitemreviews?id=15").with(csrf()))
  //           .andExpect(status().isOk())
  //           .andReturn();
  //
  //   // assert
  //   verify(menuItemReviewsRepository, times(1)).findById(15L);
  //   verify(menuItemReviewsRepository, times(1)).delete(any());
  //
  //   Map<String, Object> json = responseToJson(response);
  //   assertEquals("MenuItemReviews with id 15 deleted", json.get("message"));
  // }
  //
  // @WithMockUser(roles = {"ADMIN", "USER"})
  // @Test
  // public void admin_tries_to_delete_non_existant_menuitemreview_and_gets_right_error_message()
  //     throws Exception {
  //   // arrange
  //
  //   when(menuItemReviewsRepository.findById(eq(15L))).thenReturn(Optional.empty());
  //
  //   // act
  //   MvcResult response =
  //       mockMvc
  //           .perform(delete("/api/menuitemreviews?id=15").with(csrf()))
  //           .andExpect(status().isNotFound())
  //           .andReturn();
  //
  //   // assert
  //   verify(menuItemReviewsRepository, times(1)).findById(15L);
  //   Map<String, Object> json = responseToJson(response);
  //   assertEquals("MenuItemReviews with id 15 not found", json.get("message"));
  // }
  //
  // @WithMockUser(roles = {"ADMIN", "USER"})
  // @Test
  // public void admin_can_edit_an_existing_menuitemreview() throws Exception {
  //   // arrange
  //
  //   LocalDateTime dateOrig = LocalDateTime.parse("2023-01-01T12:00:00");
  //   LocalDateTime dateEdited = LocalDateTime.parse("2023-02-01T12:00:00");
  //
  //   MenuItemReviews origReview =
  //       MenuItemReviews.builder()
  //           .id(67L)
  //           .reviewerEmail("test@example.com")
  //           .stars(4)
  //           .dateReviewed(dateOrig)
  //           .comments("Good")
  //           .build();
  //
  //   MenuItemReviews editedReview =
  //       MenuItemReviews.builder()
  //           .id(67L)
  //           .reviewerEmail("admin@example.com")
  //           .stars(5)
  //           .dateReviewed(dateEdited)
  //           .comments("Excellent")
  //           .build();
  //
  //   String requestBody = mapper.writeValueAsString(editedReview);
  //
  //   when(menuItemReviewsRepository.findById(eq(67L))).thenReturn(Optional.of(origReview));
  //
  //   // act
  //   MvcResult response =
  //       mockMvc
  //           .perform(
  //               put("/api/menuitemreviews?id=67")
  //                   .contentType(MediaType.APPLICATION_JSON)
  //                   .characterEncoding("utf-8")
  //                   .content(requestBody)
  //                   .with(csrf()))
  //           .andExpect(status().isOk())
  //           .andReturn();
  //
  //   // assert
  //   verify(menuItemReviewsRepository, times(1)).findById(67L);
  //   verify(menuItemReviewsRepository, times(1)).save(editedReview);
  //   String responseString = response.getResponse().getContentAsString();
  //   assertEquals(requestBody, responseString);
  // }
  //
  // @WithMockUser(roles = {"ADMIN", "USER"})
  // @Test
  // public void admin_cannot_edit_menuitemreview_that_does_not_exist() throws Exception {
  //   // arrange
  //
  //   LocalDateTime dateReviewed = LocalDateTime.parse("2023-01-01T12:00:00");
  //
  //   MenuItemReviews editedReview =
  //       MenuItemReviews.builder()
  //           .id(67L)
  //           .reviewerEmail("admin@example.com")
  //           .stars(5)
  //           .dateReviewed(dateReviewed)
  //           .comments("Excellent")
  //           .build();
  //
  //   String requestBody = mapper.writeValueAsString(editedReview);
  //
  //   when(menuItemReviewsRepository.findById(eq(67L))).thenReturn(Optional.empty());
  //
  //   // act
  //   MvcResult response =
  //       mockMvc
  //           .perform(
  //               put("/api/menuitemreviews?id=67")
  //                   .contentType(MediaType.APPLICATION_JSON)
  //                   .characterEncoding("utf-8")
  //                   .content(requestBody)
  //                   .with(csrf()))
  //           .andExpect(status().isNotFound())
  //           .andReturn();
  //
  //   // assert
  //   verify(menuItemReviewsRepository, times(1)).findById(67L);
  //   Map<String, Object> json = responseToJson(response);
  //   assertEquals("MenuItemReviews with id 67 not found", json.get("message"));
  // }
}

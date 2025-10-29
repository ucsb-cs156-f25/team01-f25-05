package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq; // <-- needed for eq(...)
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map; // <-- only needed if you use Map in assertions elsewhere
import java.util.Optional; // <-- needed for Optional
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = HelpRequestController.class)
@Import(TestConfig.class)
public class HelpRequestControllerTests extends ControllerTestCase {

  @MockBean HelpRequestRepository helpRequestRepository;

  @MockBean UserRepository userRepository;

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/helprequests/all")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/helprequests/all")).andExpect(status().is(200));
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc.perform(post("/api/helprequests/post")).andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_helprequests() throws Exception {
    // arrange
    LocalDateTime t1 = LocalDateTime.parse("2024-05-01T09:15:00");
    HelpRequest hr1 =
        HelpRequest.builder()
            .requesterEmail("alice@ucsb.edu")
            .teamId("s23-5pm-1")
            .tableOrBreakoutRoom("Table 3")
            .requestTime(t1)
            .explanation("Stuck on Spring Security config")
            .solved(false)
            .build();

    LocalDateTime t2 = LocalDateTime.parse("2024-05-01T09:30:00");
    HelpRequest hr2 =
        HelpRequest.builder()
            .requesterEmail("bob@ucsb.edu")
            .teamId("s23-5pm-2")
            .tableOrBreakoutRoom("Breakout A")
            .requestTime(t2)
            .explanation("JUnit/Mockito verification help")
            .solved(true)
            .build();

    List<HelpRequest> expected = Arrays.asList(hr1, hr2);
    when(helpRequestRepository.findAll()).thenReturn(expected);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/helprequests/all")).andExpect(status().isOk()).andReturn();

    // assert
    verify(helpRequestRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expected);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void user_can_post_new_helprequest_and_solved_true_is_persisted() throws Exception {
    // arrange
    LocalDateTime t = LocalDateTime.parse("2024-05-01T10:00:00");
    HelpRequest savedFromRepo =
        HelpRequest.builder()
            .requesterEmail("charlie@ucsb.edu")
            .teamId("s23-5pm-3")
            .tableOrBreakoutRoom("Table 5")
            .requestTime(t)
            .explanation("Need help deploying to Heroku")
            .solved(true) // repo returns true to mirror the request
            .build();

    when(helpRequestRepository.save(any(HelpRequest.class))).thenReturn(savedFromRepo);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/helprequests/post")
                    .param("requesterEmail", "charlie@ucsb.edu")
                    .param("teamId", "s23-5pm-3")
                    .param("tableOrBreakoutRoom", "Table 5")
                    .param("requestTime", "2024-05-01T10:00:00")
                    .param("explanation", "Need help deploying to Heroku")
                    .param("solved", "true") // <-- make it true to kill the mutation
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // capture and verify the entity passed to save()
    ArgumentCaptor<HelpRequest> captor = ArgumentCaptor.forClass(HelpRequest.class);
    verify(helpRequestRepository, times(1)).save(captor.capture());
    HelpRequest passedToSave = captor.getValue();

    assertEquals("charlie@ucsb.edu", passedToSave.getRequesterEmail());
    assertEquals("s23-5pm-3", passedToSave.getTeamId());
    assertEquals("Table 5", passedToSave.getTableOrBreakoutRoom());
    assertEquals(t, passedToSave.getRequestTime());
    assertEquals("Need help deploying to Heroku", passedToSave.getExplanation());
    assertEquals(true, passedToSave.getSolved()); // <-- this kills the setSolved mutation

    // verify JSON response mirrors what repo returned
    String expectedJson = mapper.writeValueAsString(savedFromRepo);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/helprequests?id=7"))
        .andExpect(status().is(403)); // logged out users can't get by id
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange

    when(helpRequestRepository.findById(eq(7L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc.perform(get("/api/helprequests?id=7")).andExpect(status().isNotFound()).andReturn();

    // assert

    verify(helpRequestRepository, times(1)).findById(eq(7L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("HelpRequest with id 7 not found", json.get("message"));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    // arrange
    LocalDateTime t = LocalDateTime.parse("2024-05-01T09:45:00");

    HelpRequest helpRequest =
        HelpRequest.builder()
            .requesterEmail("carol@ucsb.edu")
            .teamId("s23-5pm-4")
            .tableOrBreakoutRoom("Breakout B")
            .requestTime(t)
            .explanation("Need help with PIT tests")
            .solved(false)
            .build();

    when(helpRequestRepository.findById(eq(7L))).thenReturn(Optional.of(helpRequest));

    // act
    MvcResult response =
        mockMvc.perform(get("/api/helprequests?id=7")).andExpect(status().isOk()).andReturn();

    // assert
    verify(helpRequestRepository, times(1)).findById(eq(7L));
    String expectedJson = mapper.writeValueAsString(helpRequest);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }
}

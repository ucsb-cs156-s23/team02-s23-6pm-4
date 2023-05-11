package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Movie;
import edu.ucsb.cs156.example.repositories.MovieRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = MoviesController.class)
@Import(TestConfig.class)
public class MovieControllerTests extends ControllerTestCase {

        @MockBean
        MovieRepository movierepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/movies/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/movies/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/movies/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/movies?id=1375666"))//NOTE!!! imdb 1375666
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/movies/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/movies/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/movies/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                Movie moviE = Movie.builder()
                                .title("Inception")
                                .id("1375666")
                                .director("Christopher Nolan")
                                .release_year(2010)
                                .build();

                when(movierepository.findById(eq("1375666"))).thenReturn(Optional.of(moviE));

                // act
                MvcResult response = mockMvc.perform(get("/api/movies?id=1375666"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(movierepository, times(1)).findById(eq("1375666"));
                String expectedJson = mapper.writeValueAsString(moviE);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }
//CONTINUE FROM HEREEEEEEE!!!!!!
        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(movierepository.findById(eq("0000000"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/movies?id=0000000"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(movierepository, times(1)).findById(eq("0000000"));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Movie with id 0000000 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_movies() throws Exception {

                // arrange

                Movie inception = Movie.builder()
                                .title("Inception")
                                .id("1375666")
                                .director("Christopher Nolan")
                                .release_year(2010)
                                .build();

                Movie parasite = Movie.builder()
                                .title("Parasite")
                                .id("6751668")
                                .director("Bong Joon Ho")
                                .release_year(2019)
                                .build();

                ArrayList<Movie> expectedMovies = new ArrayList<>();
                expectedMovies.addAll(Arrays.asList(inception, parasite));

                when(movierepository.findAll()).thenReturn(expectedMovies);

                // act
                MvcResult response = mockMvc.perform(get("/api/movies/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(movierepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedMovies);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_movie() throws Exception {
                // arrange

                Movie arrival = Movie.builder()
                                .title("Arrival")
                                .id("0137523")
                                .director("DenisVilleneuve")
                                .release_year(2016)
                                .build();

                when(movierepository.save(eq(arrival))).thenReturn(arrival);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/movies/post?title=Arrival&id=0137523&director=DenisVilleneuve&release_year=2016")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(movierepository, times(1)).save(arrival);
                String expectedJson = mapper.writeValueAsString(arrival);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_movie() throws Exception {
                // arrange

                Movie goodnurse = Movie.builder()
                                .title("The Good Nurse")
                                .id("4273800")
                                .director("Tobias Lindholm")
                                .release_year(2022)
                                .build();

                when(movierepository.findById(eq("4273800"))).thenReturn(Optional.of(goodnurse));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/movies?id=4273800")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(movierepository, times(1)).findById("4273800");
                verify(movierepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Movie with id 4273800 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_movie_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(movierepository.findById(eq("0000000"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/movies?id=0000000")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(movierepository, times(1)).findById("0000000");
                Map<String, Object> json = responseToJson(response);
                assertEquals("Movie with id 0000000 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_movie() throws Exception {
                // arrange

                Movie inceptionOrig = Movie.builder()
                                .title("Inception")
                                .id("1375666")
                                .director("Christopher Nolan")
                                .release_year(2010)
                                .build();


                Movie inceptionEdited = Movie.builder()
                                .title("Inception Movie")
                                .id("1375666")
                                .director("Christopher Nolan")
                                .release_year(2010)
                                .build();

                String requestBody = mapper.writeValueAsString(inceptionEdited);

                when(movierepository.findById(eq("1375666"))).thenReturn(Optional.of(inceptionOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/movies?id=1375666")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(movierepository, times(1)).findById("1375666");
                verify(movierepository, times(1)).save(inceptionEdited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_movie_that_does_not_exist() throws Exception {
                // arrange

                Movie editedMovie = Movie.builder()
                                .title("Toy Story 7")
                                .id("0000000")
                                .director("Jessica Ho")
                                .release_year(2023)
                                .build();

                String requestBody = mapper.writeValueAsString(editedMovie);

                when(movierepository.findById(eq("0000000"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/movies?id=0000000")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(movierepository, times(1)).findById("0000000");
                Map<String, Object> json = responseToJson(response);
                assertEquals("Movie with id 0000000 not found", json.get("message"));

        }
}

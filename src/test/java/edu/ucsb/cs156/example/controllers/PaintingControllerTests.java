package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Painting;
import edu.ucsb.cs156.example.repositories.PaintingRepository;

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

@WebMvcTest(controllers = PaintingController.class)
@Import(TestConfig.class)
public class PaintingControllerTests extends ControllerTestCase {

        @MockBean
        PaintingRepository paintingRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/painting/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/painting/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/painting/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/painting?code=mona-lisa"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/painting/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/painting/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/painting/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                Painting paintings = Painting.builder()
                                .name("Mona Lisa")
                                .code("mona-lisa")
                                .artist("Leonardo da Vinci")
                                .year(1517)
                                .medium("Oil")
                                .period("Renaissance")
                                .build();

                when(paintingRepository.findById(eq("mona-lisa"))).thenReturn(Optional.of(paintings));

                // act
                MvcResult response = mockMvc.perform(get("/api/painting?code=mona-lisa"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(paintingRepository, times(1)).findById(eq("mona-lisa"));
                String expectedJson = mapper.writeValueAsString(paintings);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(paintingRepository.findById(eq("venus"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/painting?code=venus"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(paintingRepository, times(1)).findById(eq("venus"));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Painting with id venus not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_painting() throws Exception {

                // arrange

                Painting monalisa = Painting.builder()
                                .name("Mona Lisa")
                                .code("mona-lisa")
                                .artist("Leonardo da Vinci")
                                .year(1517)
                                .medium("Oil")
                                .period("Renaissance")
                                .build();

                Painting starrynight = Painting.builder()
                                .name("Starry Night")
                                .code("starry-night")
                                .artist("Vincent van Gogh")
                                .year(1889)
                                .medium("Oil")
                                .period("Post-Impressionism")
                                .build();

                ArrayList<Painting> expectedPaintings = new ArrayList<>();
                expectedPaintings.addAll(Arrays.asList(monalisa, starrynight));

                when(paintingRepository.findAll()).thenReturn(expectedPaintings);

                // act
                MvcResult response = mockMvc.perform(get("/api/painting/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(paintingRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedPaintings);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_paintings() throws Exception {
                // arrange

                Painting persistence = Painting.builder()
                                .name("Persistence")
                                .code("persistence")
                                .artist("Salvador")
                                .year(1931)
                                .medium("Oil")
                                .period("Surrealism")
                                .build();

                when(paintingRepository.save(eq(persistence))).thenReturn(persistence);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/painting/post?name=Persistence&code=persistence&artist=Salvador&year=1931&medium=Oil&period=Surrealism")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(paintingRepository, times(1)).save(persistence);
                String expectedJson = mapper.writeValueAsString(persistence);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange

                Painting pearl = Painting.builder()
                                .name("Girl with a Pearl Earring")
                                .code("pearl")
                                .artist("Johannes Vermeer")
                                .year(1665)
                                .medium("Oil")
                                .period("Dutch Golden Age")
                                .build();

                when(paintingRepository.findById(eq("pearl"))).thenReturn(Optional.of(pearl));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/painting?code=pearl")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(paintingRepository, times(1)).findById("pearl");
                verify(paintingRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Painting with id pearl deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_paintings_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(paintingRepository.findById(eq("venus"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/painting?code=venus")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(paintingRepository, times(1)).findById("venus");
                Map<String, Object> json = responseToJson(response);
                assertEquals("Painting with id venus not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_paintings() throws Exception {
                // arrange

                Painting monalisaOrig = Painting.builder()
                                .name("Mona Lisa")
                                .code("mona-lisa")
                                .artist("Leonardo da Vinci")
                                .year(1517)
                                .medium("Oil")
                                .period("Renaissance")
                                .build();

                Painting monalisaEdited = Painting.builder()
                                .name("Mona Lisa Painting")
                                .code("mona-lisa")
                                .artist("Leonardo da Vinci")
                                .year(1517)
                                .medium("Oil")
                                .period("Renaissance")
                                .build();

                String requestBody = mapper.writeValueAsString(monalisaEdited);

                when(paintingRepository.findById(eq("mona-lisa"))).thenReturn(Optional.of(monalisaOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/painting?code=mona-lisa")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(paintingRepository, times(1)).findById("mona-lisa");
                verify(paintingRepository, times(1)).save(monalisaEdited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_paintings_that_does_not_exist() throws Exception {
                // arrange

                Painting editedPaintings = Painting.builder()
                                .name("The Birth of Venus")
                                .code("venus")
                                .artist("Sandro Botticelli")
                                .year(1486)
                                .medium("Tempera")
                                .period("Italian Renaissance")
                                .build();

                String requestBody = mapper.writeValueAsString(editedPaintings);

                when(paintingRepository.findById(eq("venus"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/painting?code=venus")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(paintingRepository, times(1)).findById("venus");
                Map<String, Object> json = responseToJson(response);
                assertEquals("Painting with id venus not found", json.get("message"));

        }
}

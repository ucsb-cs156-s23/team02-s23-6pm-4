package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Music;
import edu.ucsb.cs156.example.repositories.MusicRepository;

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

@WebMvcTest(controllers = MusicController.class)
@Import(TestConfig.class)
public class MusicControllerTests extends ControllerTestCase {

        @MockBean
        MusicRepository musicRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/music/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/music/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/music/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/music?id=0"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/music/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/music/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/music/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                Music musics = Music.builder()
                                .title("Bibo No Aozora")
                                .album("1996")
                                .artist("Ryuichi Sakamoto")
                                .genre("Classical")
                                .build();

                when(musicRepository.findById(eq(1L))).thenReturn(Optional.of(musics));

                // act
                MvcResult response = mockMvc.perform(get("/api/music?id=1"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(musicRepository, times(1)).findById(eq(1L));
                String expectedJson = mapper.writeValueAsString(musics);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(musicRepository.findById(eq(2L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/music?id=2"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(musicRepository, times(1)).findById(eq(2L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Music with id 2 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_music() throws Exception {

                // arrange

                Music nineteen = Music.builder()
                                .title("Bibo No Aozora")
                                .album("1996")
                                .artist("Ryuichi Sakamoto")
                                .genre("Classical")
                                .build();

                Music unforgiven = Music.builder()
                                .title("No Return")
                                .album("Unforgiven")
                                .artist("Lesserafim")
                                .genre("K-Pop")
                                .build();

                ArrayList<Music> expectedMusic = new ArrayList<>();
                expectedMusic.addAll(Arrays.asList(nineteen, unforgiven));

                when(musicRepository.findAll()).thenReturn(expectedMusic);

                // act
                MvcResult response = mockMvc.perform(get("/api/music/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(musicRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedMusic);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_musics() throws Exception {
                // arrange

                Music gabriel = Music.builder()
                                .title("Angostura")
                                .album("Gabriel")
                                .artist("Keshi")
                                .genre("Sad")
                                .build();

                when(musicRepository.save(eq(gabriel))).thenReturn(gabriel);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/music/post?title=Angostura&album=Gabriel&artist=Keshi&genre=Sad")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(musicRepository, times(1)).save(gabriel);
                String expectedJson = mapper.writeValueAsString(gabriel);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange

                Music ILLENIUM = Music.builder()
                                .title("Lifeline")
                                .album("ILLENIUM")
                                .artist("Illenium")
                                .genre("EDM")
                                .build();

                when(musicRepository.findById(eq(3L))).thenReturn(Optional.of(ILLENIUM));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/music?id=3")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(musicRepository, times(1)).findById(3L);
                verify(musicRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Music with id 3 deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_musics_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(musicRepository.findById(eq(4L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/music?id=4")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(musicRepository, times(1)).findById(4L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Music with id 4 not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_musics() throws Exception {
                // arrange

                Music nineteenOrig = Music.builder()
                                .title("Bibo No Aozora")
                                .album("1996")
                                .artist("Ryuichi Sakamoto")
                                .genre("Classical")
                                .build();

                Music nineteenEdited = Music.builder()
                                .title("Bibo No Aozora edited")
                                .album("1996")
                                .artist("Ryuichi Sakamoto")
                                .genre("Classical")
                                .build();

                String requestBody = mapper.writeValueAsString(nineteenEdited);

                when(musicRepository.findById(eq(5L))).thenReturn(Optional.of(nineteenOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/music?id=5")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(musicRepository, times(1)).findById(5L);
                verify(musicRepository, times(1)).save(nineteenEdited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_musics_that_does_not_exist() throws Exception {
                // arrange

                Music editedMusic = Music.builder()
                                .title("I Am")
                                .album("I've-IVE")
                                .artist("IVE")
                                .genre("K-POP")
                                .build();

                String requestBody = mapper.writeValueAsString(editedMusic);

                when(musicRepository.findById(eq(6L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/music?id=6")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(musicRepository, times(1)).findById(6L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Music with id 6 not found", json.get("message"));

        }
}

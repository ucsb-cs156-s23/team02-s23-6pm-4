package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Apartment;
import edu.ucsb.cs156.example.repositories.ApartmentRepository;

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

@WebMvcTest(controllers = ApartmentController.class)
@Import(TestConfig.class)
public class ApartmentControllerTests extends ControllerTestCase {

        @MockBean
        ApartmentRepository apartmentRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/apartment/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/apartment/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/apartment/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/apartment?code=carrillo"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/apartment/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/apartment/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/apartment/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                Apartment apartment = Apartment.builder()
                                .name("Sierra Madre Villages")
                                .code("sierra-madre-villages")
                                .address("555 Storke Road")
                                .city("Goleta")
                                .state("CA")
                                .rooms(109)
                                .description("Nice and New")
                                .build();

                when(apartmentRepository.findById(eq("sierra-madre-villages"))).thenReturn(Optional.of(apartment));

                // act
                MvcResult response = mockMvc.perform(get("/api/apartment?code=sierra-madre-villages"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(apartmentRepository, times(1)).findById(eq("sierra-madre-villages"));
                String expectedJson = mapper.writeValueAsString(apartment);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(apartmentRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/apartment?code=munger-hall"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(apartmentRepository, times(1)).findById(eq("munger-hall"));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Apartment with id munger-hall not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_apartment() throws Exception {

                // arrange

                Apartment sierraMadreVillages = Apartment.builder()
                                .name("Sierra Madre Villages")
                                .code("sierra-madre-villages")
                                .address("555 Storke Road")
                                .city("Goleta")
                                .state("CA")
                                .rooms(109)
                                .description("Nice and New")
                                .build();

                Apartment elDorado = Apartment.builder()
                                .name("El Dorado")
                                .code("el-dorado")
                                .address("6667 El Colegio Road")
                                .city("Goleta")
                                .state("CA")
                                .rooms(50)
                                .description("Tropicana but Nicer")
                                .build();

                ArrayList<Apartment> expectedApartment = new ArrayList<>();
                expectedApartment.addAll(Arrays.asList(sierraMadreVillages, elDorado));

                when(apartmentRepository.findAll()).thenReturn(expectedApartment);

                // act
                MvcResult response = mockMvc.perform(get("/api/apartment/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(apartmentRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedApartment);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_apartment() throws Exception {
                // arrange

                Apartment sanJoaquinNorthVillages = Apartment.builder()
                                .name("San Joaquin North Villages")
                                .code("san-joaquin-north-villages")
                                .address("650 Storke Road")
                                .city("Goleta")
                                .state("CA")
                                .rooms(166)
                                .description("Nice but Far")
                                .build();

                when(apartmentRepository.save(eq(sanJoaquinNorthVillages))).thenReturn(sanJoaquinNorthVillages);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/apartment/post?name=San Joaquin North Villages&code=san-joaquin-north-villages&address=650 Storke Road&city=Goleta&state=CA&rooms=166&description=Nice but Far")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(apartmentRepository, times(1)).save(sanJoaquinNorthVillages);
                String expectedJson = mapper.writeValueAsString(sanJoaquinNorthVillages);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange

                Apartment sierraMadreVillages = Apartment.builder()
                                .name("Sierra Madre Villages")
                                .code("sierra-madre-villages")
                                .address("555 Storke Road")
                                .city("Goleta")
                                .state("CA")
                                .rooms(109)
                                .description("Nice and New")
                                .build();

                when(apartmentRepository.findById(eq("sierra-madre-villages"))).thenReturn(Optional.of(sierraMadreVillages));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/apartment?code=sierra-madre-villages")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(apartmentRepository, times(1)).findById("sierra-madre-villages");
                verify(apartmentRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Apartment with id sierra-madre-villages deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_apartment_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(apartmentRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/apartment?code=munger-hall")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(apartmentRepository, times(1)).findById("munger-hall");
                Map<String, Object> json = responseToJson(response);
                assertEquals("Apartment with id munger-hall not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_apartment() throws Exception {
                // arrange

                Apartment sierraMadreVillagesOrig = Apartment.builder()
                                .name("Sierra Madre Villages")
                                .code("sierra-madre-villages")
                                .address("555 Storke Road")
                                .city("Goleta")
                                .state("CA")
                                .rooms(109)
                                .description("Nice and New")
                                .build();

                Apartment sierraMadreVillagesEdited = Apartment.builder()
                                .name("Sierra Madre")
                                .code("sierra-madre-villages")
                                .address("556 Storke Road")
                                .city("Isla Vista")
                                .state("Cali.")
                                .rooms(150)
                                .description("Nice and New with lots of space")
                                .build();

                String requestBody = mapper.writeValueAsString(sierraMadreVillagesEdited);

                when(apartmentRepository.findById(eq("sierra-madre-villages"))).thenReturn(Optional.of(sierraMadreVillagesOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/apartment?code=sierra-madre-villages")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(apartmentRepository, times(1)).findById("sierra-madre-villages");
                verify(apartmentRepository, times(1)).save(sierraMadreVillagesEdited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_apartment_that_does_not_exist() throws Exception {
                // arrange

                Apartment editedApartment = Apartment.builder()
                                .name("Munger Hall")
                                .code("munger-hall")
                                .address("555 Storke Road")
                                .city("Goleta")
                                .state("CA")
                                .rooms(1)
                                .description("No place to sleep")
                                .build();

                String requestBody = mapper.writeValueAsString(editedApartment);

                when(apartmentRepository.findById(eq("munger-hall"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/apartment?code=munger-hall")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(apartmentRepository, times(1)).findById("munger-hall");
                Map<String, Object> json = responseToJson(response);
                assertEquals("Apartment with id munger-hall not found", json.get("message"));

        }
}

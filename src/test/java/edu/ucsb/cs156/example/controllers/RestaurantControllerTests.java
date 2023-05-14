package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Restaurant;
import edu.ucsb.cs156.example.repositories.RestaurantRepository;

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

@WebMvcTest(controllers = RestaurantController.class)
@Import(TestConfig.class)
public class RestaurantControllerTests extends ControllerTestCase {

        @MockBean
        RestaurantRepository RestaurantRepository;

        @MockBean
        UserRepository userRepository;

        // Authorization tests for /api/restaurant/admin/all

        @Test
        public void logged_out_users_cannot_get_all() throws Exception {
                mockMvc.perform(get("/api/restaurant/all"))
                                .andExpect(status().is(403)); // logged out users can't get all
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_users_can_get_all() throws Exception {
                mockMvc.perform(get("/api/restaurant/all"))
                                .andExpect(status().is(200)); // logged
        }

        @Test
        public void logged_out_users_cannot_get_by_id() throws Exception {
                mockMvc.perform(get("/api/restaurant?code=mokkoji"))
                                .andExpect(status().is(403)); // logged out users can't get by id
        }

        // Authorization tests for /api/restaurant/post
        // (Perhaps should also have these for put and delete)

        @Test
        public void logged_out_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/restaurant/post"))
                                .andExpect(status().is(403));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_regular_users_cannot_post() throws Exception {
                mockMvc.perform(post("/api/restaurant/post"))
                                .andExpect(status().is(403)); // only admins can post
        }

        // Tests with mocks for database actions

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                Restaurant restaurants = Restaurant.builder()
                                .name("Mokkoji")
                                .code("mokkoji")
                                .descript("Trendy, contemporary eatery focusing on shabu-shabu hot pot and other Japanese specialties")
                                .yelp_rating("4 stars")
                                .build();

                when(RestaurantRepository.findById(eq("mokkoji"))).thenReturn(Optional.of(restaurants));

                // act
                MvcResult response = mockMvc.perform(get("/api/restaurant?code=mokkoji"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(RestaurantRepository, times(1)).findById(eq("mokkoji"));
                String expectedJson = mapper.writeValueAsString(restaurants);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(RestaurantRepository.findById(eq("orangetheory"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/restaurant?code=orangetheory"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(RestaurantRepository, times(1)).findById(eq("orangetheory"));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Restaurant with id orangetheory not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_restaurant() throws Exception {

                // arrange

                Restaurant mokkoji = Restaurant.builder()
                                .name("Mokkoji")
                                .code("mokkoji")
                                .descript("Trendy, contemporary eatery focusing on shabu-shabu hot pot and other Japanese specialties")
                                .yelp_rating("4 stars")
                                .build();

                Restaurant ohshima = Restaurant.builder()
                                .name("Ohshima")
                                .code("ohshima")
                                .descript("Simple nook known for its fresh seafood and omakase, plus other creative Japanese fare, beer and wine")
                                .yelp_rating("4.5 stars")
                                .build();

                ArrayList<Restaurant> expectedRestaurants = new ArrayList<>();
                expectedRestaurants.addAll(Arrays.asList(mokkoji, ohshima));

                when(RestaurantRepository.findAll()).thenReturn(expectedRestaurants);

                // act
                MvcResult response = mockMvc.perform(get("/api/restaurant/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(RestaurantRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedRestaurants);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_restaurant() throws Exception {
                // arrange

                Restaurant tanakaya = Restaurant.builder()
                                .name("Tanakaya")
                                .code("tanakaya")
                                .descript("Japanese soba and udon noodles are the draw at this popular, compact eatery with a casual atmosphere")
                                .yelp_rating("4 stars")
                                .build();

                when(RestaurantRepository.save(eq(tanakaya))).thenReturn(tanakaya);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/restaurant/post?name=Tanakaya&code=tanakaya&descript=Japanese soba and udon noodles are the draw at this popular, compact eatery with a casual atmosphere&yelp_rating=4 stars")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(RestaurantRepository, times(1)).save(tanakaya);
                String expectedJson = mapper.writeValueAsString(tanakaya);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_delete_a_date() throws Exception {
                // arrange

                Restaurant hironori = Restaurant.builder()
                                .name("Hironori")
                                .code("hironori")
                                .descript("Fresh cut noodles, hand made broth and sauce with lots of passion and technique from the 2 best Japanese ramen chefs")
                                .yelp_rating("4.5 stars")
                                .build();

                when(RestaurantRepository.findById(eq("hironori"))).thenReturn(Optional.of(hironori));

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/restaurant?code=hironori")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(RestaurantRepository, times(1)).findById("hironori");
                verify(RestaurantRepository, times(1)).delete(any());

                Map<String, Object> json = responseToJson(response);
                assertEquals("Restaurant with id hironori deleted", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_tries_to_delete_non_existant_restaurants_and_gets_right_error_message()
                        throws Exception {
                // arrange

                when(RestaurantRepository.findById(eq("orangetheory"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                delete("/api/restaurant?code=orangetheory")
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(RestaurantRepository, times(1)).findById("orangetheory");
                Map<String, Object> json = responseToJson(response);
                assertEquals("Restaurant with id orangetheory not found", json.get("message"));
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_restaurants() throws Exception {
                // arrange

                Restaurant mokkojiOrig = Restaurant.builder()
                                .name("Mokkoji")
                                .code("mokkoji")
                                .descript("Trendy, contemporary eatery focusing on shabu-shabu hot pot and other Japanese specialties")
                                .yelp_rating("4 stars")
                                .build();

                Restaurant mokkojiEdited = Restaurant.builder()
                                .name("Mokkoji Shabu Shabu Bar")
                                .code("mokkoji")
                                .descript("Trendy, contemporary eatery focusing on shabu-shabu hot pot and other Japanese specialties")
                                .yelp_rating("4.5 stars")
                                .build();

                String requestBody = mapper.writeValueAsString(mokkojiEdited);

                when(RestaurantRepository.findById(eq("mokkoji"))).thenReturn(Optional.of(mokkojiOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/restaurant?code=mokkoji")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(RestaurantRepository, times(1)).findById("mokkoji");
                verify(RestaurantRepository, times(1)).save(mokkojiEdited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_restaurant_that_does_not_exist() throws Exception {
                // arrange

                Restaurant editedRestaurants = Restaurant.builder()
                                .name("OrangeTheory")
                                .code("orangetheory")
                                .descript("An American boutique fitness studio franchise")
                                .yelp_rating("4.5 stars")
                                .build();

                String requestBody = mapper.writeValueAsString(editedRestaurants);

                when(RestaurantRepository.findById(eq("orangetheory"))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/restaurant?code=orangetheory")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(RestaurantRepository, times(1)).findById("orangetheory");
                Map<String, Object> json = responseToJson(response);
                assertEquals("Restaurant with id orangetheory not found", json.get("message"));

        }
}

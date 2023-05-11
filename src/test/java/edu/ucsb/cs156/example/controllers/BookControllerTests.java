package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Book;
import edu.ucsb.cs156.example.repositories.BookRepository;

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

@WebMvcTest(controllers = BookController.class)
@Import(TestConfig.class)
public class BookControllerTests extends ControllerTestCase {

    @MockBean
    BookRepository bookRepository;

    @MockBean
    UserRepository userRepository;

    // Authorization tests for /api/books/admin/all

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
            mockMvc.perform(get("/api/books/all"))
                            .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
            mockMvc.perform(get("/api/books/all"))
                            .andExpect(status().is(200)); // logged
    }

    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception {
            mockMvc.perform(get("/api/books?id=3"))
                            .andExpect(status().is(403)); // logged out users can't get by id
    }

    // Authorization tests for /api/books/post
    // (Perhaps should also have these for put and delete)

    @Test
    public void logged_out_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/books/post"))
                            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/books/post"))
                            .andExpect(status().is(403)); // only admins can post
    }


    // // Tests with mocks for database actions

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

                // arrange

                Book book1 = Book.builder()
                                .title("Hello")
                                .author("me")
                                .description("nothing")
                                .genre("Action")
                                .build();

                when(bookRepository.findById(eq(7L))).thenReturn(Optional.of(book1));

                // act
                MvcResult response = mockMvc.perform(get("/api/books?id=7"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(bookRepository, times(1)).findById(eq(7L));
                String expectedJson = mapper.writeValueAsString(book1);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

                // arrange

                when(bookRepository.findById(eq(7L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(get("/api/books?id=7"))
                                .andExpect(status().isNotFound()).andReturn();

                // assert

                verify(bookRepository, times(1)).findById(eq(7L));
                Map<String, Object> json = responseToJson(response);
                assertEquals("EntityNotFoundException", json.get("type"));
                assertEquals("Book with id 7 not found", json.get("message"));
        }

        @WithMockUser(roles = { "USER" })
        @Test
        public void logged_in_user_can_get_all_books() throws Exception {

                // arrange

                Book animalFarm = Book.builder()
                            .title("Animal Farm")
                            .author("George Orwell")
                            .description("A story about a group of farm animals who rebel against their human farmer")
                            .genre("Fable")
                            .build();

                Book fahrenheit451 = Book.builder()
                            .title("Fahrenheit 451")
                            .author("Ray Bradbury")
                            .description("A story set in a dystopian society that burns books")
                            .genre("Dystopian fiction")
                            .build();

                ArrayList<Book> expectedBooks = new ArrayList<>();
                expectedBooks.addAll(Arrays.asList(animalFarm, fahrenheit451));

                when(bookRepository.findAll()).thenReturn(expectedBooks);

                // act
                MvcResult response = mockMvc.perform(get("/api/books/all"))
                                .andExpect(status().isOk()).andReturn();

                // assert

                verify(bookRepository, times(1)).findAll();
                String expectedJson = mapper.writeValueAsString(expectedBooks);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void an_admin_user_can_post_a_new_book() throws Exception {
                // arrange

                Book greatGatsby = Book.builder()
                            .title("The Great Gatsby")
                            .author("F. Scott Fitzgerald")
                            .description("A tragic story of Jay Gatsby, a self-made millionaire")
                            .genre("Tragedy")
                            .build();

                when(bookRepository.save(eq(greatGatsby))).thenReturn(greatGatsby);

                // act
                MvcResult response = mockMvc.perform(
                                post("/api/books/post?title=The Great Gatsby&author=F. Scott Fitzgerald&description=A tragic story of Jay Gatsby, a self-made millionaire&genre=Tragedy")
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(bookRepository, times(1)).save(greatGatsby);
                String expectedJson = mapper.writeValueAsString(greatGatsby);
                String responseString = response.getResponse().getContentAsString();
                assertEquals(expectedJson, responseString);
        }
    
        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_can_edit_an_existing_book() throws Exception {
                // arrange

                Book helloOrig = Book.builder()
                            .title("Hello")
                            .author("testing lol")
                            .description("what is up")
                            .genre("Sad")
                            .build();

                Book helloEdited = Book.builder()
                            .title("Bye")
                            .author("changed")
                            .description("what is down")
                            .genre("Happy")
                            .build();

                String requestBody = mapper.writeValueAsString(helloEdited);

                when(bookRepository.findById(eq(67L))).thenReturn(Optional.of(helloOrig));

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/books?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isOk()).andReturn();

                // assert
                verify(bookRepository, times(1)).findById(67L);
                verify(bookRepository, times(1)).save(helloEdited); // should be saved with updated info
                String responseString = response.getResponse().getContentAsString();
                assertEquals(requestBody, responseString);
        }

        @WithMockUser(roles = { "ADMIN", "USER" })
        @Test
        public void admin_cannot_edit_book_that_does_not_exist() throws Exception {
                // arrange

                Book editedBook = Book.builder()
                                .title("red")
                                .author("dead")
                                .description("okman")
                                .genre("yo")
                                .build();

                String requestBody = mapper.writeValueAsString(editedBook);

                when(bookRepository.findById(eq(67L))).thenReturn(Optional.empty());

                // act
                MvcResult response = mockMvc.perform(
                                put("/api/books?id=67")
                                                .contentType(MediaType.APPLICATION_JSON)
                                                .characterEncoding("utf-8")
                                                .content(requestBody)
                                                .with(csrf()))
                                .andExpect(status().isNotFound()).andReturn();

                // assert
                verify(bookRepository, times(1)).findById(67L);
                Map<String, Object> json = responseToJson(response);
                assertEquals("Book with id 67 not found", json.get("message"));

        }
    
}

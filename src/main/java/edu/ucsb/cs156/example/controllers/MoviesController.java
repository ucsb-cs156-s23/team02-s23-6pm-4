package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Movie;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.MovieRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;


@Api(description = "Movies")
@RequestMapping("/api/movies")
@RestController
@Slf4j
public class MoviesController extends ApiController {

    @Autowired
    MovieRepository movierepository;

    @ApiOperation(value = "List all movies")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Movie> allMovies() {
        Iterable<Movie> movies = movierepository.findAll();
        return movies;
    }

    @ApiOperation(value = "Get a single movie")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Movie getById(
            @ApiParam("id") @RequestParam String id) {
        Movie moviE = movierepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Movie.class, id));
        return moviE;
    }

    @ApiOperation(value = "Create a new movie")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Movie postMovie(
        @ApiParam("id") @RequestParam String id,
        @ApiParam("title") @RequestParam String title,
        @ApiParam("director") @RequestParam String director,
        @ApiParam("release_year") @RequestParam long release_year

        )
        {

        Movie moviE = new Movie();
        moviE.setId(id);
        moviE.setTitle(title);
        moviE.setDirector(director);
        moviE.setRelease_year(release_year);
       
        Movie savedMovie = movierepository.save(moviE);

        return savedMovie;
    }

    @ApiOperation(value = "Delete a Movie")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteMovie(
            @ApiParam("id") @RequestParam String id) {
        Movie moviE = movierepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Movie.class, id));

        movierepository.delete(moviE);
        return genericMessage("Movie with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single movie")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Movie updateMovie(
            @ApiParam("id") @RequestParam String id,
            @RequestBody @Valid Movie incoming) {

        Movie moviE = movierepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Movie.class, id));


        moviE.setTitle(incoming.getTitle());  
        moviE.setDirector(incoming.getDirector());
        moviE.setRelease_year(incoming.getRelease_year());

        movierepository.save(moviE);

        return moviE;
    }
}

package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Restaurant;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.RestaurantRepository;
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


@Api(description = "Restaurant")
@RequestMapping("/api/restaurant")
@RestController
@Slf4j
public class RestaurantController extends ApiController {


    @Autowired
    RestaurantRepository RestaurantRepository;

    @ApiOperation(value = "List all restaurants")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Restaurant> allRestaurants() {
        Iterable<Restaurant> restaurants = RestaurantRepository.findAll();
        return restaurants;
    }

    @ApiOperation(value = "Get a single restaurant")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Restaurant getById(
            @ApiParam("code") @RequestParam String code) {
        Restaurant restaurants = RestaurantRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(Restaurant.class, code));

        return restaurants;
    }

    @ApiOperation(value = "Create a new restaurant")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Restaurant postRestaurants(
        @ApiParam("code") @RequestParam String code,
        @ApiParam("name") @RequestParam String name,
        @ApiParam("descript") @RequestParam String descript,
        @ApiParam("yelp_rating") @RequestParam String yelp_rating
        )
        {

        Restaurant restaurants = new Restaurant();
        restaurants.setCode(code);
        restaurants.setName(name);
        restaurants.setDescript(descript);
        restaurants.setYelp_rating(yelp_rating);
    

        Restaurant savedRestaurant = RestaurantRepository.save(restaurants);

        return savedRestaurant;
    }

    @ApiOperation(value = "Delete a Restaurant")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteRestaurant(
            @ApiParam("code") @RequestParam String code) {
        Restaurant restaurants = RestaurantRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(Restaurant.class, code));

        RestaurantRepository.delete(restaurants);
        return genericMessage("Restaurant with id %s deleted".formatted(code));
    }

    @ApiOperation(value = "Update a single restaurant")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Restaurant updateRestaurant(
            @ApiParam("code") @RequestParam String code,
            @RequestBody @Valid Restaurant incoming) {

        Restaurant restaurants = RestaurantRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(Restaurant.class, code));


        restaurants.setName(incoming.getName());  
        restaurants.setDescript(incoming.getDescript());
        restaurants.setYelp_rating(incoming.getYelp_rating());
    

        RestaurantRepository.save(restaurants);

        return restaurants;
    }
}

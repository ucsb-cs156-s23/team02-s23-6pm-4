package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Apartment;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.ApartmentRepository;
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


@Api(description = "Apartment")
@RequestMapping("/api/apartment")
@RestController
@Slf4j
public class ApartmentController extends ApiController {

    @Autowired
    ApartmentRepository apartmentRepository;

    @ApiOperation(value = "List all apartments")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Apartment> allApartments() {
        Iterable<Apartment> apartments = apartmentRepository.findAll();
        return apartments;
    }

    @ApiOperation(value = "Get a single apartment")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Apartment getById(
            @ApiParam("code") @RequestParam String code) {
        Apartment apartment = apartmentRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(Apartment.class, code));

        return apartment;
    }

    @ApiOperation(value = "Create a new apartment")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Apartment postApartment(
        @ApiParam("code") @RequestParam String code,
        @ApiParam("name") @RequestParam String name,
        @ApiParam("address") @RequestParam String address,
        @ApiParam("city") @RequestParam String city,
        @ApiParam("state") @RequestParam String state,
        @ApiParam("rooms") @RequestParam int rooms,
        @ApiParam("description") @RequestParam String description
        )
        {

        Apartment apartment = new Apartment();
        apartment.setCode(code);
        apartment.setName(name);
        apartment.setAddress(address);
        apartment.setCity(city);
        apartment.setState(state);
        apartment.setRooms(rooms);
        apartment.setDescription(description);

        Apartment savedApartment = apartmentRepository.save(apartment);

        return savedApartment;
    }

    @ApiOperation(value = "Delete an Apartment")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteApartment(
            @ApiParam("code") @RequestParam String code) {
        Apartment apartment = apartmentRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(Apartment.class, code));

        apartmentRepository.delete(apartment);
        return genericMessage("Apartment with id %s deleted".formatted(code));
    }

    @ApiOperation(value = "Update a single apartment")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Apartment updateCommons(
            @ApiParam("code") @RequestParam String code,
            @RequestBody @Valid Apartment incoming) {

        Apartment apartment = apartmentRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(Apartment.class, code));


        apartment.setName(incoming.getName());  
        apartment.setAddress(incoming.getAddress());
        apartment.setCity(incoming.getCity());
        apartment.setState(incoming.getState());
        apartment.setRooms(incoming.getRooms());
        apartment.setDescription(incoming.getDescription());

        apartmentRepository.save(apartment);

        return apartment;
    }
}

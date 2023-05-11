package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Painting;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.PaintingRepository;
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


@Api(description = "Painting")
@RequestMapping("/api/painting")
@RestController
@Slf4j
public class PaintingController extends ApiController {

    @Autowired
    PaintingRepository paintingRepository;

    @ApiOperation(value = "List all paintings")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Painting> allPaintingss() {
        Iterable<Painting> paintings = paintingRepository.findAll();
        return paintings;
    }

    @ApiOperation(value = "Get a single paintings")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Painting getById(
            @ApiParam("code") @RequestParam String code) {
        Painting paintings = paintingRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(Painting.class, code));

        return paintings;
    }

    @ApiOperation(value = "Create a new paintings")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Painting postPaintings(
        @ApiParam("code") @RequestParam String code,
        @ApiParam("name") @RequestParam String name,
        @ApiParam("artist") @RequestParam String artist,
        @ApiParam("year") @RequestParam int year,
        @ApiParam("medium") @RequestParam String medium,
        @ApiParam("period") @RequestParam String period
        )
        {

        Painting paintings = new Painting();
        paintings.setCode(code);
        paintings.setName(name);
        paintings.setArtist(artist);
        paintings.setYear(year);
        paintings.setMedium(medium);
        paintings.setPeriod(period);

        Painting savedPaintings = paintingRepository.save(paintings);

        return savedPaintings;
    }

    @ApiOperation(value = "Delete a Painting")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deletePaintings(
            @ApiParam("code") @RequestParam String code) {
        Painting paintings = paintingRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(Painting.class, code));

        paintingRepository.delete(paintings);
        return genericMessage("Painting with id %s deleted".formatted(code));
    }

    @ApiOperation(value = "Update a single paintings")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Painting updatePaintings(
            @ApiParam("code") @RequestParam String code,
            @RequestBody @Valid Painting incoming) {

        Painting paintings = paintingRepository.findById(code)
                .orElseThrow(() -> new EntityNotFoundException(Painting.class, code));


        paintings.setName(incoming.getName());  
        paintings.setArtist(incoming.getArtist());
        paintings.setYear(incoming.getYear());
        paintings.setMedium(incoming.getMedium());
        paintings.setPeriod(incoming.getPeriod());

        paintingRepository.save(paintings);

        return paintings;
    }
}

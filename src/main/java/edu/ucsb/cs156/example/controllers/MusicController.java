package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Music;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.MusicRepository;
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


@Api(description = "Music")
@RequestMapping("/api/music")
@RestController
@Slf4j
public class MusicController extends ApiController {

    @Autowired
    MusicRepository musicRepository;

    @ApiOperation(value = "List all Musics")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Music> allMusics() {
        Iterable<Music> musics = musicRepository.findAll();
        return musics;
    }

    @ApiOperation(value = "Get a single musics")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Music getById(
            @ApiParam("id") @RequestParam Long id) {
        Music musics = musicRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Music.class, id));

        return musics;
    }

    @ApiOperation(value = "Create a new music")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Music postMusic(
        @ApiParam("title") @RequestParam String title,
        @ApiParam("album") @RequestParam String album,
        @ApiParam("artist") @RequestParam String artist,
        @ApiParam("genre") @RequestParam String genre

        )
        {

        Music musics = new Music();
        musics.setTitle(title);
        musics.setAlbum(album);
        musics.setArtist(artist);
        musics.setGenre(genre);

        Music savedMusics = musicRepository.save(musics);

        return savedMusics;
    }

    @ApiOperation(value = "Delete a Music")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteMusics(
            @ApiParam("id") @RequestParam Long id) {
        Music musics = musicRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Music.class, id));
        musicRepository.delete(musics);
        return genericMessage("Music with id %s deleted".formatted(id));
    }

    @ApiOperation(value = "Update a single musics")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Music updateMusics(
            @ApiParam("id") @RequestParam Long id,
            @RequestBody @Valid Music incoming) {

        Music musics = musicRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Music.class, id));


        musics.setTitle(incoming.getTitle());  
        musics.setAlbum(incoming.getAlbum());
        musics.setArtist(incoming.getArtist());
        musics.setGenre(incoming.getGenre());

        musicRepository.save(musics);

        return musics;
    }
}

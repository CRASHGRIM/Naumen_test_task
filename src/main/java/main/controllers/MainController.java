package main.controllers;

import com.google.gson.Gson;
import main.common.ConfProperties;
import main.common.CustomDB;
import main.models.Note;
import org.aspectj.weaver.patterns.HasMemberTypePatternForPerThisMatching;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Optional;

@RestController
public class MainController {

    private CustomDB customDB;

    private int titleMaxLength;


    MainController(ConfProperties properties) {
        customDB = new CustomDB(properties);
        titleMaxLength = properties.titleSize;
    }


    @GetMapping("/notes")
    ResponseEntity<String> getNotes(@RequestParam("title") Optional<String> title,
                                    @RequestParam("content") Optional<String> content) {
        String outList = "";
        if (!title.isPresent() && !content.isPresent())
            outList = customDB.getAll();
        else if (title.isPresent())
            outList = customDB.findByTitle(title.get());
        else if (content.isPresent())
            outList = customDB.findByContent(content.get());
        return new ResponseEntity<String>(outList, HttpStatus.OK);
    }


    @PostMapping("/notes")
    ResponseEntity<String> addNote(@RequestBody String newNote) {
        JSONObject parsedNote = new JSONObject(newNote);
        if (!parsedNote.has("content"))
            return new ResponseEntity<String>("no content",
                HttpStatus.BAD_REQUEST);
        String title;
        if (parsedNote.has("title"))
            title = parsedNote.getString("title");
        else
            title = parsedNote.getString("content").substring(0, titleMaxLength);
        String content = parsedNote.getString("content");
        Note note = new Note(title, content);
        customDB.WriteNote(note);
        Gson gson = new Gson();
        return new ResponseEntity<String>(gson.toJson(note), HttpStatus.OK);
    }

    @GetMapping("/notes/{id}")
    ResponseEntity<String> getNoteByID(@PathVariable Long id) {
        return new ResponseEntity<String>(customDB.GetRecordById(id), HttpStatus.OK); //ToDO если пришла пустая строка то стоит возвращать 400
    }


    @PutMapping("/notes/{id}")
    void updateNoteByID(@PathVariable Long id, @RequestBody String toUpdate)
    {
        customDB.updateNoteByID(id, toUpdate);
    }

    @DeleteMapping("/notes/{id}")
    void deleteNoteByID(@PathVariable Long id) {
        customDB.DeleteLineByID(id); //ToDo проверить что такая строка вообще была
    }

}


package main.controllers;

import com.google.gson.Gson;
import main.common.CustomDB;
import main.models.Note;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;

import java.util.Dictionary;
import java.util.HashMap;

@RestController
public class MainController {

    private CustomDB customDB;


    MainController() {
        customDB = new CustomDB();
    }


    @GetMapping("/notes")
    String getNotes() {
        Gson gson = new Gson();
        return "";
    }


    @PostMapping("/notes")
    String addNote(@RequestBody JSONObject newNote) {
        String title = (String) newNote.get("title");
        String content = (String) newNote.get("content");
        Note note = new Note(title, content);
        customDB.WriteNote(note);
        Gson gson = new Gson();
        return gson.toJson(note);
    }

    // Single item

    @GetMapping("/notes/{id}")
    String getNoteByID(@PathVariable Long id) {
        return customDB.GetRecordById(id);
    }

    /*@GetMapping("/notes")
    String getNoteByTitle(@RequestParam String title){

        return customDB.findByTitle(title);
    }*/

    /*@GetMapping("/notes")
    String getNoteByContent(@RequestParam String content){

        return customDB.findByContent(content);
    }*/

    @DeleteMapping("/notes/{id}")
    void deleteNoteByID(@PathVariable Long id) {
        customDB.DeleteLineByID(id);
    }

}


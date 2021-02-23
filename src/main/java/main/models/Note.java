package main.models;

import com.google.gson.Gson;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
public class Note {

    private Long id;
    private String title;
    private String content;
    private static Long currentIndex = 0L;

    public Note(String content)
    {
        this.id = createID();
        this.content = content;
        this.title = "";
    }

    public Note(String title, String content)
    {
        this.id = createID();
        this.title = title;
        this.content = content;
    }

    private static long createID()
    {
        currentIndex++;
        return currentIndex;
    }

    public Long getId(){return id;}

    public String getTitle(){return title;}

    public void setTitle(String title){this.title = title;}

    public String getContent(){return content;}

    public void setContent(String content){this.content = content;}


    public String toString()
    {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}

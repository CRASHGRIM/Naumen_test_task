package main.common;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

public class IndexTreeEntry {

    private Long ID;
    private ArrayList<Long> endedStrings;
    private HashMap<Character, Long> childs;

    IndexTreeEntry(Long ID)
    {
        endedStrings = new ArrayList<Long>();
        childs = new HashMap<>();
        this.ID = ID;
    }

    public ArrayList<Long> getEndedStrings()
    {
        return endedStrings;
    }

    public HashMap<Character, Long> getChilds()
    {
        return childs;
    }

    public Long getID()
    {
        return ID;
    }

    public void AddChild(char letter, Long childID)
    {
        this.childs.put(letter, childID);
    }

    public void AddEndedString(Long index)
    {
        endedStrings.add(index);
    }

    public void DeleteEndedString(Long index)
    {
        endedStrings.remove((Long) index);
    }

    public String toString()
    {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

}

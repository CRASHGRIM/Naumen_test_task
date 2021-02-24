package main.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Tree {

    private ArrayList<Long> endedStrings;
    private HashMap<Character, Tree> childs;
    private final char letter;

    Tree(char letter)
    {
        endedStrings = new ArrayList<Long>();
        childs = new HashMap<>();
        this.letter = letter;
    }

    public ArrayList<Long> getEndedStrings()
    {
        return endedStrings;
    }

    public HashMap<Character, Tree> getChilds()
    {
        return childs;
    }

    public char getLetter()
    {
        return letter;
    }

    public void AddChild(Tree tree)
    {
        this.childs.put(tree.letter, tree);
    }

    public void AddEndedString(Long index)
    {
        endedStrings.add(index);
    }

    public void DeleteEndedString(Long index)
    {
        endedStrings.remove((Long) index);
    }


}

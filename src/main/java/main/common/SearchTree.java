package main.common;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class SearchTree {

    private Tree tree;


    public SearchTree(Tree tree)
    {
        this.tree = tree;
    }

    public ArrayList<Long> Search(String searchString){
        Tree currentTree = tree;
        for(int i=0; i<searchString.length();i++)
        {
            if (currentTree.getChilds().containsKey(searchString.charAt(i)))
                currentTree = currentTree.getChilds().get(searchString.charAt(i));
            else
            {
                return new ArrayList<>();
            }
        }
        return currentTree.getEndedStrings();
    }

    public void Add(String text, long strIndex)
    {
        Tree currentTree = tree;
        for(int i=0; i<text.length();i++)
        {
            if (currentTree.getChilds().containsKey(text.charAt(i)))
                currentTree = currentTree.getChilds().get(text.charAt(i));
            else
            {
                var treeToAdd = new Tree(text.charAt(i));
                currentTree.AddChild(treeToAdd);
                currentTree = currentTree.getChilds().get(text.charAt(i));
            }
            currentTree.AddEndedString(strIndex);
        }
    }

    public void Delete(String text, long strIndex)
    {
        Tree currentTree = tree;
        for(int i=0; i<text.length();i++)
        {
            if (currentTree.getChilds().containsKey(text.charAt(i)))
                currentTree = currentTree.getChilds().get(text.charAt(i));
            else
            {
                return;// такого слова не было в дереве
            }
            currentTree.AddEndedString(strIndex);
        }
    }

}

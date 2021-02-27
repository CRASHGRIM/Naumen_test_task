package main.common;

import com.google.gson.Gson;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class SearchTree {

    private Long index;
    private String indexParameterName;
    private int fragmentation;


    public SearchTree(String indexParameterName, int fragmentSize) {
        index = 0L;
        fragmentation = fragmentSize;
        this.indexParameterName = indexParameterName;
        addNewIndexTreeEntry();
    }

    public ArrayList<Long> search(String searchString) {

        IndexTreeEntry currentEntry = getRecord(0L);
        for (int i = 0; i < searchString.length(); i++) {
            if (currentEntry.getChilds().containsKey(searchString.charAt(i)))
            {
                var newIndex = currentEntry.getChilds().get(searchString.charAt(i));
                currentEntry = getRecord(newIndex);
            }
            else {
                return new ArrayList<>(); // не смогли спуститься по всему слову, ничего не найдено
            }
        }
        return currentEntry.getEndedStrings();
    }

    public void add(String text, long strIndex) {
        IndexTreeEntry currentEntry = getRecord(0L); // самая первая запись откуда начинается поиск
        for (int i = 0; i < text.length(); i++) {
            if (currentEntry.getChilds().containsKey(text.charAt(i)))
            {
                var newIndex = currentEntry.getChilds().get(text.charAt(i));
                currentEntry = getRecord(newIndex);
            }
            else {
                var newIndex = addNewIndexTreeEntry();
                currentEntry.addChild(text.charAt(i), newIndex);
                rewriteEntry(currentEntry);
                newIndex = currentEntry.getChilds().get(text.charAt(i));
                currentEntry = getRecord(newIndex);
            }
        }
        currentEntry.addEndedString(strIndex);
        rewriteEntry(currentEntry);
    }

    private void rewriteEntry(IndexTreeEntry entry)
    {
        var fragName = getIndexFragmentNameByID(entry.getID());

        try {
            File myObj = new File(fragName+"_temp");
            myObj.createNewFile();
        } catch (IOException e) {
            System.out.println("Error when creating temp index fragment");
            e.printStackTrace();
            return;
        }

        FileWriter DBwriter = null;

        try {
            DBwriter = new FileWriter(fragName+"_temp", true);
        } catch (IOException e) {
            System.out.println("Error when creating index writer");
            e.printStackTrace();
        }
        Scanner scanner;
        Path path = Paths.get(fragName);
        try {
            scanner = new Scanner(path);
        }
        catch (IOException e)
        {
            System.out.println("Error when finding index fragment to read");
            return;
        }

        scanner.useDelimiter(System.getProperty("line.separator"));
        try{
            while (scanner.hasNext())
            {
                var readedline = scanner.nextLine();
                if (new JSONObject(readedline).getLong("ID")!=entry.getID())
                {
                    DBwriter.write(readedline);
                    DBwriter.write("\n");
                }
                else
                {
                    DBwriter.write(entry.toString());
                    DBwriter.write("\n");
                }
            }
            DBwriter.close();
        }
        catch (Exception e)
        {
            System.out.println("error with writing temp");
            try{
                DBwriter.close();
            }
            catch (Exception r)
            {
                System.out.println("problems with index writer closing");
            }
            scanner.close();
        }
        scanner.close();
        File oldFragment = new File(fragName);
        oldFragment.delete();
        File newFragment = new File(fragName+"_temp");
        newFragment.renameTo(oldFragment);

    }

    public void delete(String text, long strIndex) {
        IndexTreeEntry currentEntry = getRecord(0L); // самая первая запись откуда начинается поиск
        for (int i = 0; i < text.length(); i++) {
            if (currentEntry.getChilds().containsKey(text.charAt(i)))
            {
                var newIndex = currentEntry.getChilds().get(text.charAt(i));
                currentEntry = getRecord(newIndex);
            }
            else {
                return;// такого слова не было в дереве
            }
            currentEntry.deleteEndedString(strIndex);
        }
    }


    public String getIndexFragmentNameByID(Long ID) {
        long fragmentIndex = ID - ID % fragmentation;
        var fragmentName = String.format(System.getProperty("user.dir") + "/DBrecords/indexes/index_%s_%s.txt", indexParameterName, Long.toString(fragmentIndex));
        return fragmentName;
    }

    public IndexTreeEntry getRecord(Long ID) {
        var fragmentName = getIndexFragmentNameByID(ID);
        Scanner scanner;
        Path path = Paths.get(fragmentName);
        try {
            scanner = new Scanner(path);
        }
        catch (IOException e)
        {
            System.out.println("Error when finding fragment to read");
            return null;
        }

        scanner.useDelimiter(System.getProperty("line.separator"));
        while (scanner.hasNext())
        {
            var readedline = scanner.nextLine();
            if (new JSONObject(readedline).getLong("ID") ==ID)
            {
                scanner.close();
                Gson gson = new Gson();
                return gson.fromJson(readedline, IndexTreeEntry.class);
            }
        }
        return null;

    }

    private Long addNewIndexTreeEntry()
    {
        var newEntry = new IndexTreeEntry(index);
        index++;
        Gson gson = new Gson();
        var serialised =  gson.toJson(newEntry);
        var fragmentName = getIndexFragmentNameByID(newEntry.getID());

        if (newEntry.getID()%fragmentation==0)
        {
            try {
                File myObj = new File(fragmentName);
                myObj.createNewFile();
            } catch (IOException e) {
                System.out.println("Error when creating new index fragment");
                e.printStackTrace();
                return 0L;
            }
        }

        try {
            FileWriter DBwriter = new FileWriter(fragmentName, true);
            DBwriter.write(serialised);
            DBwriter.write("\n");
            DBwriter.close();
            return newEntry.getID();
        } catch (IOException e) {
            System.out.println("Error when writing note to fragment");
            e.printStackTrace();
            return 0L;
        }


    }

}

package main.common;

import com.google.gson.Gson;
import main.models.Note;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Scanner;
import java.util.function.Function;

@Component
public class CustomDB {

    private int fragmentantion;

    private SearchTree titleSearchTree;

    private SearchTree contentSearchTree;

    private ConfProperties properties;

    public CustomDB(ConfProperties confProperties) {
        File DBdir = new File(System.getProperty("user.dir")+"/DBrecords");
        if (DBdir.exists()){
            for(File file: DBdir.listFiles())
                if (!file.isDirectory())
                    file.delete();
        }
        else
            DBdir.mkdirs();

        this.properties = confProperties;
        fragmentantion = properties.fragmentSize;
        titleSearchTree = new SearchTree("title", properties.indexFragmentSize);
        contentSearchTree = new SearchTree("content", properties.indexFragmentSize);
    }

    public void WriteNote(Note note)
    {
        long fragmentIndex = note.getId()-note.getId()%fragmentantion;
        var fragmentName = String.format(System.getProperty("user.dir")+"/DBrecords/%s.txt", Long.toString(fragmentIndex));

        if (note.getId()%fragmentantion==0)
        {
            try {
                File myObj = new File(fragmentName);
                myObj.createNewFile();
            } catch (IOException e) {
                System.out.println("Error when creating new fragment");
                e.printStackTrace();
                return;
            }
        }

        try {
            FileWriter DBwriter = new FileWriter(fragmentName, true);
            DBwriter.write(note.toString());
            DBwriter.write("\n");
            DBwriter.close();
        } catch (IOException e) {
            System.out.println("Error when writing note to fragment");
            e.printStackTrace();
        }


        titleSearchTree.Add(note.getTitle(), note.getId());
        contentSearchTree.Add(note.getContent(), note.getId());




    }

    public String GetRecordById(long ID){

        long fragmentIndex = ID-ID%fragmentantion;
        var fragmentName = String.format(System.getProperty("user.dir")+"/DBrecords/%s.txt", Long.toString(fragmentIndex));

        Scanner scanner;
        Path path = Paths.get(fragmentName);
        try {
            scanner = new Scanner(path);
        }
        catch (IOException e)
        {
            System.out.println("Error when finding fragment to read");
            return "";
        }

        scanner.useDelimiter(System.getProperty("line.separator"));
        while (scanner.hasNext())
        {
            var readedline = scanner.nextLine();
            if (getIDFromLine(readedline)==ID)
            {
                scanner.close();
                return readedline;
            }
        }
        return "";

    }

    public void DeleteLineByID(long ID){
        var fragName = getFragmentNameFromID(ID);

        String title = ""; // нужен для того чтобы в дереве быстро найти индекс не перебирая все дерево
        String content = "";

        try {
            File myObj = new File(fragName+"_temp");
            myObj.createNewFile();
        } catch (IOException e) {
            System.out.println("Error when creating temp fragment");
            e.printStackTrace();
            return;
        }

        FileWriter DBwriter = null;

        try {
            DBwriter = new FileWriter(fragName+"_temp", true);
            //DBwriter.write(note.toString());
            //DBwriter.write("\n");
        } catch (IOException e) {
            System.out.println("Error when creating writer");
            e.printStackTrace();
        }

        Scanner scanner;
        Path path = Paths.get(fragName);
        try {
            scanner = new Scanner(path);
        }
        catch (IOException e)
        {
            System.out.println("Error when finding fragment to read");
            return;
        }

        scanner.useDelimiter(System.getProperty("line.separator"));
        try{
            while (scanner.hasNext())
            {
                var readedline = scanner.nextLine();
                if (getIDFromLine(readedline)!=ID)
                {
                    DBwriter.write(readedline);
                    DBwriter.write("\n");
                }
                else
                {
                    var parsedObj = new JSONObject(readedline);
                    title = parsedObj.getString("title"); // нужно для более быстрого удаления строки из дерева
                    content = parsedObj.getString("content");
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
                System.out.println("problems with writer closing");
            }
            scanner.close();  //здесь надо бы поинтеллектуальнее свалиться
        }
        scanner.close();
        File oldFragment = new File(fragName);
        oldFragment.delete();
        File newFragment = new File(fragName+"_temp");
        newFragment.renameTo(oldFragment);

        titleSearchTree.Delete(title, ID);
        contentSearchTree.Delete(content, ID);

    }

    private String getFragmentNameFromID(long ID)
    {
        long fragmentIndex = ID-ID%fragmentantion;
        var fragmentName = String.format(System.getProperty("user.dir")+"/DBrecords/%s.txt", Long.toString(fragmentIndex));
        return fragmentName;
    }

    private long getIDFromLine(String line)
    {
        var gson = new Gson();
        var note = gson.fromJson(line, Note.class);
        return note.getId();
    }


    public String findByTitle(String title)
    {
        var found = titleSearchTree.Search(title);
        Collections.sort(found);
        var outString = new StringBuilder();
        outString.append('[');
        for (Long index:found)
        {
            outString.append(GetRecordById(index));
            outString.append(',');
        }
        if (outString.charAt(outString.length()-1)==',')
            outString.replace(outString.length()-1, outString.length(), "");
        outString.append(']');
        return outString.toString();
    }


    public String findByContent(String content)
    {
        var found = contentSearchTree.Search(content);
        Collections.sort(found);
        var outString = new StringBuilder();
        outString.append('[');
        for (Long index:found)
        {
            outString.append(GetRecordById(index));
            outString.append(',');
        }
        if (outString.charAt(outString.length()-1)==',')
            outString.replace(outString.length()-1, outString.length(), "");
        outString.append(']');
        return outString.toString();
    }

    public String getAll()  // TODO выдается содержимое всех файлов, надо отфильтровать только сами записи, возможно разделить по папкам
    {
        var folder = new File(System.getProperty("user.dir")+"/DBrecords");
        StringBuilder outString = new StringBuilder();
        outString.append('[');
        for (final File fileEntry : folder.listFiles()) {
            if (!fileEntry.isDirectory()) {

                Scanner scanner;
                Path path = Paths.get(fileEntry.getAbsolutePath());
                try {
                    scanner = new Scanner(path);
                }
                catch (IOException e)
                {
                    System.out.println("Error when finding fragment to read");
                    return "";
                }

                scanner.useDelimiter(System.getProperty("line.separator"));
                try{
                    while (scanner.hasNext())
                    {
                        var readedline = scanner.nextLine();
                        outString.append(readedline);
                        outString.append(",");
                    }
                }
                catch (Exception e)
                {
                    System.out.println("error with reading");
                    scanner.close();  //ToDO здесь надо бы поинтеллектуальнее свалиться
                }
                scanner.close();



            }
        }
        if (outString.charAt(outString.length()-1)==',')
            outString.replace(outString.length()-1, outString.length(), "");
        outString.append(']');
        return outString.toString();
    }


    public void updateNoteByID(Long ID, String toUpdate)
    {
        var fragName = getFragmentNameFromID(ID);

        try {
            File myObj = new File(fragName+"_temp");
            myObj.createNewFile();
        } catch (IOException e) {
            System.out.println("Error when creating temp fragment");
            e.printStackTrace();
            return;
        }

        FileWriter DBwriter = null;

        try {
            DBwriter = new FileWriter(fragName+"_temp", true);
        } catch (IOException e) {
            System.out.println("Error when creating writer");
            e.printStackTrace();
        }

        Scanner scanner;
        Path path = Paths.get(fragName);
        try {
            scanner = new Scanner(path);
        }
        catch (IOException e)
        {
            System.out.println("Error when finding fragment to read");
            return;
        }

        scanner.useDelimiter(System.getProperty("line.separator"));
        try{
            while (scanner.hasNext())
            {
                var readedline = scanner.nextLine();
                if (getIDFromLine(readedline)!=ID)
                {
                    DBwriter.write(readedline);
                    DBwriter.write("\n");
                }
                else
                {
                    var readedlineParsed = new JSONObject(readedline);
                    var toUpdateParsed = new JSONObject(toUpdate);
                    if (toUpdateParsed.has("content")) {
                        contentSearchTree.Delete(readedlineParsed.getString("content"), readedlineParsed.getLong("id"));
                        contentSearchTree.Add(toUpdateParsed.getString("content"), readedlineParsed.getLong("id"));
                        readedlineParsed.put("content", toUpdateParsed.get("content"));
                    }
                    if (toUpdateParsed.has("title")) {
                        titleSearchTree.Delete(readedlineParsed.getString("title"), readedlineParsed.getLong("id"));
                        titleSearchTree.Add(toUpdateParsed.getString("title"), readedlineParsed.getLong("id"));
                        readedlineParsed.put("title", toUpdateParsed.get("title"));
                    }
                    DBwriter.write(readedlineParsed.toString());
                    DBwriter.write("\n");
                }


            }
            DBwriter.close();
        }
        catch (Exception e)
        {
            System.out.println("error with writing temp");
            e.printStackTrace();
            try{
                DBwriter.close();
            }
            catch (Exception r)
            {
                System.out.println("problems with writer closing");
            }
            scanner.close();  //здесь надо бы поинтеллектуальнее свалиться
        }
        scanner.close();
        File oldFragment = new File(fragName);
        oldFragment.delete();
        File newFragment = new File(fragName+"_temp");
        newFragment.renameTo(oldFragment);

    }

}

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

@Component
public class CustomDB {

    private int fragmentantion;

    private SearchTree titleSearchTree;

    private SearchTree contentSearchTree;

    private ConfProperties properties;

    public CustomDB(ConfProperties confProperties) {
        makeDirectory("/DBrecords");
        makeDirectory("/DBrecords/indexes");
        makeDirectory("/DBrecords/records");

        this.properties = confProperties;
        fragmentantion = properties.fragmentSize;
        titleSearchTree = new SearchTree("title", properties.indexFragmentSize);
        contentSearchTree = new SearchTree("content", properties.indexFragmentSize);
    }

    private void makeDirectory(String path)
    {
        File dirToCreate = new File(System.getProperty("user.dir")+path);
        if (dirToCreate.exists()){
            for(File file: dirToCreate.listFiles())
                if (!file.isDirectory())
                    file.delete();
        }
        else
            dirToCreate.mkdirs();
    }

    public void writeNote(Note note)
    {
        long fragmentIndex = note.getId()-note.getId()%fragmentantion;
        var fragmentName = String.format(System.getProperty("user.dir")+"/DBrecords/records/%s.txt", Long.toString(fragmentIndex));

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


        titleSearchTree.add(note.getTitle(), note.getId());
        contentSearchTree.add(note.getContent(), note.getId());




    }

    public String getRecordById(long ID){

        long fragmentIndex = ID-ID%fragmentantion;
        var fragmentName = String.format(System.getProperty("user.dir")+"/DBrecords/records/%s.txt", Long.toString(fragmentIndex));

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

    public boolean deleteLineByID(long ID){
        var fragName = getFragmentNameFromID(ID);

        String title = ""; // нужен для того чтобы в дереве быстро найти индекс не перебирая все дерево
        String content = "";

        try {
            File myObj = new File(fragName+"_temp");
            myObj.createNewFile();
        } catch (IOException e) {
            System.out.println("Error when creating temp fragment");
            e.printStackTrace();
            return false;
        }

        FileWriter DBwriter = null;

        try {
            DBwriter = new FileWriter(fragName+"_temp", true);
        } catch (IOException e) {
            System.out.println("Error when creating writer"); // ToDo удалить файлы и выйти
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
            return false;
        }

        scanner.useDelimiter(System.getProperty("line.separator"));
        Boolean isRecordFound = false;
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
                    isRecordFound = true;
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

        titleSearchTree.delete(title, ID);
        contentSearchTree.delete(content, ID);
        return isRecordFound;

    }

    private String getFragmentNameFromID(long ID)
    {
        long fragmentIndex = ID-ID%fragmentantion;
        var fragmentName = String.format(System.getProperty("user.dir")+"/DBrecords/records/%s.txt", Long.toString(fragmentIndex));
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
        var found = titleSearchTree.search(title);
        Collections.sort(found);
        var outString = new StringBuilder();
        outString.append('[');
        for (Long index:found)
        {
            outString.append(getRecordById(index));
            outString.append(',');
        }
        if (outString.charAt(outString.length()-1)==',')
            outString.replace(outString.length()-1, outString.length(), "");
        outString.append(']');
        return outString.toString();
    }


    public String findByContent(String content)
    {
        var found = contentSearchTree.search(content);
        Collections.sort(found);
        var outString = new StringBuilder();
        outString.append('[');
        for (Long index:found)
        {
            outString.append(getRecordById(index));
            outString.append(',');
        }
        if (outString.charAt(outString.length()-1)==',')
            outString.replace(outString.length()-1, outString.length(), "");
        outString.append(']');
        return outString.toString();
    }

    public String getAll()  // TODO выдается содержимое всех файлов, надо отфильтровать только сами записи, возможно разделить по папкам
    {
        var folder = new File(System.getProperty("user.dir")+"/DBrecords/records");
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
                        contentSearchTree.delete(readedlineParsed.getString("content"), readedlineParsed.getLong("id"));
                        contentSearchTree.add(toUpdateParsed.getString("content"), readedlineParsed.getLong("id"));
                        readedlineParsed.put("content", toUpdateParsed.get("content"));
                    }
                    if (toUpdateParsed.has("title")) {
                        titleSearchTree.delete(readedlineParsed.getString("title"), readedlineParsed.getLong("id"));
                        titleSearchTree.add(toUpdateParsed.getString("title"), readedlineParsed.getLong("id"));
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

package DBTests;


import main.MainApp;
import main.common.ConfProperties;
import main.common.CustomDB;
import main.models.Note;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = MainApp.class)
public class DBtests {


    private int titleMaxLength;

    private ConfProperties properties;



    DBtests(ConfProperties properties) {
        this.properties = properties;
        titleMaxLength = properties.titleSize;
    }

    @Test
    public void writeToDBTest()
    {
        var DB = new CustomDB(properties);
        var note = new Note("aaaaa", "bbb");
        System.out.println(note.toString());
        for(int i=0;i<10;i++)
        {
            DB.writeNote(note);
        }
        for(int i=0;i<10;i++)
        {
            var readed = DB.getRecordById(i);
            assert readed.equals(note.toString());
        }
    }

    @Test
    public void writeDeleteToDBTest()
    {
        var DB = new CustomDB(properties);
        for(int i=0;i<10;i++)
        {
            var note = new Note("aaaaa", "bbb");
            DB.writeNote(note);
        }
        DB.deleteLineByID(3L);
        DB.deleteLineByID(7L);
    }

    @Test
    public void findByTitleToDBTest()
    {
        var DB = new CustomDB(properties);
        for(int i=1;i<10;i++)
        {
            var note = new Note(String.valueOf(i), "bbb");
            DB.writeNote(note);
        }
        System.out.println(DB.findByTitle("7"));
        assert DB.findByTitle("7").equals("{\"id\":7,\"title\":\"7\",\"content\":\"bbb\"}");
    }




}

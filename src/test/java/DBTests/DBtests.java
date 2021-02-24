package DBTests;


import main.MainApp;
import main.common.ConfProperties;
import main.common.CustomDB;
import main.models.Note;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = MainApp.class)
public class DBtests {

    @Test
    public void WriteToDBTest(ConfProperties properties)
    {
        var DB = new CustomDB(properties);
        var note = new Note("aaaaa", "bbb");
        System.out.println(note.toString());
        for(int i=0;i<10;i++)
        {
            DB.WriteNote(note);
        }
        for(int i=0;i<10;i++)
        {
            var readed = DB.GetRecordById(i);
            assert readed.equals(note.toString());
        }
    }

    @Test
    public void WriteDeleteToDBTest(ConfProperties properties)
    {
        var DB = new CustomDB(properties);
        for(int i=0;i<10;i++)
        {
            var note = new Note("aaaaa", "bbb");
            DB.WriteNote(note);
        }
        DB.DeleteLineByID(3L);
        DB.DeleteLineByID(7L);
    }

    @Test
    public void FindByTitleToDBTest(ConfProperties properties)
    {
        var DB = new CustomDB(properties);
        for(int i=1;i<10;i++)
        {
            var note = new Note(String.valueOf(i), "bbb");
            DB.WriteNote(note);
        }
        System.out.println(DB.findByTitle("7"));
        assert DB.findByTitle("7").equals("{\"id\":7,\"title\":\"7\",\"content\":\"bbb\"}");
    }




}

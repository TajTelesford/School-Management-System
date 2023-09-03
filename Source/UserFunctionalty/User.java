package Source.UserFunctionalty;
import java.util.Random;
public class User 
{
    private String UserType;
    private int ID;
    private String Name;
    private String Email;
    private String Password;
    static int[] IDCache;
    static public int MAX_NUMBER_OF_IDS = 1000000;

    public enum UserTypes {
        Teacher, Student
    }

    public User(String type, String name, String email, String password)
    {
        if(IDCache == null) IDCache = new int[MAX_NUMBER_OF_IDS];
        UserType = type;
        Name = name;
        Email = email;
        Password = password;
        CreateID();
    }

    static int RandomNumber()
    {
        Random random = new Random();
        return random.nextInt(MAX_NUMBER_OF_IDS);
    }

    public void CreateID()
    {
        int checkID = RandomNumber();
        boolean c = IDCreationCheck(checkID, IDCache);
        do {
            checkID = RandomNumber();
            c = IDCreationCheck(checkID, IDCache);
        } while (!c);
        ID = checkID;
    } 
    
    public boolean IDCreationCheck(int id, final int[] IDCacheList)
    {
        if(IDCacheList.length < 0) IDCache[0] = id;

        for(int i = 0; i < IDCacheList.length; i++)
        {
            if(id == IDCacheList[i]) return false;
        }
        return true;
    }

    public String getUserType(){ return UserType; }
    public int getUserID(){ return ID; }
    public String getUserName(){ return Name; }
    public String getUserEmail(){ return Email; }
    public String getUserPassword(){ return Password; }
}

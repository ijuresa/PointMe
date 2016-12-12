package UserData;

/**
 * Created by ivanj on 23/10/2016.
 */

public class User implements IUser {

    //Getters and Setters
    public String getName() {
        return name;
    }
    public void setName(String _name) {
        this.name = _name;
    }
    public String getAge() {
        return age;
    }
    public void setAge(String _age) {
        this.age = _age;
    }
    public String getGender() {
        return gender;
    }
    public void setGender(String _gender) {
        this.gender = _gender;
    }

    //User data
    private String name;
    private String age;
    private String gender;

    //Constructors
    private User() { }

    private User(String _name, String _age, String _gender) {
        this.name = _name;
        this.age = _age;
        this.gender = _gender;
    }

    private static final User _user = new User();
    public static User getUser() { return _user; }
}

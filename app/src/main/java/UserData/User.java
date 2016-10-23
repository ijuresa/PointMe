package UserData;

import android.widget.RadioButton;

/**
 * Created by ivanj on 23/10/2016.
 */

public class User implements IUser {
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getAge() {
        return age;
    }
    public void setAge(String age) {
        this.age = age;
    }
    public char getGender() {
        return gender;
    }
    public void setGender(char gender) {
        this.gender = gender;
    }

    private String name;
    private String age;
    private char gender;

    public User() { }

    public User(String _name, String _age, char _gender) {
        this.name = _name;
        this.age = _age;
        this.gender = _gender;
    }

}

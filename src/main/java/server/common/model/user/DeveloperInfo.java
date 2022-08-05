package server.common.model.user;

import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber;
import java.util.HashSet;
import lombok.Data;

@Data
public class DeveloperInfo {

    private String firstName;

    private String lastName;

    private PhoneNumber phoneNumber;

    private HashSet<String> developerOf;

    private HashSet<String> adminOf;
}

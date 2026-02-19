package CarPoolingSystem;

import java.util.UUID;

public class User {
    private String userId;
    private String name;
    private String emailId;
    public User(String name,String emailId){
        this.name=name;
        this.emailId=emailId;
        userId=UUID.randomUUID().toString();
    }

    public String getUserId(){
        return userId;
    }

    
    
}

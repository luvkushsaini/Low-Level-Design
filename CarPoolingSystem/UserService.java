package CarPoolingSystem;

import java.util.HashMap;
import java.util.Map;

public class UserService {
    private Map<String,User>users=new HashMap<>();

    public User createUser(String name,String email){
        User user=new User(name,email);
        users.put(user.getUserId(),user);

        return user;
    }
    public User getUser(String userId){
        return users.get(userId);
        
    }
}

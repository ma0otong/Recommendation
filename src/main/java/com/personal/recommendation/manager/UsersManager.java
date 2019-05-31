package com.personal.recommendation.manager;

import com.personal.recommendation.dao.UsersDAO;
import com.personal.recommendation.model.Users;
import com.personal.recommendation.utils.CustomizedHashMap;
import com.personal.recommendation.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 用户Manager
 */
@Service
public class UsersManager {

    private final UsersDAO usersDAO;
    @Autowired
    public UsersManager(UsersDAO usersDAO) {
        this.usersDAO = usersDAO;
    }

    public Users getUserById(long userId){
        return usersDAO.getUserById(userId);
    }

    public List<Users> getUsersByIds(List<Long> userIds){
        List<Users> list = new ArrayList<>();
        for(Long id : userIds){
            Users user = usersDAO.getUserById(id);
            if(user != null){
                list.add(user);
            }
        }
        return list;
    }

    @SuppressWarnings("unused")
    public void updateUserTimeStamp(Date timestamp){
        usersDAO.updateTimeStamp(timestamp);
    }

    public HashMap<Long, CustomizedHashMap<Integer, CustomizedHashMap<String, Double>>> getUserPrefListMap(List<Long> userIds) {
        HashMap<Long, CustomizedHashMap<Integer, CustomizedHashMap<String, Double>>> userPrefListMap = new HashMap<>();
        try {
            if (!userIds.isEmpty()) {
                List<Users> userList = getUsersByIds(userIds);
                for (Users user : userList) {
                    userPrefListMap.put(user.getId(), JsonUtil.jsonPrefListToMap(user.getPrefList()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userPrefListMap;
    }

    public List<Long> getAllUserIds(){
        return usersDAO.getAllUserIds();
    }

    public void updatePrefListById(long userId, String newPrefStr){
        usersDAO.updatePrefListById(userId, newPrefStr);
    }

}

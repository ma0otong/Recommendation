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
@SuppressWarnings("unused")
public class UsersManager {

    private final UsersDAO usersDAO;

    @Autowired
    public UsersManager(UsersDAO usersDAO) {
        this.usersDAO = usersDAO;
    }

    public List<Users> getAllUsers(){
        return usersDAO.getAllUsers();
    }

    public Users getUserById(Long userId) {
        return usersDAO.getUserById(userId);
    }

    private List<Users> getUsersByIds(List<Long> userIds) {
        List<Users> list = new ArrayList<>();
        for (Long id : userIds) {
            Users user = usersDAO.getUserById(id);
            if (user != null) {
                list.add(user);
            }
        }
        return list;
    }

    public void updateUserTimeStamp(Date timestamp) {
        usersDAO.updateTimeStamp(timestamp);
    }

    public HashMap<Long, CustomizedHashMap<Integer, CustomizedHashMap<String, Double>>> getUserPrefListMap(Long userId) {
        HashMap<Long, CustomizedHashMap<Integer, CustomizedHashMap<String, Double>>> userPrefListMap = new HashMap<>();
        try {
            Users user = getUserById(userId);
            userPrefListMap.put(user.getId(), JsonUtil.jsonPrefListToMap(user.getPrefList()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userPrefListMap;
    }

    public List<Long> getAllUserIds() {
        return usersDAO.getAllUserIds();
    }

    public void updatePrefListById(long userId, String newPrefStr) {
        usersDAO.updatePrefListById(userId, newPrefStr);
    }

    public void initializePrefList(Users user, int moduleCount){
        // prefList为空则初始化prefList
        if (user.getPrefList() == null || user.getPrefList().isEmpty()) {
            StringBuilder newPrefStr = new StringBuilder("{");
            for (int i = 1; i <= moduleCount; i++) {
                newPrefStr.append("\"").append(i).append("\":").append("{}").append(",");
            }
            newPrefStr = new StringBuilder(newPrefStr.substring(0, newPrefStr.length() - 1) + "}");
            // 更新users preference
            usersDAO.updatePrefListById(user.getId(), newPrefStr.toString());
            user.setPrefList(newPrefStr.toString());
        }
    }

}

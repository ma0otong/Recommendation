package com.personal.recommendation.manager;

import com.personal.recommendation.dao.UsersDAO;
import com.personal.recommendation.model.Users;
import com.personal.recommendation.utils.CustomizedHashMap;
import com.personal.recommendation.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

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

    public List<Users> getAllUsers() {
        return usersDAO.getAllUsers();
    }

    public Users getUserById(Long userId) {
        return usersDAO.getUserById(userId);
    }

    private List<Users> getUsersByIds(List<Long> userIds) {
        return usersDAO.getUserByIds(userIds);
    }

    public void updateUserTimeStamp(Date timestamp) {
        usersDAO.updateTimeStamp(timestamp);
    }

    public HashMap<Long, CustomizedHashMap<String, CustomizedHashMap<String, Double>>> getUserPrefListMap(Long userId) {
        HashMap<Long, CustomizedHashMap<String, CustomizedHashMap<String, Double>>> userPrefListMap = new HashMap<>();
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

    public void updatePrefAndProfileById(long userId, String newPrefStr, String userProfile) {
        usersDAO.updatePrefAndProfileById(userId, newPrefStr, userProfile);
    }

    public void initializePrefList(Users user, Set<String> moduleList) {
        // prefList为空则初始化prefList
        StringBuilder newPrefStr = new StringBuilder("{");
        for (String moduleName : moduleList) {
            newPrefStr.append("\"").append(moduleName).append("\":").append("{}").append(",");
        }
        newPrefStr = new StringBuilder(newPrefStr.substring(0, newPrefStr.length() - 1) + "}");
        // 更新users preference
        usersDAO.updatePrefListById(user.getId(), newPrefStr.toString());
        user.setPrefList(newPrefStr.toString());
    }

}

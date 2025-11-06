package data.repository;

import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.Executors;

import javax.inject.Inject;
import javax.inject.Singleton;

import data.db.UserDao;
import data.model.User;

@Singleton
public class UserRepository {

    private final UserDao userDao;

    @Inject
    public UserRepository(UserDao userDao) {
        this.userDao = userDao;
    }

    public long register(User user) {
        return userDao.insert(user);
    }

    public User findByEmail(String email) {
        return userDao.findByEmail(email);
    }
    public MutableLiveData<User> findById(long id) {
        MutableLiveData<User> user = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            User foundUser = userDao.findById(id);
            user.postValue(foundUser);
        });
        return user;
    }

    public MutableLiveData<User> login(String email, String password) {
        MutableLiveData<User> user = new MutableLiveData<>();
        Executors.newSingleThreadExecutor().execute(() -> {
            User foundUser = userDao.login(email, password);
            if(foundUser != null) {
                user.postValue(foundUser);
            }
        });
        return user;
    }

    public void update(User user) {
        userDao.update(user);
    }

    public void delete(User user) {
        userDao.delete(user);
    }
}

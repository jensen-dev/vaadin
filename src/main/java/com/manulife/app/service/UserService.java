package com.manulife.app.service;

import com.manulife.app.entity.User;
import com.manulife.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(Long id) throws Exception {
        User user = this.getUserById(id);
        if (Objects.nonNull(user)) {
            userRepository.delete(user);
        } else {
            throw new Exception("User with id " + id + " is not found");
        }
    }

    public User updateUser(User updatedUser, Long id) {
        User user = this.getUserById(id);
        if (Objects.nonNull(user)) {
            user.setEmail(updatedUser.getEmail());
            user.setRole(updatedUser.getRole());
            user.setName(updatedUser.getName());
            userRepository.save(user);

            return user;
        } else {
            return null;
        }
    }
}

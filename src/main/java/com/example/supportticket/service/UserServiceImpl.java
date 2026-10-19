package com.example.supportticket.service;

import com.example.supportticket.dto.UserRequest;
import com.example.supportticket.dto.UserResponse;
import com.example.supportticket.entity.User;
import com.example.supportticket.enums.Role;
import com.example.supportticket.exception.ConflictException;
import com.example.supportticket.exception.ResourceNotFoundException;
import com.example.supportticket.iservice.UserService;
import com.example.supportticket.repository.TicketRepository;
import com.example.supportticket.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TicketRepository ticketRepository;

    public UserServiceImpl(UserRepository userRepository, TicketRepository ticketRepository) {
        this.userRepository = userRepository;
        this.ticketRepository = ticketRepository;
    }

    @Override
    public UserResponse createUser(UserRequest request) {
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already exists: " + email);
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(email);
        user.setRole(request.getRole());
        return UserResponse.from(userRepository.save(user));
    }

    @Override
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponse> responses = new ArrayList<>();
        for (User user : users) {
            responses.add(UserResponse.from(user));
        }
        return responses;
    }

    @Override
    public UserResponse getUserById(Long id) {
        return UserResponse.from(findUser(id));
    }

    @Override
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = findUser(id);
        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmailAndIdNot(email, id)) {
            throw new ConflictException("Email already exists: " + email);
        }
        if (user.getRole() == Role.CUSTOMER && request.getRole() == Role.AGENT
                && ticketRepository.existsByCreatedById(id)) {
            throw new ConflictException("Cannot change role to AGENT because this user has tickets");
        }

        user.setName(request.getName());
        user.setEmail(email);
        user.setRole(request.getRole());
        return UserResponse.from(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        User user = findUser(id);
        if (ticketRepository.existsByCreatedById(id)) {
            throw new ConflictException("Cannot delete user with id " + id + " because they still have tickets");
        }
        userRepository.delete(user);
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}

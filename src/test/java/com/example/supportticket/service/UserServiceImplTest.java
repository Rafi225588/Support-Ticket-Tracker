package com.example.supportticket.service;

import com.example.supportticket.dto.UserRequest;
import com.example.supportticket.dto.UserResponse;
import com.example.supportticket.entity.User;
import com.example.supportticket.enums.Role;
import com.example.supportticket.exception.ConflictException;
import com.example.supportticket.exception.ResourceNotFoundException;
import com.example.supportticket.repository.TicketRepository;
import com.example.supportticket.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUser_savesUser_whenEmailIsNew() {
        UserRequest request = userRequest("Rahim Uddin", "rahim@example.com", Role.CUSTOMER);
        when(userRepository.existsByEmail("rahim@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setId(1L);
            return savedUser;
        });

        UserResponse response = userService.createUser(request);

        assertEquals(1L, response.getId());
        assertEquals("Rahim Uddin", response.getName());
        assertEquals("rahim@example.com", response.getEmail());
        assertEquals(Role.CUSTOMER, response.getRole());
    }

    @Test
    void createUser_throwsConflict_whenEmailAlreadyExists() {
        UserRequest request = userRequest("Rahim Uddin", "rahim@example.com", Role.CUSTOMER);
        when(userRepository.existsByEmail("rahim@example.com")).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> userService.createUser(request));

        assertEquals("Email already exists: rahim@example.com", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_treatsEmailWithDifferentCaseAsDuplicate() {
        UserRequest request = userRequest("Rahim Uddin", "  Rahim@Example.COM ", Role.CUSTOMER);
        when(userRepository.existsByEmail("rahim@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.createUser(request));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_throwsConflict_whenEmailBelongsToAnotherUser() {
        User rahim = user(1L, "Rahim Uddin", "rahim@example.com", Role.CUSTOMER);
        UserRequest request = userRequest("Rahim Uddin", "karim@example.com", Role.CUSTOMER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(rahim));
        when(userRepository.existsByEmailAndIdNot("karim@example.com", 1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.updateUser(1L, request));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_throwsConflict_whenCustomerWithTicketsBecomesAgent() {
        User rahim = user(1L, "Rahim Uddin", "rahim@example.com", Role.CUSTOMER);
        UserRequest request = userRequest("Rahim Uddin", "rahim@example.com", Role.AGENT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(rahim));
        when(userRepository.existsByEmailAndIdNot("rahim@example.com", 1L)).thenReturn(false);
        when(ticketRepository.existsByCreatedById(1L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.updateUser(1L, request));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_throwsConflict_whenUserStillHasTickets() {
        User rahim = user(1L, "Rahim Uddin", "rahim@example.com", Role.CUSTOMER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(rahim));
        when(ticketRepository.existsByCreatedById(1L)).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class, () -> userService.deleteUser(1L));

        assertEquals("Cannot delete user with id 1 because they still have tickets", exception.getMessage());
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteUser_deletesUser_whenUserHasNoTickets() {
        User rahim = user(1L, "Rahim Uddin", "rahim@example.com", Role.CUSTOMER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(rahim));
        when(ticketRepository.existsByCreatedById(1L)).thenReturn(false);

        userService.deleteUser(1L);

        verify(userRepository).delete(rahim);
    }

    @Test
    void getUserById_throwsNotFound_whenUserDoesNotExist() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserById(99L));

        assertEquals("User not found with id 99", exception.getMessage());
    }

    private UserRequest userRequest(String name, String email, Role role) {
        UserRequest request = new UserRequest();
        request.setName(name);
        request.setEmail(email);
        request.setRole(role);
        return request;
    }

    private User user(Long id, String name, String email, Role role) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        user.setEmail(email);
        user.setRole(role);
        return user;
    }
}
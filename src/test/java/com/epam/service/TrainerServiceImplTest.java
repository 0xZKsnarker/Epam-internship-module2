package com.epam.service;

import com.epam.dao.TraineeDao;
import com.epam.dao.TrainerDao;
import com.epam.domain.Trainee;
import com.epam.domain.Trainer;
import com.epam.domain.User;
import com.epam.exception.ResourceNotFoundException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

    @Mock private TrainerDao trainerDao;
    @Mock private TraineeDao traineeDao;
    @Mock private MeterRegistry meterRegistry;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private Counter counter;

    @InjectMocks
    private TrainerServiceImpl service;

    private void setupMeterRegistryMock() {
        when(meterRegistry.counter(anyString(), any(String[].class))).thenReturn(counter);
    }

    private static User user(String first, String last, String uname, String pwd, boolean active) {
        User u = new User();
        u.setFirstName(first);
        u.setLastName(last);
        u.setUsername(uname);
        u.setPassword(pwd);
        u.setActive(active);
        return u;
    }

    private static Trainer trainer(long id, String first, String last, String uname, String pwd) {
        Trainer tr = new Trainer();
        tr.setId(id);
        tr.setUser(user(first, last, uname, pwd, true));
        return tr;
    }

    @Test
    @DisplayName("create() should hash password and persist the trainer")
    void create_hashesPasswordAndPersists() {
        setupMeterRegistryMock();
        when(traineeDao.findAll()).thenReturn(Collections.emptyList());
        when(trainerDao.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

        Trainer toSave = trainer(0, "John", "Smith", null, "rawPassword");

        doAnswer(invocation -> {
            Trainer capturedTrainer = invocation.getArgument(0);
            assertEquals("hashedPassword", capturedTrainer.getUser().getPassword());
            return null;
        }).when(trainerDao).create(any(Trainer.class));


        Trainer result = service.create(toSave);

        assertNotEquals("hashedPassword", result.getUser().getPassword());
        verify(passwordEncoder).encode(anyString());
        verify(meterRegistry).counter("gym.users.created", "type", "trainer");
    }

    @Test
    @DisplayName("create() resolves username collision by suffixing")
    void create_usernameCollision() {
        setupMeterRegistryMock();
        when(traineeDao.findAll()).thenReturn(Collections.emptyList());
        when(trainerDao.findByUsername("John.Smith")).thenReturn(Optional.of(new Trainer()));
        when(trainerDao.findByUsername("John.Smith.1")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

        Trainer toSave = trainer(0, "John", "Smith", null, null);
        service.create(toSave);

        assertEquals("John.Smith.1", toSave.getUser().getUsername());
        verify(trainerDao).create(toSave);
    }

    @Test
    @DisplayName("create() throws IllegalStateException when a trainee with the same name exists")
    void create_throwsWhenTraineeWithSameNameExists() {
        User existingUser = new User();
        existingUser.setFirstName("John");
        existingUser.setLastName("Smith");
        Trainee existingTrainee = new Trainee();
        existingTrainee.setUser(existingUser);

        when(traineeDao.findAll()).thenReturn(List.of(existingTrainee));
        Trainer trainerToCreate = trainer(0, "John", "Smith", null, null);

        assertThrows(IllegalStateException.class, () -> service.create(trainerToCreate));
        verify(trainerDao, never()).create(any(Trainer.class));
    }

    @Nested
    class Update {
        @Test
        @DisplayName("update() succeeds when the trainer exists")
        void update_success() {
            setupMeterRegistryMock();
            Trainer stored = trainer(5, "Ann", "Lee", "Ann.Lee", "pwd");
            Trainer modified = trainer(5, "Ann", "Lee", "Ann.Lee", "new");

            when(trainerDao.findById(5L)).thenReturn(Optional.of(stored));
            doNothing().when(trainerDao).update(any(Trainer.class));

            Trainer result = service.update(modified);

            assertSame(modified, result);
            verify(trainerDao).update(modified);
            verify(meterRegistry).counter("gym.users.updated", "type", "trainer");
        }

        @Test
        @DisplayName("update() throws when the trainer is missing")
        void update_missing() {
            when(trainerDao.findById(99L)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                    () -> service.update(trainer(99, "Ghost", "User", "Ghost.User", "pwd")));
            verify(trainerDao, never()).update(any());
        }
    }

    @Test
    @DisplayName("changePassword() updates the trainer's password with a hashed value")
    void changePassword_updatesWithHashedValue() {
        Trainer tr = trainer(4, "P", "Q", "P.Q", "oldHashed");
        when(trainerDao.findByUsername("P.Q")).thenReturn(Optional.of(tr));
        when(passwordEncoder.encode("newPass")).thenReturn("newHashedPass");

        service.changePassword("P.Q", "newPass");

        verify(passwordEncoder).encode("newPass");
        assertEquals("newHashedPass", tr.getUser().getPassword());
        verify(trainerDao).update(tr);
    }

    @Test
    @DisplayName("changePassword() throws when trainer missing")
    void changePassword_missing() {
        when(trainerDao.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.changePassword("ghost", "x"));
    }

    @Test
    @DisplayName("activateTrainer() flips the active flag")
    void activate_flipsFlag() {
        setupMeterRegistryMock();
        Trainer tr = trainer(6, "M", "N", "M.N", "pwd");
        tr.getUser().setActive(false);
        when(trainerDao.findByUsername("M.N")).thenReturn(Optional.of(tr));

        service.activateTrainer("M.N", true);

        assertTrue(tr.getUser().isActive());
        verify(meterRegistry).counter("gym.users.activation.change", "type", "trainer", "status", "true");
    }

    @Test
    void findById_delegates() {
        Trainer tr = trainer(3, "X", "Y", "X.Y", "pwd");
        when(trainerDao.findById(3L)).thenReturn(Optional.of(tr));
        assertSame(tr, service.findById(3L).orElseThrow());
    }

    @Test
    void findAll_delegates() {
        List<Trainer> list = List.of(trainer(2, "A", "B", "A.B", "pwd"));
        when(trainerDao.findAll()).thenReturn(list);
        assertEquals(list, service.findAll());
    }

    @Test
    void findByUsername_delegates() {
        Trainer tr = trainer(8, "C", "D", "C.D", "pwd");
        when(trainerDao.findByUsername("C.D")).thenReturn(Optional.of(tr));
        assertSame(tr, service.findByUsername("C.D").orElseThrow());
    }

    @Test
    @DisplayName("getUnassignedTrainers() delegates to DAO")
    void unassignedTrainers_delegates() {
        List<Trainer> expected = List.of(trainer(1, "Coach", "One", "Coach.One", "pwd"));
        when(trainerDao.findUnassignedTrainers("trainee1")).thenReturn(expected);
        assertEquals(expected, service.getUnassignedTrainers("trainee1"));
    }
}
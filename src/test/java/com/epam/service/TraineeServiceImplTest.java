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
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

    @Mock private TraineeDao traineeDao;
    @Mock private TrainerDao trainerDao;
    @Mock private MeterRegistry meterRegistry;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private Counter counter;

    @InjectMocks
    private TraineeServiceImpl service;

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

    private static Trainee trainee(long id, String first, String last, String uname, String pwd) {
        Trainee t = new Trainee();
        t.setId(id);
        t.setUser(user(first, last, uname, pwd, true));
        return t;
    }

    private static Trainer trainer(long id, String first, String last, String uname) {
        Trainer tr = new Trainer();
        tr.setId(id);
        tr.setUser(user(first, last, uname, "pwd", true));
        return tr;
    }

    @Test
    @DisplayName("create() should hash the password before saving")
    void create_hashesPasswordAndPersists() {

        setupMeterRegistryMock();
        when(trainerDao.findAll()).thenReturn(Collections.emptyList());
        when(traineeDao.findByUsername(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");

        Trainee toSave = trainee(0, "John", "Smith", null, null);

        doAnswer(invocation -> {
            Trainee capturedTrainee = invocation.getArgument(0);
            assertEquals("hashedPassword", capturedTrainee.getUser().getPassword());
            return null;
        }).when(traineeDao).create(any(Trainee.class));

        Trainee result = service.create(toSave);


        assertNotNull(result.getUser().getPassword());
        assertNotEquals("hashedPassword", result.getUser().getPassword());

        verify(passwordEncoder, times(1)).encode(anyString());
        verify(meterRegistry).counter("gym.users.created", "type", "trainee");
    }

    @Test
    @DisplayName("create() resolves username collision by suffixing")
    void create_resolvesUsernameCollision() {
        setupMeterRegistryMock();
        when(trainerDao.findAll()).thenReturn(Collections.emptyList());
        when(traineeDao.findByUsername("John.Smith")).thenReturn(Optional.of(new Trainee()));
        when(traineeDao.findByUsername("John.Smith.1")).thenReturn(Optional.empty());

        Trainee toSave = trainee(0, "John", "Smith", null, null);
        service.create(toSave);

        assertEquals("John.Smith.1", toSave.getUser().getUsername());
        verify(traineeDao).create(toSave);
    }

    @Test
    @DisplayName("create() throws IllegalStateException when a trainer with the same name exists")
    void create_throwsWhenTrainerWithSameNameExists() {
        User existingUser = new User();
        existingUser.setFirstName("John");
        existingUser.setLastName("Smith");
        Trainer existingTrainer = new Trainer();
        existingTrainer.setUser(existingUser);

        when(trainerDao.findAll()).thenReturn(List.of(existingTrainer));
        Trainee traineeToCreate = trainee(0, "John", "Smith", null, null);

        assertThrows(IllegalStateException.class, () -> service.create(traineeToCreate));
        verify(traineeDao, never()).create(any(Trainee.class));
    }

    @Nested
    class Update {
        @Test
        @DisplayName("update() forwards to DAO when entity exists")
        void succeedsWhenFound() {
            setupMeterRegistryMock();
            Trainee existing = trainee(1, "Ann", "Lee", "Ann.Lee", "pwd");
            when(traineeDao.findById(1L)).thenReturn(Optional.of(existing));

            Trainee modified = trainee(1, "Ann", "Lee", "Ann.Lee", "pwd2");
            Trainee out = service.update(modified);

            assertSame(modified, out);
            verify(traineeDao).update(modified);
            verify(meterRegistry).counter("gym.users.updated", "type", "trainee");
        }

        @Test
        @DisplayName("update() throws when entity missing")
        void throwsWhenMissing() {
            when(traineeDao.findById(99L)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                    () -> service.update(trainee(99, "Ghost", "User", "Ghost.User", "x")));
            verify(traineeDao, never()).update(any());
        }
    }

    @Test
    @DisplayName("delete() removes record when present")
    void delete_removesWhenFound() {
        setupMeterRegistryMock();
        when(traineeDao.findById(5L)).thenReturn(Optional.of(trainee(5, "A", "B", "A.B", "pwd")));
        service.delete(5L);
        verify(traineeDao).delete(5L);
        verify(meterRegistry).counter("gym.users.deleted", "type", "trainee");
    }

    @Test
    @DisplayName("delete() throws when record absent")
    void delete_throwsWhenMissing() {
        when(traineeDao.findById(5L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.delete(5L));
        verify(traineeDao, never()).delete(anyLong());
    }

    @Test
    @DisplayName("deleteByUsername() cascades to delete(id)")
    void deleteByUsername_deletesViaId() {
        setupMeterRegistryMock();
        Trainee t = trainee(7, "Z", "Q", "Z.Q", "pwd");
        when(traineeDao.findByUsername("Z.Q")).thenReturn(Optional.of(t));
        when(traineeDao.findById(7L)).thenReturn(Optional.of(t));

        service.deleteByUsername("Z.Q");
        verify(traineeDao).delete(7L);
    }

    @Test
    @DisplayName("deleteByUsername() throws when absent")
    void deleteByUsername_throwsWhenMissing() {
        when(traineeDao.findByUsername("none")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.deleteByUsername("none"));
    }

    @Test
    @DisplayName("changePassword() should hash the new password")
    void changePassword_updatesFieldWithHashedPassword() {
        Trainee t = trainee(4, "P", "Q", "P.Q", "oldHashedPassword");
        when(traineeDao.findByUsername("P.Q")).thenReturn(Optional.of(t));
        when(passwordEncoder.encode("newRawPassword")).thenReturn("newHashedPassword");

        service.changePassword("P.Q", "newRawPassword");

        verify(passwordEncoder).encode("newRawPassword");
        assertEquals("newHashedPassword", t.getUser().getPassword());
        verify(traineeDao).update(t);
    }

    @Test
    @DisplayName("changePassword() throws for missing user")
    void changePassword_missingUser() {
        when(traineeDao.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.changePassword("ghost", "x"));
    }

    @Test
    @DisplayName("activateTrainee() flips active flag")
    void activate_flipsFlag() {
        setupMeterRegistryMock();
        Trainee t = trainee(6, "M", "N", "M.N", "pwd");
        t.getUser().setActive(false);
        when(traineeDao.findByUsername("M.N")).thenReturn(Optional.of(t));

        service.activateTrainee("M.N", true);

        assertTrue(t.getUser().isActive());
        verify(meterRegistry).counter("gym.users.activation.change", "type", "trainee", "status", "true");
    }

    @Nested
    class UpdateTrainers {
        @Test
        @DisplayName("updateTrainers() replaces trainee's trainer set")
        void updatesTrainerList() {
            setupMeterRegistryMock();
            Trainee t = trainee(10, "Stu", "Dent", "Stu.Dent", "pwd");
            when(traineeDao.findByUsername("Stu.Dent")).thenReturn(Optional.of(t));

            Trainer tr1 = trainer(1, "Coach", "One", "Coach.One");
            Trainer tr2 = trainer(2, "Coach", "Two", "Coach.Two");

            when(trainerDao.findByUsername("Coach.One")).thenReturn(Optional.of(tr1));
            when(trainerDao.findByUsername("Coach.Two")).thenReturn(Optional.of(tr2));

            Trainee result = service.updateTrainers("Stu.Dent", List.of("Coach.One", "Coach.Two"));

            assertEquals(Set.of(tr1, tr2), result.getTrainers());
            verify(meterRegistry).counter("gym.trainee.trainers.updated", "trainee", "Stu.Dent");
        }

        @Test
        @DisplayName("updateTrainers() throws when trainer missing")
        void throwsWhenTrainerMissing() {
            Trainee t = trainee(10, "Stu", "Dent", "Stu.Dent", "pwd");
            when(traineeDao.findByUsername("Stu.Dent")).thenReturn(Optional.of(t));
            when(trainerDao.findByUsername("Missing")).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> service.updateTrainers("Stu.Dent", List.of("Missing")));
        }
    }

    @Test
    void findById_delegates() {
        Trainee t = trainee(3, "X", "Y", "X.Y", "pwd");
        when(traineeDao.findById(3L)).thenReturn(Optional.of(t));
        assertSame(t, service.findById(3L).orElseThrow());
    }

    @Test
    void findAll_delegates() {
        List<Trainee> list = List.of(trainee(1, "A", "B", "A.B", "pwd"));
        when(traineeDao.findAll()).thenReturn(list);
        assertEquals(list, service.findAll());
    }

    @Test
    void findByUsername_delegates() {
        Trainee t = trainee(8, "C", "D", "C.D", "pwd");
        when(traineeDao.findByUsername("C.D")).thenReturn(Optional.of(t));
        assertSame(t, service.findByUsername("C.D").orElseThrow());
    }
}
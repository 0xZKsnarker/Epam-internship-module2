package com.epam.service;

import com.epam.dao.TraineeDao;
import com.epam.dao.TrainerDao;
import com.epam.domain.Trainee;
import com.epam.domain.Trainer;
import com.epam.domain.User;
import com.epam.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

    @Mock  private TraineeDao traineeDao;
    @Mock  private TrainerDao trainerDao;

    @InjectMocks
    private TraineeServiceImpl service;


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
    @DisplayName("create() generates unique username & password then persists")
    void create_generatesCredentialsAndPersists() {
        when(traineeDao.usernameExists("John.Smith")).thenReturn(false);

        Trainee toSave = trainee(0, "John", "Smith", null, null);

        Trainee result = service.create(toSave);

        assertEquals("John.Smith", result.getUser().getUsername());
        assertNotNull(result.getUser().getPassword());
        assertEquals(10, result.getUser().getPassword().length());
        verify(traineeDao).create(result);
    }

    @Test
    @DisplayName("create() resolves username collision by suffixing")
    void create_resolvesUsernameCollision() {
        when(traineeDao.usernameExists("John.Smith")).thenReturn(true);
        when(traineeDao.usernameExists("John.Smith.1")).thenReturn(false);

        Trainee toSave = trainee(0, "John", "Smith", null, null);
        service.create(toSave);

        assertEquals("John.Smith.1", toSave.getUser().getUsername());
        verify(traineeDao).create(toSave);
    }


    @Nested
    class Update {

        @Test
        @DisplayName("update() forwards to DAO when entity exists")
        void succeedsWhenFound() {
            Trainee existing = trainee(1, "Ann", "Lee", "Ann.Lee", "pwd");
            when(traineeDao.findById(1L)).thenReturn(Optional.of(existing));

            Trainee modified = trainee(1, "Ann", "Lee", "Ann.Lee", "pwd2");
            Trainee out = service.update(modified);

            assertSame(modified, out);
            verify(traineeDao).update(modified);
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
        when(traineeDao.findById(5L)).thenReturn(Optional.of(trainee(5, "A", "B", "A.B", "pwd")));
        service.delete(5L);
        verify(traineeDao).delete(5L);
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
        assertThrows(ResourceNotFoundException.class,
                () -> service.deleteByUsername("none"));
    }


    @Nested
    class CheckCredentials {

        @Test
        @DisplayName("returns true when username & password match")
        void credentialsMatch() {
            Trainee t = trainee(3, "A", "B", "A.B", "secret");
            when(traineeDao.findByUsername("A.B")).thenReturn(Optional.of(t));
            assertTrue(service.checkCredentials("A.B", "secret"));
        }

        @Test
        @DisplayName("returns false on wrong password or missing user")
        void credentialsDontMatch() {
            Trainee t = trainee(3, "A", "B", "A.B", "secret");
            when(traineeDao.findByUsername("A.B")).thenReturn(Optional.of(t));
            assertFalse(service.checkCredentials("A.B", "oops"));
            when(traineeDao.findByUsername("missing")).thenReturn(Optional.empty());
            assertFalse(service.checkCredentials("missing", "any"));
        }
    }


    @Test
    @DisplayName("changePassword() updates the user’s password")
    void changePassword_updatesField() {
        Trainee t = trainee(4, "P", "Q", "P.Q", "old");
        when(traineeDao.findByUsername("P.Q")).thenReturn(Optional.of(t));

        service.changePassword("P.Q", "newPass");

        assertEquals("newPass", t.getUser().getPassword());
    }

    @Test
    @DisplayName("changePassword() throws for missing user")
    void changePassword_missingUser() {
        when(traineeDao.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.changePassword("ghost", "x"));
    }


    @Test
    @DisplayName("activateTrainee() flips active flag")
    void activate_flipsFlag() {
        Trainee t = trainee(6, "M", "N", "M.N", "pwd");
        t.getUser().setActive(false);
        when(traineeDao.findByUsername("M.N")).thenReturn(Optional.of(t));

        service.activateTrainee("M.N", true);

        assertTrue(t.getUser().isActive());
    }


    @Nested
    class UpdateTrainers {

        @Test
        @DisplayName("updateTrainers() replaces trainee’s trainer set")
        void updatesTrainerList() {
            Trainee t = trainee(10, "Stu", "Dent", "Stu.Dent", "pwd");
            when(traineeDao.findByUsername("Stu.Dent")).thenReturn(Optional.of(t));

            Trainer tr1 = trainer(1, "Coach", "One", "Coach.One");
            Trainer tr2 = trainer(2, "Coach", "Two", "Coach.Two");

            when(trainerDao.findByUsername("Coach.One")).thenReturn(Optional.of(tr1));
            when(trainerDao.findByUsername("Coach.Two")).thenReturn(Optional.of(tr2));

            Trainee result = service.updateTrainers("Stu.Dent",
                    List.of("Coach.One", "Coach.Two"));

            assertEquals(Set.of(tr1, tr2), result.getTrainers());
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

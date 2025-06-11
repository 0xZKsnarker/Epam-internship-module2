package com.epam.service;

import com.epam.dao.TrainerDao;
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
class TrainerServiceImplTest {

    @Mock   private TrainerDao trainerDao;
    @InjectMocks
    private TrainerServiceImpl service;


    private static User user(String first, String last,
                             String uname, String pwd, boolean active) {
        User u = new User();
        u.setFirstName(first);
        u.setLastName(last);
        u.setUsername(uname);
        u.setPassword(pwd);
        u.setActive(active);
        return u;
    }

    private static Trainer trainer(long id, String first, String last,
                                   String uname, String pwd) {
        Trainer tr = new Trainer();
        tr.setId(id);
        tr.setUser(user(first, last, uname, pwd, true));
        return tr;
    }


    @Test
    @DisplayName("create() generates credentials and persists the trainer")
    void create_generatesCredentials() {
        when(trainerDao.usernameExists("John.Smith")).thenReturn(false);

        Trainer toSave = trainer(0, "John", "Smith", null, null);
        Trainer result  = service.create(toSave);

        assertEquals("John.Smith", result.getUser().getUsername());
        assertNotNull(result.getUser().getPassword());
        assertEquals(10, result.getUser().getPassword().length());
        verify(trainerDao).create(result);
    }

    @Test
    @DisplayName("create() resolves username collision by suffixing")
    void create_usernameCollision() {
        when(trainerDao.usernameExists("John.Smith")).thenReturn(true);
        when(trainerDao.usernameExists("John.Smith.1")).thenReturn(false);

        Trainer toSave = trainer(0, "John", "Smith", null, null);
        service.create(toSave);

        assertEquals("John.Smith.1", toSave.getUser().getUsername());
        verify(trainerDao).create(toSave);
    }


    @Nested
    class Update {

        @Test
        @DisplayName("update() succeeds when the trainer exists")
        void update_success() {
            Trainer stored = trainer(5, "Ann", "Lee", "Ann.Lee", "pwd");
            when(trainerDao.findById(5L)).thenReturn(Optional.of(stored));

            Trainer modified = trainer(5, "Ann", "Lee", "Ann.Lee", "new");
            assertSame(modified, service.update(modified));
            verify(trainerDao).update(modified);
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


    @Nested
    class CheckCredentials {

        @Test
        @DisplayName("checkCredentials() returns true for matching user & password")
        void credentials_match() {
            Trainer tr = trainer(3, "A", "B", "A.B", "secret");
            when(trainerDao.findByUsername("A.B")).thenReturn(Optional.of(tr));
            assertTrue(service.checkCredentials("A.B", "secret"));
        }

        @Test
        @DisplayName("checkCredentials() returns false on wrong password or missing user")
        void credentials_fail() {
            Trainer tr = trainer(3, "A", "B", "A.B", "secret");
            when(trainerDao.findByUsername("A.B")).thenReturn(Optional.of(tr));
            assertFalse(service.checkCredentials("A.B", "oops"));
            when(trainerDao.findByUsername("missing")).thenReturn(Optional.empty());
            assertFalse(service.checkCredentials("missing", "x"));
        }
    }


    @Test
    @DisplayName("changePassword() updates the trainer’s password")
    void changePassword_updates() {
        Trainer tr = trainer(4, "P", "Q", "P.Q", "old");
        when(trainerDao.findByUsername("P.Q")).thenReturn(Optional.of(tr));

        service.changePassword("P.Q", "newPass");

        assertEquals("newPass", tr.getUser().getPassword());
    }

    @Test
    @DisplayName("changePassword() throws when trainer missing")
    void changePassword_missing() {
        when(trainerDao.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.changePassword("ghost", "x"));
    }


    @Test
    @DisplayName("activateTrainer() flips the active flag")
    void activate_flipsFlag() {
        Trainer tr = trainer(6, "M", "N", "M.N", "pwd");
        tr.getUser().setActive(false);
        when(trainerDao.findByUsername("M.N")).thenReturn(Optional.of(tr));

        service.activateTrainer("M.N", true);

        assertTrue(tr.getUser().isActive());
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

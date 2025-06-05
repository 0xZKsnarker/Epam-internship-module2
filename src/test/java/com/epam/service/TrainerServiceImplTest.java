package com.epam.service;

import com.epam.dao.TrainerDao;
import com.epam.domain.Trainer;
import com.epam.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

    @Mock
    private TrainerDao trainerDao;

    @InjectMocks
    private TrainerServiceImpl service;

    // Helper to build a Trainer
    private Trainer trainer(long id, String first, String last, String uname) {
        return new Trainer(first, last, uname, null, true, "fitness", id);
    }

    // create() should generate username/password and persist the trainer
    @Test
    void createGeneratesCredentialsAndPersists() {
        when(trainerDao.usernameExists("John.Smith")).thenReturn(false);
        Trainer t = trainer(0, "John", "Smith", null);

        Trainer out = service.create(t);

        assertEquals("John.Smith", out.getUsername());
        assertNotNull(out.getPassword());
        assertEquals(10, out.getPassword().length());
        verify(trainerDao).create(out);
    }

    // create() should suffix username when a collision exists
    @Test
    void createResolvesUsernameCollision() {
        when(trainerDao.usernameExists("John.Smith")).thenReturn(true);
        when(trainerDao.usernameExists("John.Smith.1")).thenReturn(false);

        Trainer t = trainer(0, "John", "Smith", null);
        service.create(t);

        assertEquals("John.Smith.1", t.getUsername());
        verify(trainerDao).create(t); // Also verify it's created
    }

    // update() should succeed when the record exists
    @Test
    void updateSucceedsWhenPresent() {
        Trainer stored = trainer(5, "Ann", "Lee", "Ann.Lee");
        when(trainerDao.findById(5L)).thenReturn(Optional.of(stored));
        Trainer modified = trainer(5, "Ann", "Lee", "Ann.Lee");

        Trainer result = service.update(modified);

        assertSame(modified, result);
        verify(trainerDao).update(modified);
    }

    // update() should throw when the record is missing
    @Test
    void updateThrowsWhenMissing() {
        when(trainerDao.findById(99L)).thenReturn(Optional.empty());
        Trainer ghost = trainer(99, "Ghost", "User", "Ghost.User");

        assertThrows(ResourceNotFoundException.class, () -> service.update(ghost));
        verify(trainerDao, never()).update(any());
    }

    // findById() should delegate to the DAO
    @Test
    void findByIdDelegates() {
        Trainer tr = trainer(3, "X", "Y", "X.Y");
        when(trainerDao.findById(3L)).thenReturn(Optional.of(tr));

        Optional<Trainer> res = service.findById(3L);

        assertTrue(res.isPresent());
        assertSame(tr, res.get());
    }

    // findAll() should delegate to the DAO
    @Test
    void findAllDelegates() {
        List<Trainer> list = List.of(trainer(2, "A", "B", "A.B"));
        when(trainerDao.findAll()).thenReturn(list);

        List<Trainer> out = service.findAll();

        assertEquals(list, out);
    }
}
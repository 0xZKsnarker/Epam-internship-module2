package com.epam.service;

import com.epam.dao.TraineeDao;
import com.epam.domain.Trainee;
import com.epam.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TraineeServiceImplTest {

    @Mock
    private TraineeDao traineeDao;

    @InjectMocks
    private TraineeServiceImpl service;

    private Trainee sample(long id, String first, String last, String uname) {
        return new Trainee(first, last, uname, null, true,
                LocalDate.of(1990, 1, 1), "Main", id);
    }

    // create() should assign username/password and call dao.create()
    @Test
    void createGeneratesCredentialsAndPersists() {
        when(traineeDao.findAll()).thenReturn(Collections.emptyList());
        Trainee t = sample(0, "John", "Smith", null);

        Trainee result = service.create(t);

        assertNotNull(result.getUsername()); // username set
        assertEquals(10, result.getPassword().length()); // 10-char pwd
        verify(traineeDao).create(result);// persisted
    }

    // create() should suffix username when duplicate exists
    @Test
    void createResolvesUsernameCollision() {
        Trainee existing = sample(1, "John", "Smith", "John.Smith");
        when(traineeDao.findAll()).thenReturn(List.of(existing));
        Trainee t = sample(0, "John", "Smith", null);

        service.create(t);

        assertEquals("John.Smith.1", t.getUsername());
    }

    // update() should forward to dao.update when record exists
    @Test
    void updateSucceedsWhenPresent() {
        Trainee stored = sample(2, "Ann", "Lee", "Ann.Lee");
        when(traineeDao.findById(2L)).thenReturn(Optional.of(stored));
        Trainee modified = sample(2, "Ann", "Lee", "Ann.Lee");

        Trainee out = service.update(modified);

        assertSame(modified, out);
        verify(traineeDao).update(modified);
    }

    // update() should throw when record absent
    @Test
    void updateThrowsWhenMissing() {
        when(traineeDao.findById(99L)).thenReturn(Optional.empty());
        Trainee ghost = sample(99, "Ghost", "User", "Ghost.User");

        assertThrows(ResourceNotFoundException.class, () -> service.update(ghost));
        verify(traineeDao, never()).update(any());
    }

    // delete() should call dao.delete when record exists
    @Test
    void deleteRemovesWhenPresent() {
        when(traineeDao.findById(5L)).thenReturn(Optional.of(sample(5, "A", "B", "A.B")));
        service.delete(5L);
        verify(traineeDao).delete(5L);
    }

    // delete() should throw when record absent
    @Test
    void deleteThrowsWhenMissing() {
        when(traineeDao.findById(5L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.delete(5L));
        verify(traineeDao, never()).delete(anyLong());
    }

    // findById() delegates to dao
    @Test
    void findByIdDelegates() {
        Trainee t = sample(3, "X", "Y", "X.Y");
        when(traineeDao.findById(3L)).thenReturn(Optional.of(t));
        Optional<Trainee> res = service.findById(3L);
        assertTrue(res.isPresent());
        assertSame(t, res.get());
    }

    // findAll() delegates to dao
    @Test
    void findAllDelegates() {
        List<Trainee> list = List.of(sample(1, "A", "B", "A.B"));
        when(traineeDao.findAll()).thenReturn(list);
        assertEquals(list, service.findAll());
    }
}

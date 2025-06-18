package com.epam.service;

import com.epam.dao.TrainingTypeDao;
import com.epam.domain.TrainingType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingTypeServiceImplTest {

    @Mock
    private TrainingTypeDao trainingTypeDao;

    @InjectMocks
    private TrainingTypeServiceImpl service;

    @Test
    @DisplayName("findById should delegate the call to the DAO")
    void findById_delegatesToDao() {
        // Arrange
        TrainingType trainingType = new TrainingType();
        trainingType.setId(1L);
        trainingType.setName("Cardio");
        when(trainingTypeDao.findById(1L)).thenReturn(Optional.of(trainingType));

        // Act
        Optional<TrainingType> result = service.findById(1L);

        // Assert
        assertSame(trainingType, result.orElse(null), "The returned training type should be the same as the one from the DAO");
        verify(trainingTypeDao).findById(1L); // Verify that the DAO method was called
    }

    @Test
    @DisplayName("findAll should delegate the call to the DAO")
    void findAll_delegatesToDao() {
        // Arrange
        TrainingType tt1 = new TrainingType();
        TrainingType tt2 = new TrainingType();
        List<TrainingType> expectedList = List.of(tt1, tt2);
        when(trainingTypeDao.findAll()).thenReturn(expectedList);

        // Act
        List<TrainingType> result = service.findAll();

        // Assert
        assertEquals(expectedList, result, "The returned list should be the same as the one from the DAO");
        verify(trainingTypeDao).findAll(); // Verify that the DAO method was called
    }
}
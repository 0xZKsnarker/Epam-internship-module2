package com.epam;

import com.epam.config.AppConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.time.LocalDate; // Import if you want to create new Trainee/Training objects

public class Main {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        GymFacade gymFacade = context.getBean(GymFacade.class);

        System.out.println("Gym CRM Application Started!");

        // --- Manual Testing Section ---

        // 1. Verify initial data loading (Read operations)
        System.out.println("\n--- Initial Data Loaded ---");
        System.out.println("Trainers in system:");
        gymFacade.trainers().findAll().forEach(System.out::println);
        System.out.println("Trainees in system:");
        gymFacade.trainees().findAll().forEach(System.out::println);
        System.out.println("Trainings in system:");
        gymFacade.trainings().findAll().forEach(System.out::println);


        // 2. Test Trainee Service (Create, Update, Delete, Select)
        System.out.println("\n--- Testing Trainee Service ---");

        // Create a new Trainee
        Trainee newTrainee = new Trainee("David", "Lee", null, null, true, LocalDate.of(1993, 7, 1), "404 Cedar Rd", 4L); // userId can be 0L if auto-generated, or specify if manual
        System.out.println("Creating new trainee: " + newTrainee.getFirstName() + " " + newTrainee.getLastName());
        Trainee createdTrainee = gymFacade.trainees().create(newTrainee);
        System.out.println("Created trainee: " + createdTrainee);

        // Find the created Trainee by ID
        System.out.println("Finding trainee with ID 4: " + gymFacade.trainees().findById(4L).orElse(null));

        // Update the created Trainee
        createdTrainee.setAddress("505 New St.");
        createdTrainee.setActive(false);
        System.out.println("Updating trainee ID 4 address and active status...");
        Trainee updatedTrainee = gymFacade.trainees().update(createdTrainee);
        System.out.println("Updated trainee: " + updatedTrainee);

        // Delete the created Trainee
        System.out.println("Deleting trainee with ID 4...");
        gymFacade.trainees().delete(4L);
        System.out.println("Finding trainee with ID 4 after deletion: " + gymFacade.trainees().findById(4L).orElse(null));

        // Show all trainees after operations
        System.out.println("All trainees after operations:");
        gymFacade.trainees().findAll().forEach(System.out::println);


        // 3. Test Trainer Service (Create, Update, Select)
        System.out.println("\n--- Testing Trainer Service ---");

        // Create a new Trainer
        Trainer newTrainer = new Trainer("Emily", "Clark", null, null, true, "Pilates", 4L); // userId can be 0L if auto-generated
        System.out.println("Creating new trainer: " + newTrainer.getFirstName() + " " + newTrainer.getLastName());
        Trainer createdTrainer = gymFacade.trainers().create(newTrainer);
        System.out.println("Created trainer: " + createdTrainer);

        // Update the created Trainer
        createdTrainer.setSpecialization("CrossFit");
        System.out.println("Updating trainer ID 4 specialization...");
        Trainer updatedTrainer = gymFacade.trainers().update(createdTrainer);
        System.out.println("Updated trainer: " + updatedTrainer);

        // Show all trainers after operations
        System.out.println("All trainers after operations:");
        gymFacade.trainers().findAll().forEach(System.out::println);


        // 4. Test Training Service (Create, Select)
        System.out.println("\n--- Testing Training Service ---");

        // Create a new Training
        // Assuming Trainee ID 1 and Trainer ID 1 exist from initial load
        Training newTraining = new Training(1L, 1L, "Evening Run", "Cardio", LocalDate.of(2024, 6, 8), 45);
        System.out.println("Creating new training: " + newTraining.getTrainingName());
        Training createdTraining = gymFacade.trainings().create(newTraining);
        System.out.println("Created training: " + createdTraining);

        // Show all trainings after operations
        System.out.println("All trainings after operations:");
        gymFacade.trainings().findAll().forEach(System.out::println);

        // --- End Manual Testing Section ---

        System.out.println("\nGym CRM Application Finished.");
    }
}
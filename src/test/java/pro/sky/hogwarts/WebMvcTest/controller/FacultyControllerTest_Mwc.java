package pro.sky.hogwarts.WebMvcTest.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.internal.verification.Times;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import pro.sky.hogwarts.controller.FacultyController;
import pro.sky.hogwarts.entity.Faculty;
import pro.sky.hogwarts.entity.Student;
import pro.sky.hogwarts.repository.FacultyRepository;
import pro.sky.hogwarts.repository.StudentRepository;
import pro.sky.hogwarts.service.FacultyService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FacultyController.class)
public class FacultyControllerTest_Mwc {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private FacultyRepository facultyRepository;
    @MockBean
    private StudentRepository studentRepository;
    @SpyBean
    private FacultyService facultyService;
    @Autowired
    private ObjectMapper objectMapper;
    private final Faker faker = new Faker();


    /***   CREATE  - OK ***/
    @Test
    public void createTest() throws Exception {
        //Faculty facultyIn = new Faculty(1L, "f-1", "green");
        Faculty facultyIn = generateFaculty();
        facultyRepository.save(facultyIn);

        Faculty faculty = new Faculty();
        faculty.setId(1L);
        faculty.setName(facultyIn.getName());
        faculty.setColor(facultyIn.getColor());

        when(facultyRepository.save(any())).thenReturn(faculty);

        mockMvc.perform(MockMvcRequestBuilders.post("/faculties")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(facultyIn)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    Faculty facultyDtoOut = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            Faculty.class
                    );
                    assertThat(facultyDtoOut).isNotNull();
                    assertThat(facultyDtoOut.getId()).isEqualTo(1L);
                    assertThat(facultyDtoOut.getColor()).isEqualTo(facultyIn.getColor());
                    assertThat(facultyDtoOut.getName()).isEqualTo(facultyIn.getName());
                });
        verify(facultyRepository, new Times(1)).save(any());
    }

    /***   UPDATE  - OK ***/
    @Test
    public void updateTest() throws Exception {
        Faculty facultyIn = new Faculty(1L, "f-1", "green");
        Faculty oldFaculty = generateFaculty();

        when(facultyRepository.findById(eq(1L))).thenReturn(Optional.of(oldFaculty));
        oldFaculty.setId(1L);
        oldFaculty.setName(facultyIn.getName());
        oldFaculty.setColor(facultyIn.getColor());

        when(facultyRepository.save(any())).thenReturn(oldFaculty);
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/faculties/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(facultyIn)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    Faculty facultyOut = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            Faculty.class
                    );

                    assertThat(facultyOut).isNotNull();
                    assertThat(facultyOut.getId()).isEqualTo(1L);
                    assertThat(facultyOut.getColor()).isEqualTo(facultyIn.getColor());
                    assertThat(facultyOut.getName()).isEqualTo(facultyIn.getName());
                });
        verify(facultyRepository, Mockito.times(1)).save(any());
        Mockito.reset(facultyRepository);

        //not found checking
        when(facultyRepository.findById(eq(2L))).thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders
                        .put("/faculties/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(facultyIn)))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(result -> {
                    String responseString = result.getResponse().getContentAsString();
                    assertThat(responseString).isNotNull();
                    assertThat(responseString).isEqualTo("Факультет с id = 2 не найден!");

                });
        verify(facultyRepository, never()).save(any());
    }

    /*** GET_BY_ID  - OK  ***/
    @Test
    public void getFacultyByIdTest() throws Exception {
        Faculty faculty = generateFaculty();
        faculty.setId(1L);

        when(facultyRepository.findById(faculty.getId())).thenReturn(Optional.of(faculty));

        mockMvc.perform(get("/faculties/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(faculty.getId()))
                .andExpect(jsonPath("$.name").value(faculty.getName()))
                .andExpect(jsonPath("$.color").value(faculty.getColor()));

        // not found checking
        when(facultyRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/faculties/100").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    /***   DELETE  -  OK ***/
    @Test
    public void deleteTest() throws Exception {
        Faculty faculty = generateFaculty();

        when(facultyRepository.findById(eq(faculty.getId()))).thenReturn(Optional.of(faculty));

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/faculties/" + faculty.getId())
                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    Faculty faculty0ut = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            Faculty.class
                    );
                    assertThat(faculty0ut).isNotNull();
                    assertThat(faculty0ut.getId()).isEqualTo(1L);
                    assertThat(faculty0ut.getColor()).isEqualTo(faculty.getColor());
                    assertThat(faculty0ut.getName()).isEqualTo(faculty.getName());
                });
        verify(facultyRepository, times(1)).delete(any());
        Mockito.reset(facultyRepository);

        // not found checking
        when(facultyRepository.findById(eq(2L))).thenReturn(Optional.empty());

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/faculties/2")
                ).andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(result -> {
                    String responseString = result.getResponse().getContentAsString();
                    assertThat(responseString).isNotNull();
                    assertThat(responseString).isEqualTo("Факультет с id = 2 не найден!");
                });
        verify(facultyRepository, never()).delete(any());
    }

    /***   FIND_ALL_OR_BY_COLOR  -  OK ***/
    @Test
    public void findAllFacultyOrByColorTest() throws Exception {
        Faculty faculty1 = generateFaculty();
        Faculty faculty2 = generateFaculty();
        Faculty faculty3 = generateFaculty();
        List<Faculty> facultiesExpected = List.of(faculty1, faculty2, faculty3);
        List<Faculty> expectedResult = facultiesExpected.stream()
                .toList();
        when(facultyRepository.findAll()).thenReturn(facultiesExpected);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/faculties"))
                .andExpect(MockMvcResultMatchers.status().isOk())

                .andExpect(result -> {
                    List<Faculty> facultiesOut = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            new TypeReference<>() {
                            }
                    );

                    assertThat(facultiesOut).isNotNull().isNotEmpty();

                    Stream.iterate(0, index -> index + 1)
                            .limit(facultiesOut.size())
                            .forEach(index -> {
                                Faculty faculty0ut = facultiesOut.get(index);
                                Faculty expected = expectedResult.get(index);
                                assertThat(faculty0ut.getId()).isEqualTo(expected.getId());
                                assertThat(faculty0ut.getName()).isEqualTo(expected.getName());
                                assertThat(faculty0ut.getColor()).isEqualTo(expected.getColor());
                            });
                });

        String color = facultiesExpected.get(0).getColor();
        facultiesExpected = facultiesExpected.stream()
                .filter(faculty -> faculty.getColor().equals(color))
                .collect(Collectors.toList());
        List<Faculty> facultiesExpected2 = facultiesExpected.stream()
                .filter(faculty -> faculty.getColor().equals(color))
                .toList();

        when(facultyRepository.findAllByColor(eq(color))).thenReturn(facultiesExpected);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/faculties?color={color}", color)

                ).andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    List<Faculty> facultyDto0uts = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            new TypeReference<>() {
                            }
                    );

                    assertThat(facultyDto0uts).isNotNull().isNotEmpty();
                    Stream.iterate(0, index -> index + 1)
                            .limit(facultyDto0uts.size())
                            .forEach(index -> {
                                Faculty facultyDtoOut = facultyDto0uts.get(index);
                                Faculty expected = expectedResult.get(index);
                                assertThat(facultyDtoOut.getId()).isEqualTo(expected.getId());
                                assertThat(facultyDtoOut.getColor()).isEqualTo(expected.getColor());
                                assertThat(facultyDtoOut.getName()).isEqualTo(expected.getName());
                            });
                });
        when(facultyRepository.findAll()).thenReturn(facultiesExpected);
    }

    /*** FIND_BY_COLOR_OR_NAME  - OK  ***/
    @Test
    public void findFacultyByColorOrName() throws Exception {
        Faculty faculty1 = new Faculty(1L, "fac-1", "green");
        facultyRepository.save(faculty1);
        Faculty faculty2 = new Faculty(1L, "fac-2", "red");
        facultyRepository.save(faculty2);
        Faculty faculty3 = new Faculty(1L, "fac-3", "yellow");
        facultyRepository.save(faculty3);

        List<Faculty> facultiesExpected = List.of(faculty1, faculty2, faculty3);
        when(facultyRepository
                .findByColorContainingIgnoreCaseOrNameContainingIgnoreCase("fac", "fac"))
                .thenReturn(facultiesExpected);
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/faculties/filter?colorOrName=fac")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        List<Faculty> facultiesRed = List.of(faculty2);
        when(facultyRepository
                .findByColorContainingIgnoreCaseOrNameContainingIgnoreCase("red", "red"))
                .thenReturn(facultiesRed);
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/faculties/filter?colorOrName=red")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    public Faculty generateFaculty() {
        Faculty generatedFaculty = new Faculty();
        generatedFaculty.setId(1L);
        generatedFaculty.setName(faker.name().name());
        generatedFaculty.setColor(faker.color().name());
        return generatedFaculty;
    }
//Commit-3
}

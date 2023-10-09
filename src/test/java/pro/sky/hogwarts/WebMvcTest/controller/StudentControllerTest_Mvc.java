package pro.sky.hogwarts.WebMvcTest.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import pro.sky.hogwarts.controller.StudentController;
import pro.sky.hogwarts.entity.*;
import pro.sky.hogwarts.repository.*;
import pro.sky.hogwarts.service.*;

import java.util.*;
import java.util.stream.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc // аннотация подтягивает зависимости
public class StudentControllerTest_Mvc {
    /*** MockMvc - для тестирования контроллеров и позволяет их тестировать без запуска HTTP-сервера ***/
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * MockBean - для не внесения изменений в БД / помещает реализацию репозитория в application context /
     * позволяет задать нужное поведение объекту/ объекты не имеют реализации методов, хоть и являются экземплярами класса
     * с существующей логикой
     **/
    @MockBean
    private StudentRepository studentRepository;
    @MockBean
    private FacultyRepository facultyRepository;

    /**
     * SpyBean - помещает реализацию репозитория в application context /
     * отличие от MockBean: если не переопределить поведение метода объекта - будет запущен реальный код.
     **/
    @SpyBean
    private StudentService studentService;
    @SpyBean
    FacultyService facultyService;

    /*** InjectMocks говорит о том, что контроллер будет использовать объекты, помеченные аннотациями @SpyBean и @MockBean ***/
    @InjectMocks
    StudentController studentController;

    @AfterEach
    public void clean() {
        studentRepository.deleteAll();
    }


    /**   CREATE - РАБОТАЕТ **/
    @Test
    public void createStudentTest() throws Exception {
        final long id = 7;
        final String name = "Седьмой";
        final Integer age = 7;

        // данные, к-е будем отправлять
        JSONObject studentObject = new JSONObject();
        studentObject.put("id", id);
        studentObject.put("name", name);
        studentObject.put("age", age);

        // данные, к-е должны вернуться
        Student student = new Student();
        student.setId(id);
        student.setName(name);
        student.setAge(age);

        /* when (...save).thenReturn(..) // when (...findById).thenReturn(..) - заменяем функциональность методов .save
        и .findById на те, к-е хотим здесь применять */
        /* когда будем вызывать метод .save для репозитория и передадим в нее любой объект класса Student,
        тогда мы вернем тот самый объект, к-й создавали (Student student = new Student()): */

        when(studentRepository.save(any(Student.class))).thenReturn(student);
        when(studentRepository.findById(any(Long.class))).thenReturn(Optional.of(student)); //новый Optional от студента, к-го ищем

        mockMvc.perform(MockMvcRequestBuilders //создание запроса
                        .post("/students") // post-запрос на URL "/students" (порт не указываем)
                        .content(studentObject.toString()) // тело запроса - наш json-объект в виде строки
                        .contentType(MediaType.APPLICATION_JSON) // тип контента в заголовке
                        .accept(MediaType.APPLICATION_JSON) // в ответ также ожидаем файл json
                ) // метод .perform выполняет запрос

                .andExpect(status().isOk()) // ожидаем в ответе статус 200 (ОК)
                .andExpect(jsonPath("$.id").value(id)) // ищем в ответе поле id, и его value должно быть как наше id
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.age").value(age));

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/students/" + id)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id)) // ищем в ответе поле id, и его value должно быть как наше id
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.age").value(age));
    }


    /**   UPDATE  - РАБОТАЕТ  ***/
    @Test
    public void updateTest() throws Exception {

        Student oldStudent = new Student(1L, "Perviy", 1);
        Student studentIn = new Student(1L, "First", 1);

        when(studentRepository.findById(eq(1L))).thenReturn(Optional.of(oldStudent));

        oldStudent.setName(studentIn.getName());
        oldStudent.setAge(studentIn.getAge());

        when(studentRepository.save(any())).thenReturn(oldStudent);

        mockMvc.perform(MockMvcRequestBuilders
                        .put("/students/" + oldStudent.getId())
                        .content(objectMapper.writeValueAsString(studentIn))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))

                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    Student studentOut = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            Student.class
                    );
                    assertThat(studentOut).isNotNull();
                    assertThat(studentOut.getId()).isEqualTo(1L);
                    assertThat(studentOut.getAge()).isEqualTo(studentIn.getAge());
                    assertThat(studentOut.getName()).isEqualTo(studentIn.getName());
                });
        Mockito.reset(studentRepository);

        //not found checking
        when(studentRepository.findById(eq(2L))).thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.put("/students/2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(studentIn)))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(result -> {
                    String responseString = result.getResponse().getContentAsString();
                    assertThat(responseString).isNotNull();
                    assertThat(responseString).isEqualTo("Студент с id = 2 не найден!");

                });
        verify(studentRepository, never()).save(any());
    }


    /**   DELETE  - РАБОТАЕТ  ***/
    @Test
    public void deleteTest() throws Exception {
        long id = 1L;
        String name = "Первый";
        int age = 1;
        Student student = new Student();
        student.setId(id);
        student.setName(name);
        student.setAge(age);

        when(studentRepository.findById(eq(1L))).thenReturn(Optional.of(student));
        mockMvc.perform(delete("/students/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.age").value(age));

        assertThat(student).isNotNull();
        assertThat(student.getId()).isEqualTo(1L);
        assertThat(student.getAge()).isEqualTo(student.getAge());
        assertThat(student.getName()).isEqualTo(student.getName());

        verify(studentRepository, times(1)).delete(any());
        Mockito.reset(studentRepository);

        // not found checking
        when(studentRepository.findById(eq(2L))).
                thenReturn(Optional.empty());
        mockMvc.perform(MockMvcRequestBuilders.delete("/students/2"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(result -> {
                    String responseString = result.getResponse().getContentAsString();
                    assertThat(responseString).isNotNull();
                    assertThat(responseString).isEqualTo("Студент с id = 2 не найден!");
                });
        verify(studentRepository, never()).delete(any());
    }

    /**   GET_BY_ID  - РАБОТАЕТ ***/
    @Test
    public void getStudentByIdTest() throws Exception {
        long id = 1L;
        String name = "Первый";
        int age = 1;
        Student student = new Student();
        student.setId(id);
        student.setName(name);
        student.setAge(age);

        when(studentRepository.findById(id)).thenReturn(Optional.of(student));

        mockMvc.perform(get("/students/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.age").value(age));

        // not found checking
        when(studentRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/students/100").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    /**   GET-ALL  - РАБОТАЕТ ***/
    @Test
    public void findAllStudentsTest() throws Exception {

        Student st1 = new Student(1L, "First", 1);
        Student st2 = new Student(2L, "Second", 2);
        Student st3 = new Student(7L, "Third", 7);

        List<Student> studentsExpected = List.of(st1, st2, st3);

        when(studentRepository.findAll()).thenReturn(studentsExpected);

        mockMvc.perform(MockMvcRequestBuilders
                        .get("/students"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(result -> {
                    List<Student> studentsOut = objectMapper.readValue(
                            result.getResponse().getContentAsString(),
                            new TypeReference<>() {
                            }
                    );
                    assertThat(studentsOut)
                            .isNotNull()
                            .isNotEmpty();
                    Stream.iterate(0, index -> index + 1)
                            .limit(studentsOut.size())
                            .forEach(index -> {
                                Student student0ut = studentsOut.get(index);
                                Student studentExpected = studentsExpected.get(index);
                                assertThat(student0ut.getId()).isEqualTo(studentExpected.getId());
                                assertThat(student0ut.getName()).isEqualTo(studentExpected.getName());
                                assertThat(student0ut.getAge()).isEqualTo(studentExpected.getAge());
                            });
                });
    }


    /**   FIND_BY_AGE  - РАБОТАЕТ ***/
    @Test
    public void findStudentsByAgeTest() throws Exception {
        Student st1 = new Student(1L, "1-й", 1);
        studentRepository.save(st1);
        Student st2 = new Student(2L, "2-й", 1);
        studentRepository.save(st2);
        Student st3 = new Student(7L, "7-й", 7);
        studentRepository.save(st3);

        List<Student> students = new ArrayList<>();
        students.add(st1);
        students.add(st2);

        when(studentRepository.findAllStudentsByAge(1)).thenReturn(students);
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/students")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }


    /**   FIND_BY_AGE_BETWEEN  - РАБОТАЕТ ***/
    @Test
    public void findStudentsByAgeBetweenTest() throws Exception {
        Student st1 = new Student(1L, "1-й", 1);
        studentRepository.save(st1);
        Student st2 = new Student(2L, "2-й", 2);
        studentRepository.save(st2);
        Student st3 = new Student(7L, "7-й", 7);
        studentRepository.save(st3);

        List<Student> students = new ArrayList<>();
        students.add(st1);
        students.add(st2);

        when(studentRepository.findStudentsByAgeBetween(1,5)).thenReturn(students);
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/students/filter?ageFrom=1&ageTo=5")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
//Commit-3
}

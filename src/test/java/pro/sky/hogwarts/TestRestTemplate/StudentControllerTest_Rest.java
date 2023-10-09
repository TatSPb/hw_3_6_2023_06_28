package pro.sky.hogwarts.TestRestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.javafaker.Faker;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import pro.sky.hogwarts.controller.*;
import pro.sky.hogwarts.entity.*;
import pro.sky.hogwarts.repository.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

/***  !!!  ВСЕ ТЕСТЫ РАБОТАЮТ ТОЛЬКО ЕСЛИ ЗАПУСКАТЬ ИХ ПО ОТДЕЛЬНОСТИ ***/

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StudentControllerTest_Rest {
    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private StudentRepository studentRepository;
    @Autowired
    private StudentController studentController;
    @Autowired
    private FacultyRepository facultyRepository;

    private ObjectMapper objectMapper = new ObjectMapper();

    private final Faker faker = new Faker();

    @AfterEach
    public void clean() {
        studentRepository.deleteAll();
    }

    @Test
    void contextLoads() throws Exception {
        Assertions.assertThat(studentRepository).isNotNull();
    }

    /*** CREATE - ОК ***/
    @Test
    public void createStudentTest() {
        Student studentIn = generateStudent();
        ResponseEntity<Student> responseEntity = testRestTemplate.postForEntity(
                "http://localhost:" + port + "/students",
                studentIn,
                Student.class
        );
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Student studentOut = responseEntity.getBody();
        assertThat(studentOut).isNotNull();
        assertThat(studentOut.getId()).isNotEqualTo(0L);
        assertThat(studentOut.getAge()).isEqualTo(studentIn.getAge());
        assertThat(studentOut.getName()).isEqualTo(studentIn.getName());
    }



    /*** UPDATE - ОК (после добавления studentRepository.save(studentWrongName);) ***/
    @Test
    public void updateStudentTest() {
        Student studentWrongName = new Student(1L, "Perviy", 20);
        studentRepository.save(studentWrongName);
        Student studentIn = new Student();
        studentIn.setId(studentWrongName.getId());
        studentIn.setName("Первый");
        studentIn.setAge(studentWrongName.getAge());

        ResponseEntity<Student> responseEntity = testRestTemplate.exchange(
                "http://localhost:" + port + "/students/" + studentWrongName.getId(),
                HttpMethod.PUT,
                new HttpEntity<>(studentIn),
                Student.class
        );
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Student studentOut = responseEntity.getBody();
        assertThat(studentOut).usingRecursiveComparison().isEqualTo(studentIn);

        //not found checking
        long incorrectId = studentWrongName.getId() + 1;
        ResponseEntity<String> stringResponseEntity = testRestTemplate.exchange(
                "http://localhost:" + port + "/students/" + incorrectId,
                HttpMethod.PUT,
                new HttpEntity<>(studentIn),
                String.class
        );
        assertThat(stringResponseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(stringResponseEntity.getBody()).isEqualTo("Студент с id = " + incorrectId + " не найден!");
    }

    /*** DELETE - OK ***/
    @Test
    public void deleteStudentTest() {
        Student studentIn = new Student(1L, "1-й", 1);
        studentRepository.save(studentIn);

        ResponseEntity<Student> responseEntity = testRestTemplate.exchange(
                "http://localhost:" + port + "/students/" + studentIn.getId(),
                HttpMethod.DELETE,
                HttpEntity.EMPTY,
                Student.class
        );
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(studentRepository.findById(1L)).isEmpty();
        assertFalse(studentRepository.findById(1L).isPresent());
    }

    /*** GET_BY_ID - OK ***/
    //ВАРИАНТ-1 (getForEntity) - РАБОТАЕТ
    @Test
    public void getStudentTest1() {
        Student studentIn = new Student(1L, "1-й", 1);
        studentRepository.save(studentIn);
        ResponseEntity<Student> responseEntity = testRestTemplate.getForEntity(
                "http://localhost:" + port + "/students/" + studentIn.getId(),
                Student.class,
                studentIn.getId()
        );
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Student studentsOut = responseEntity.getBody();
        assertThat(studentsOut).isNotNull(); // ок
        assertThat(studentsOut.getName()).isNotNull(); // ок
        assertThat(studentsOut.getName()).isEqualTo("1-й");

        //not found checking
        long incorrectId = studentIn.getId() + 1;
        ResponseEntity<String> stringResponseEntity = testRestTemplate.getForEntity(
                "http://localhost:" + port + "/students/" + incorrectId,
                String.class,
                studentIn.getId()
        );
        assertThat(stringResponseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(stringResponseEntity.getBody()).isEqualTo("Студент с id = " + incorrectId + " не найден!");
    }

    //ВАРИАНТ-2 (getForObject)- РАБОТАЕТ
    @Test
    public void getStudentTest2() {
        Student studentIn = new Student(1L, "1-й", 1);
        studentRepository.save(studentIn);
        Student studentOut = testRestTemplate.getForObject(  // getForObject не принимает тело запроса, потому что у HTTP метода GET его нет
                "http://localhost:" + port + "/students/" + studentIn.getId(),
                Student.class,
                studentIn.getId()
        );
        assertThat(studentOut.getId()).isNotEqualTo(0L);
        assertThat(studentOut.getAge()).isEqualTo(studentIn.getAge());
        assertThat(studentOut.getName()).isEqualTo(studentIn.getName());
    }

    //ВАРИАНТ-3 (usingRecursiveComparison) - РАБОТАЕТ
    @Test
    public void getStudentTest3() {
        Student studentIn = new Student(1L, "1-й", 1);
        studentRepository.save(studentIn);
        ResponseEntity<Student> responseEntity = this.testRestTemplate.getForEntity(
                "http://localhost:" + port + "/students/" + studentIn.getId(),
                Student.class);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Student studentOut = responseEntity.getBody();
        assertThat(studentOut).usingRecursiveComparison().isEqualTo(studentIn);

        //not found checking
        long incorrectId = studentIn.getId() + 1;
        ResponseEntity<String> stringResponseEntity = testRestTemplate.getForEntity(
                "http://localhost:" + port + "/students/" + incorrectId,
                String.class,
                studentIn.getId()
        );
        assertThat(stringResponseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(stringResponseEntity.getBody()).isEqualTo("Студент с id = " + incorrectId + " не найден!");
    }

    /*** GET_ALL  - РАБОТАЕТ ***/
    @Test
    public void getAllStudentsTest1() throws Exception { // пример из шпаргалки
        Assertions
                .assertThat(this.testRestTemplate.getForObject("http://localhost:" + port + "/students", String.class))
                .isNotNull();
    }

    @Test
    public void getAllStudentsTest2() { // РАБОТАЕТ
        studentRepository.save(new Student(13L, "13-й", 22));
        studentRepository.save(new Student(14L, "14-й", 20));
        studentRepository.save(new Student(15L, "15-й", 20));

        ResponseEntity<Student[]> responseEntity = testRestTemplate.getForEntity(
                "http://localhost:" + port + "/students",
                Student[].class
        );
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

        Student[] studentsOut = responseEntity.getBody();
        assertThat(studentsOut).isNotNull(); // ок
        assertThat(studentsOut[0].getName()).isNotNull(); // ок
        assertThat(studentsOut[0].getName()).isEqualTo("13-й");
        assertThat(studentsOut[1].getName()).isEqualTo("14-й");

    }

    /*** FIND_BY_AGE ***/
    @Test
    public void findStudentsByAgeTest() throws JsonProcessingException {
        Student st1 = new Student(1L, "1-й", 1);
        studentRepository.save(st1);
        Student st2 = new Student(2L, "2-й", 1);
        studentRepository.save(st2);
        Student st3 = new Student(7L, "7-й", 7);
        studentRepository.save(st3);

        Collection<Student> studentsIn = new ArrayList<>();
        studentsIn.add(st1);
        studentsIn.add(st2);


        ResponseEntity<List> responseEntity = testRestTemplate.exchange(
                "http://localhost:" + port + "/students?age=1",
                HttpMethod.GET, // метод, что делаем
                null, // тело запроса (передаем объект, когда исп-м аннотацию request body)
                List.class // класс ответа
        );

//        ResponseEntity<List> responseEntity = testRestTemplate.getForEntity(
//                "http://localhost:" + port + "/student?age=1",
//                List.class);

        List<Student> studentsOut = responseEntity.getBody();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().size()).isNotZero();
        assertThat(objectMapper.writeValueAsString(studentsOut)).isEqualTo(objectMapper.writeValueAsString(studentsIn));
    }


    /*** FIND_BY_AGE_BETWEEN ***/
    @Test
    public void findStudentsByAgeBetweenTest() throws JsonProcessingException {
        Student st1 = new Student(1L, "1-й", 1);
        studentRepository.save(st1);
        Student st2 = new Student(2L, "2-й", 2);
        studentRepository.save(st2);
        Student st3 = new Student(7L, "7-й", 7);
        studentRepository.save(st3);

        List<Student> students = new ArrayList<>();
        students.add(st1);
        students.add(st2);

        // фильтрация студентов
//        List expected = students.stream()
//                .filter(s ->
//                        s.getAge() >= 1 & s.getAge() < 5
//                ).toList();

        ResponseEntity<List> responseEntity = testRestTemplate.exchange(
                "http://localhost:" + port + "/students/filter?ageFrom=1&ageTo=5",
                HttpMethod.GET,
                null,
                List.class
        );

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().size()).isNotZero();
        assertThat(objectMapper.writeValueAsString(responseEntity.getBody())).isEqualTo(objectMapper.writeValueAsString(students));
    }

    public Student generateStudent() {
        Student generatedStudent = new Student();
        generatedStudent.setId(1L);
        generatedStudent.setAge(faker.random().nextInt(10, 20));
        generatedStudent.setName(faker.name().name());
        return generatedStudent;
    }
//Commit-3
}
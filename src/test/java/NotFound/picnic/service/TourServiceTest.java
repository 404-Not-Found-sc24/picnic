package NotFound.picnic.service;

import NotFound.picnic.domain.*;
import NotFound.picnic.dto.tour.*;
import NotFound.picnic.dto.schedule.*;
import NotFound.picnic.enums.State;
import NotFound.picnic.exception.CustomException;
import NotFound.picnic.exception.ErrorCode;
import NotFound.picnic.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-dev.properties")
class TourServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @InjectMocks
    private TourService tourService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getLocations() {
    }

    @Test
    void getSchedules() {
    }

    @Test
    void getCities() {
    }

    @Test
    void duplicateSchedule() {
    }

    @Test
    void getLocationDetail() {
    }

    @Test
    void addPlaceToSchedule() {
    }

    @Test
    void getDiaries() {
    }

    @Test
    void getDiaryDetail() {
    }

    @Test
    void getSchedulesByLocationId() {
    }

    @Test
    void addNewLocation() {
    }
}
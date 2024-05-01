package NotFound.picnic.controller;

import NotFound.picnic.dto.*;
import NotFound.picnic.service.TourService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tour")
public class TourController {
    private final TourService tourService;

    @GetMapping("/locations")
    public ResponseEntity<List<LocationGetDto>> getLocations(@RequestParam(name="city") String city, @RequestParam(required = false, defaultValue = "", name="keyword") String keyword, @RequestParam(required = false, defaultValue = "0", name="lastIdx") int lastIdx) throws UnsupportedEncodingException {
        List<LocationGetDto> locationGetDtoList = tourService.GetLocations(city, keyword, lastIdx);
        return ResponseEntity.ok().body(locationGetDtoList);
    }
    @GetMapping("/city")
    public ResponseEntity<List<CityGetDto>> getCities(){
        List <CityGetDto> CityGetDtoList = tourService.GetCities();
        return ResponseEntity.ok().body(CityGetDtoList);

    }

    @GetMapping("/schedules")
    public ResponseEntity<List<ScheduleGetDto>> getSchedules(@RequestParam(name="city") String city, @RequestParam(required = false, defaultValue = "", name="keyword") String keyword, @RequestParam(required = false, defaultValue = "0", name="lastIdx") int lastIdx) {
        List<ScheduleGetDto> schedulesGetDtoList = tourService.GetSchedules(city, keyword, lastIdx);
        return ResponseEntity.ok().body(schedulesGetDtoList);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/schedules/{scheduleId}")
    public ResponseEntity<String> duplicateSchedule(@PathVariable(name="scheduleId") Long scheduleId, @RequestBody ScheduleDuplicateDto scheduleDuplicateDto, Principal principal){
        String response = tourService.DuplicateSchedule(scheduleId, scheduleDuplicateDto, principal);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<LocationDetailDto> getLocationDetail(@PathVariable(name="locationId") Long locationId){
        LocationDetailDto locationDetailDto = tourService.GetLocationDetail(locationId);
        return ResponseEntity.ok().body(locationDetailDto);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/schedules")
    public ResponseEntity<String> addPlaceToSchedule(@RequestBody ScheduleAddPlaceDto scheduleAddPlaceDto, Principal principal){
        String response = tourService.AddPlaceToSchedule(scheduleAddPlaceDto, principal);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/diaries/{locationId}")
    public ResponseEntity<List<DiaryGetDto>> getDiaries(@PathVariable(name="locationId") Long locationId) {
        List<DiaryGetDto> diaryGetDtoList = tourService.GetDiaries(locationId);
        return ResponseEntity.ok().body(diaryGetDtoList);
    }

    @GetMapping("/schedules/{locationId}")
    public ResponseEntity<List<ScheduleGetDto>> getSchedulesByLocationId(@PathVariable(name="locationId") Long locationId) {
        List<ScheduleGetDto> scheduleGetDtoList = tourService.GetSchedulesByLocationId(locationId);
        return ResponseEntity.ok().body(scheduleGetDtoList);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/new-location")
    public ResponseEntity<String> addNewLocation(NewLocationDto newLocationDto, Principal principal){
        String response = tourService.AddNewLocation(newLocationDto, principal);
        return ResponseEntity.ok().body(response);
    }
}

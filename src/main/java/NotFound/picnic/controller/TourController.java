package NotFound.picnic.controller;

import NotFound.picnic.dto.LocationDetailDto;
import NotFound.picnic.dto.LocationGetDto;
import NotFound.picnic.dto.ScheduleGetDto;
import NotFound.picnic.dto.CityGetDto;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/tour")
public class TourController {
    private final TourService tourService;

    @GetMapping("/locations")
    public ResponseEntity<List<LocationGetDto>> getLocations(@RequestParam(name="city") String city, @RequestParam(required = false, defaultValue = "", name="keyword") String keyword) throws UnsupportedEncodingException {
        List<LocationGetDto> locationGetDtoList = tourService.GetLocations(city, keyword);
        return ResponseEntity.ok().body(locationGetDtoList);
    }
    @GetMapping("/city")
    public ResponseEntity<List<CityGetDto>> getCities(){
        List <CityGetDto> CityGetDtoList = tourService.GetCities();
        return ResponseEntity.ok().body(CityGetDtoList);

    }


    @GetMapping("/schedules")
    public ResponseEntity<List<ScheduleGetDto>> getSchedules(@RequestParam(name="city") String city, @RequestParam(required = false, defaultValue = "", name="keyword") String keyword) {
        List<ScheduleGetDto> schedulesGetDtoList = tourService.GetSchedules(city, keyword);
        return ResponseEntity.ok().body(schedulesGetDtoList);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/schedules/{scheduleId}")
    public ResponseEntity<String> duplicateSchedule(@PathVariable(name="scheduleID") Long scheduleId, Principal principal){
        String response = tourService.DuplicateSchedule(scheduleId, principal);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/location/{locationId}")
    public ResponseEntity<LocationDetailDto> getLocationDetail(@PathVariable(name="locationId") Long locationId){
        LocationDetailDto locationDetailDto = tourService.GetLocationDetail(locationId);
        return ResponseEntity.ok().body(locationDetailDto);
    }
}

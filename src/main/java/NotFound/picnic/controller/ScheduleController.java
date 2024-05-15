package NotFound.picnic.controller;

import NotFound.picnic.dto.*;
import NotFound.picnic.service.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/schedule")
public class ScheduleController {
    private final ScheduleService scheduleService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping()
    public ResponseEntity<Long> createSchedule(@RequestBody ScheduleCreateDto scheduleCreateDto, Principal principal) {
        Long message = scheduleService.createSchedule(scheduleCreateDto, principal);
        return ResponseEntity.ok().body(message);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/place/{scheduleId}")
    public ResponseEntity<String> createLocations(@PathVariable(name="scheduleId") Long scheduleId, @RequestBody List<PlaceCreateDto> placeCreateDtoList, Principal principal) {
        String message = scheduleService.createLocations(scheduleId, placeCreateDtoList, principal);
        return ResponseEntity.ok().body(message);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/diary/{placeId}")
    public ResponseEntity<String> createDiary(@PathVariable(name="placeId") Long placeId, DiaryCreateDto diaryCreateDto) throws IOException {
        String message = scheduleService.createDiary(placeId, diaryCreateDto);
        return ResponseEntity.ok().body(message);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/schedules/{scheduleId}")
    public ResponseEntity<List<SchedulePlaceDiaryGetDto>> getSchedulePlaceDiary(@PathVariable(name="scheduleId") Long scheduleId, Principal principal){
        List<SchedulePlaceDiaryGetDto> schedulePlaceDiaryList = scheduleService.getSchedulePlaceDiary(scheduleId, principal);
        return ResponseEntity.ok().body(schedulePlaceDiaryList);
    }

    @GetMapping("/places/{scheduleId}")
    public ResponseEntity<List<List<PlaceGetDto>>> getPlaces(@PathVariable(name="scheduleId") Long scheduleId) {
        List<List<PlaceGetDto>> placeList = scheduleService.getPlaces(scheduleId);
        return ResponseEntity.ok().body(placeList);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("")
    public ResponseEntity<List<MyScheduleGetDto>> getSchedulesInMyPage(Principal principal) {
        List<MyScheduleGetDto> scheduleGetDtos = scheduleService.GetSchedulesInMyPage(principal);
        return ResponseEntity.ok().body(scheduleGetDtos);
    }
}

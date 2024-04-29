package NotFound.picnic.controller;

import NotFound.picnic.dto.PlaceCreateDto;
import NotFound.picnic.dto.DiaryCreateDto;
import NotFound.picnic.dto.ScheduleCreateDto;
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
@RequestMapping("/api/schedule")
public class ScheduleController {
    private final ScheduleService scheduleService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping()
    public ResponseEntity<String> createSchedule(@RequestBody ScheduleCreateDto scheduleCreateDto, Principal principal) {
        String message = scheduleService.createSchedule(scheduleCreateDto, principal);
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
}

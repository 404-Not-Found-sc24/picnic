package NotFound.picnic.controller;

import NotFound.picnic.dto.LocationGetDto;
import NotFound.picnic.service.TourService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.List;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tour")
public class TourController {
    private final TourService tourService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/locations")
    public ResponseEntity<List<LocationGetDto>> getLocations(@RequestParam(name="city") String city, @RequestParam(required = false, defaultValue = "", name="keyword") String keyword) throws UnsupportedEncodingException {
        List<LocationGetDto> locationGetDtoList = tourService.GetLocations(city, keyword);
        return ResponseEntity.ok().body(locationGetDtoList);
    }

}

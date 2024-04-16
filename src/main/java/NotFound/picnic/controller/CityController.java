package NotFound.picnic.controller;
import NotFound.picnic.service.CityService;
import NotFound.picnic.dto.CityDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;



@RestController
@RequestMapping("/tour/city")
public class CityController {
	private final CityService cityService;
	
	@GetMapping
    public ResponseEntity<List<CityDto>> getCity() {
        List<CityDto> cityDtos = cityService.getCity();
        return new ResponseEntity<>(cityDtos, HttpStatus.OK);
    }

}

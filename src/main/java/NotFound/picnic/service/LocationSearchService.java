package NotFound.picnic.service;

import NotFound.picnic.domain.Location;
import NotFound.picnic.repository.LocationRepository;
import NotFound.picnic.dto.LocationSearchDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.amazonaws.services.kms.model.NotFoundException;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class LocationSearchService {
	
    private final LocationRepository locationRepository;
    
    public List<Location> LocationSearch(LocationSearchDto locationSearchDto, Principal principal) {
    	
    	String city = locationSearchDto.getCity();
        String keyword = locationSearchDto.getKeyword();
        
        List<Location> locations = new ArrayList<>();
        
        if (city != null && keyword != null) {
            locations = locationRepository.findByCityAndNameContainingOrAddressContaining(city, keyword,keyword);
        } else if (city != null) {
            locations = locationRepository.findByCity(city);
        } else if (keyword != null) {
        	locations = locationRepository.findByNameContainingOrAddressContaining(keyword,keyword);
;
        } else {
            locations = locationRepository.findAll();
        }
        
        if (locations.isEmpty()) {
            throw new NotFoundException("검색 결과가 없습니다.");
        }
        
        return locations;
    	
    	
    }
    
    


}

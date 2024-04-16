package NotFound.picnic.service;

import NotFound.picnic.domain.City;
import NotFound.picnic.dto.CityDto;
import NotFound.picnic.repository.CityRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.amazonaws.services.kms.model.NotFoundException;


import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class CityService {
	private final CityRepository cityRepository;
	public List<CityDto> getCity() {
		List<City> cities =cityRepository.findAll();
		
		List<CityDto> cityDtos = new ArrayList<>();
		for (City city : cities) {
		    CityDto cityDto = new CityDto();
		    cityDto.setCityName(city.getName());
		    cityDto.setImageUrl(city.getImageUrl());
		    cityDtos.add(cityDto);
		}
				
		
		return cityDtos;
	}

}

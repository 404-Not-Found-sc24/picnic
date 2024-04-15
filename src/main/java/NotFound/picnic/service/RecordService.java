package NotFound.picnic.service;

import NotFound.picnic.domain.Location;
import NotFound.picnic.repository.LocationRepository;

import NotFound.picnic.domain.Member;
import NotFound.picnic.repository.MemberRepository;

import NotFound.picnic.domain.Place;
import NotFound.picnic.repository.PlaceRepository;
import NotFound.picnic.dto.PlaceCreateDto;


import NotFound.picnic.domain.Image;
import NotFound.picnic.repository.ImageRepository;




import NotFound.picnic.repository.RecordRepository;
import NotFound.picnic.domain.Record;
import NotFound.picnic.dto.RecordCreateDto;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.kms.model.NotFoundException;




@Slf4j
@Service
@RequiredArgsConstructor
public class RecordService {
	
	private final MemberRepository memberRepository;
    private final RecordRepository recordRepository;
    private final ImageRepository imageRepository; 
    
    	
    
    public String createRecord(RecordCreateDto recordcreatdto, Principal principal) {
    	
    	Optional<Member> optionalMember = memberRepository.findMemberByEmail(principal.getName());
    	Member member = optionalMember.orElseThrow(() -> new UsernameNotFoundException("유저가 존재하지 않습니다."));
        log.info("title: "+ recordcreatdto.getTitle());
        
        Record records = Record.builder()
        		.placeId(recordcreatdto.getPlaceId())
        		.title(recordcreatdto.getTitle())
        		.content(recordcreatdto.getContent())
        		.build();
        
        
        List<Image> images = new ArrayList<>();
        for (Long imageId : recordcreatdto.getImageIds()) {
            Image image = imageRepository.findById(imageId)
            		.orElseThrow(() -> new NotFoundException("이미지를 찾을 수 없습니다: " + imageId));
            images.add(image);
        }
        // Record와 이미지들의 관계 설정
        records.setImageList(images);
        
        recordRepository.save(records);
        
        
    	return "일기생성";
    	
    	
    }
    
	

}

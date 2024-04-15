package NotFound.picnic.service;

import NotFound.picnic.domain.Location;
import NotFound.picnic.repository.LocationRepository;

import NotFound.picnic.domain.Member;
import NotFound.picnic.repository.MemberRepository;

import NotFound.picnic.domain.Place;
import NotFound.picnic.repository.PlaceRepository;
import NotFound.picnic.dto.PlaceCreateDto;




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



@Slf4j
@Service
@RequiredArgsConstructor
public class RecordService {
	
	private final MemberRepository memberRepository;
    private final PlaceRepository placeRepository;
    private final RecordRepository recordRepository;
    
    	
    
    public String createRecord(RecordCreateDto recordcreatdto, Principal principal) {
    	
    	Optional<Member> optionalMember = memberRepository.findMemberByEmail(principal.getName());
    	Member member = optionalMember.orElseThrow(() -> new UsernameNotFoundException("유저가 존재하지 않습니다."));
        log.info("title"+ recordcreatdto.getTitle());
        
        Record records = Record.builder()
        		.placeId(recordcreatdto.getPlaceId())
        		.title(recordcreatdto.getTitle())
        		.content(recordcreatdto.getContent())
        		.build();
        
        recordRepository.save(records);
        
        
    	return "일기생성";
    	
    	
    }
    
	

}

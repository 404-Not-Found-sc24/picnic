package NotFound.picnic.schedule;

import NotFound.picnic.repository.RecordRepository;
import NotFound.picnic.domain.Record;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/schedule/record")
public class post_record {
	private final RecordRepository recordRepository;
	
	public post_record(RecordRepository recordRepository) {
		this.recordRepository=recordRepository;
	}
	@PostMapping
	public Record createRecord(@RequestBody  Record record) {
		return recordRepository.save(record);
	}
	
	@PostMapping("/{recordId}")
	public ResponseEntity<Record> reRecord(@PathVariable Long recordId, @RequestBody  Record record) {
		return (!recordRepository.existsById(recordId))
				? new ResponseEntity<>(recordRepository.save(record), HttpStatus.CREATED) :
					new ResponseEntity<>(recordRepository.save(record), HttpStatus.OK );
					
				
	}
	
	@DeleteMapping("/{recordId}")
		void deleteRecord(@PathVariable Long recordId) {
			recordRepository.deleteById(recordId);
		}
	
	@DeleteMapping("/{title}")
	void deleteRecord(@PathVariable String title) {
		recordRepository.deleteBytitle(title);
	}
	

}

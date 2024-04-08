package NotFound.picnic.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long culture;

    @Column
    private String title;

    @Column
    private String content;

    @CreatedDate
    private LocalDateTime createAt;

    @LastModifiedDate
    private LocalDateTime modifiedAt;

    @Column
    private int type;

    @PrePersist
    public void prePersist() {
        // 서울 시간대로 현재 시간을 설정
        if (this.createAt == null) {
            this.createAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        }
        if (this.modifiedAt == null) {
            this.modifiedAt = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        }
    }
}

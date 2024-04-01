package NotFound.picnic.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.dao.RecoverableDataAccessException;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long imageId;

    @Column
    private Long imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "record_id")
    @ToString.Exclude
    private Record record;
}

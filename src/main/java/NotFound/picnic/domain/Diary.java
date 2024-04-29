package NotFound.picnic.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Diary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long diaryId;

    @Column
    private String title;

    @Column
    private String content;

    @Column
    private String weather;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "place_id")
    @ToString.Exclude
    private Place place;

    @OneToMany(mappedBy = "diary", fetch = FetchType.LAZY, cascade = { CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
    @ToString.Exclude
    private List<Image> imageList;
}

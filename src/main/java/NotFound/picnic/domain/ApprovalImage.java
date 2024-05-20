package NotFound.picnic.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ApprovalImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long approvalImgId;

    @Column
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "approval_id")
    @ToString.Exclude
    private Approval approval;

}

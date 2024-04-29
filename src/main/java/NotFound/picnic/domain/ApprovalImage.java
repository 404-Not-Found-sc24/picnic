package NotFound.picnic.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
